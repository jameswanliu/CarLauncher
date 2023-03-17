/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.carlauncher;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.annotation.Nullable;
import android.app.Activity;
import android.app.ActivityOptions;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.content.pm.CarPackageManager;
import android.car.media.CarMediaManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Process;
import android.service.media.MediaBrowserService;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 包含应用程序启动程序类使用的助手方法的Util类
 */
class AppLauncherUtils {
    private static final String TAG = "AppLauncherUtils";

    @Retention(SOURCE)
    @IntDef({APP_TYPE_LAUNCHABLES, APP_TYPE_MEDIA_SERVICES})
    @interface AppTypes {}
    static final int APP_TYPE_LAUNCHABLES = 1;
    static final int APP_TYPE_MEDIA_SERVICES = 2;

    private AppLauncherUtils() {
    }

    /**
     * 排序列表的｛@link AppMetaData｝的比较器
     * 按“displayName”属性升序排列。
     */
    static final Comparator<AppMetaData> ALPHABETICAL_COMPARATOR = Comparator
            .comparing(AppMetaData::getDisplayName, String::compareToIgnoreCase);

    /**
     * 在给定应用程序的AppMetaData的情况下启动应用程序的Helper方法。
     *
     * @param app 请求应用的AppMetaData
     */
    static void launchApp(Context context, Intent intent) {
        ActivityOptions options = ActivityOptions.makeBasic();

        // 在当前的车载系统屏幕上启动目标App的Activity
        options.setLaunchDisplayId(context.getDisplayId());
        context.startActivity(intent, options.toBundle());
    }

    /** 捆绑应用程序和服务信息。 */
    static class LauncherAppsInfo {
        /*
         * 所有汽车发射器组件的地图（包括发射器活动和媒体服务）
         * 由ComponentName键入的元数据。    
         */
        private final Map<ComponentName, AppMetaData> mLaunchables;

        /** 由ComponentName键入的所有媒体服务的映射。 */
        private final Map<ComponentName, ResolveInfo> mMediaServices;

        LauncherAppsInfo(@NonNull Map<ComponentName, AppMetaData> launchablesMap,
                @NonNull Map<ComponentName, ResolveInfo> mediaServices) {
            mLaunchables = launchablesMap;
            mMediaServices = mediaServices;
        }

        /** 如果所有映射都为空，则返回true。 */
        boolean isEmpty() {
            return mLaunchables.isEmpty() && mMediaServices.isEmpty();
        }

        /**
         * 返回给定的componentName是否为媒体服务。
         */
        boolean isMediaService(ComponentName componentName) {
            return mMediaServices.containsKey(componentName);
        }

        /** 返回 {@link AppMetaData} 对于给定的componentName */
        @Nullable
        AppMetaData getAppMetaData(ComponentName componentName) {
            return mLaunchables.get(componentName);
        }

        /** 返回所有可启动组件的新列表 {@link AppMetaData}. */
        @NonNull
        List<AppMetaData> getLaunchableComponentsList() {
            return new ArrayList<>(mLaunchables.values());
        }
    }

    private final static LauncherAppsInfo EMPTY_APPS_INFO = new LauncherAppsInfo(
            Collections.emptyMap(), Collections.emptyMap());

    /*
     * 获取给定包中的媒体源。如果包中有多个源，
     * 返回第一个。
     */
    static ComponentName getMediaSource(@NonNull PackageManager packageManager,
            @NonNull String packageName) {
        Intent mediaIntent = new Intent();
        mediaIntent.setPackage(packageName);
        mediaIntent.setAction(MediaBrowserService.SERVICE_INTERFACE);

        List<ResolveInfo> mediaServices = packageManager.queryIntentServices(mediaIntent,
                PackageManager.GET_RESOLVED_FILTER);

        if (mediaServices == null || mediaServices.isEmpty()) {
            return null;
        }
        String defaultService = mediaServices.get(0).serviceInfo.name;
        if (!TextUtils.isEmpty(defaultService)) {
            return new ComponentName(packageName, defaultService);
        }
        return null;
    }

    /**
     * 获取我们希望在启动器中以未排序的顺序看到的所有组件，包括启动器活动和媒体服务。
     *
     * @param blackList             要隐藏的应用程序（包名称）列表（可能为空）
     * @param customMediaComponents 不应在Launcher中显示的媒体组件（组件名称）列表（可能为空），因为将显示其应用程序的Launcher活动
     * @param appTypes              要显示的应用程序类型（例如：全部或仅媒体源）
     * @param openMediaCenter       当用户选择媒体源时，启动器是否应导航到media center。
     * @param launcherApps          {@link LauncherApps}系统服务
     * @param carPackageManager     {@link CarPackageManager}系统服务
     * @param packageManager        {@link PackageManager}系统服务
     * @return 一个新的 {@link LauncherAppsInfo}
     */
    @NonNull
    static LauncherAppsInfo getLauncherApps(
            @NonNull Set<String> appsToHide,
            @NonNull Set<String> customMediaComponents,
            @AppTypes int appTypes,
            boolean openMediaCenter,
            LauncherApps launcherApps,
            CarPackageManager carPackageManager,
            PackageManager packageManager,
            CarMediaManager carMediaManager) {

        if (launcherApps == null || carPackageManager == null || packageManager == null
                || carMediaManager == null) {
            return EMPTY_APPS_INFO;
        }

        // 检索所有符合给定intent的服务
        List<ResolveInfo> mediaServices = packageManager.queryIntentServices(
                new Intent(MediaBrowserService.SERVICE_INTERFACE),
                PackageManager.GET_RESOLVED_FILTER);

        // 检索指定packageName的Activity的列表
        List<LauncherActivityInfo> availableActivities =
                launcherApps.getActivityList(null, Process.myUserHandle());

        Map<ComponentName, AppMetaData> launchablesMap = new HashMap<>(
                mediaServices.size() + availableActivities.size());
        Map<ComponentName, ResolveInfo> mediaServicesMap = new HashMap<>(mediaServices.size());

        // Process media services
        if ((appTypes & APP_TYPE_MEDIA_SERVICES) != 0) {
            for (ResolveInfo info : mediaServices) {
                String packageName = info.serviceInfo.packageName;
                String className = info.serviceInfo.name;
                ComponentName componentName = new ComponentName(packageName, className);
                mediaServicesMap.put(componentName, info);
                if (shouldAddToLaunchables(componentName, appsToHide, customMediaComponents,
                        appTypes, APP_TYPE_MEDIA_SERVICES)) {
                    final boolean isDistractionOptimized = true;

                    Intent intent = new Intent(Car.CAR_INTENT_ACTION_MEDIA_TEMPLATE);
                    intent.putExtra(Car.CAR_EXTRA_MEDIA_COMPONENT, componentName.flattenToString());

                    AppMetaData appMetaData = new AppMetaData(
                        info.serviceInfo.loadLabel(packageManager),
                        componentName,
                        info.serviceInfo.loadIcon(packageManager),
                        isDistractionOptimized,
                        context -> {
                            if (openMediaCenter) {
                                AppLauncherUtils.launchApp(context, intent);
                            } else {
                                selectMediaSourceAndFinish(context, componentName, carMediaManager);
                            }
                        },
                        context -> {
                            // 返回系统中所有MainActivity带有Intent.CATEGORY_INFO 和 Intent.CATEGORY_LAUNCHER的intent
                            Intent packageLaunchIntent =
                                    packageManager.getLaunchIntentForPackage(packageName);
                            AppLauncherUtils.launchApp(context,
                                    packageLaunchIntent != null ? packageLaunchIntent : intent);
                        });
                    launchablesMap.put(componentName, appMetaData);
                }
            }
        }

        // for循环来获取所有应用信息
        if ((appTypes & APP_TYPE_LAUNCHABLES) != 0) {
            for (LauncherActivityInfo info : availableActivities) {
                ComponentName componentName = info.getComponentName();
                String packageName = componentName.getPackageName();
                if (shouldAddToLaunchables(componentName, appsToHide, customMediaComponents,
                        appTypes, APP_TYPE_LAUNCHABLES)) {
                    boolean isDistractionOptimized =
                        isActivityDistractionOptimized(carPackageManager, packageName,
                            info.getName());

                    Intent intent = new Intent(Intent.ACTION_MAIN)
                        .setComponent(componentName)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // 获取app的name，和 app的图标
                    AppMetaData appMetaData = new AppMetaData(
                        info.getLabel(),
                        componentName,
                        info.getBadgedIcon(0),
                        isDistractionOptimized,

                        // 【Derry提醒你，下面的分析，就是这个launchApp方法】
                        context -> AppLauncherUtils.launchApp(context, intent),
                        null);
                    launchablesMap.put(componentName, appMetaData);
                }
            }
        }

        return new LauncherAppsInfo(launchablesMap, mediaServicesMap);
    }

    private static boolean shouldAddToLaunchables(@NonNull ComponentName componentName,
            @NonNull Set<String> appsToHide,
            @NonNull Set<String> customMediaComponents,
            @AppTypes int appTypesToShow,
            @AppTypes int componentAppType) {
        if (appsToHide.contains(componentName.getPackageName())) {
            return false;
        }
        switch (componentAppType) {
            // 流程媒体服务
            case APP_TYPE_MEDIA_SERVICES:
                // 对于customMediaComponents中的媒体服务，如果其应用程序的启动器
                // 活动将显示在Launcher中，不要在发射器
                if (customMediaComponents.contains(componentName.flattenToString())
                        && (appTypesToShow & APP_TYPE_LAUNCHABLES) != 0) {
                    return false;
                }
                return true;
            // Process activities
            case APP_TYPE_LAUNCHABLES:
                return true;
            default:
                Log.e(TAG, "无效的componentAppType : " + componentAppType);
                return false;
        }
    }

    private static void selectMediaSourceAndFinish(Context context, ComponentName componentName,
            CarMediaManager carMediaManager) {
        try {
            carMediaManager.setMediaSource(componentName, CarMediaManager.MEDIA_SOURCE_MODE_BROWSE);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "车辆未连接", e);
        }
    }

    /**
     * 获取活动是否经过了分心优化
     *
     * @param carPackageManager 这个 {@link CarPackageManager} 是系统的服务
     * @param packageName       这个是应用程序的程序包名称
     * @param activityName      这个是请求的活动名称
     * @return 如果提供的活动是分散注意力的最佳活动，则为true
     */
    static boolean isActivityDistractionOptimized(
            CarPackageManager carPackageManager, String packageName, String activityName) {
        boolean isDistractionOptimized = false;
        // 下面代码目的是 尝试获取分心优化信息
        try {
            if (carPackageManager != null) {
                isDistractionOptimized =
                        carPackageManager.isActivityDistractionOptimized(packageName, activityName);
            }
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "获取DO信息时车辆未连接", e);
        }
        return isDistractionOptimized;
    }
}
