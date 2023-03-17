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

import android.annotation.Nullable;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.util.function.Consumer;

/**
 * 应用程序的元数据，包括显示名称、组件名称、可绘制图标和
 * 打开应用程序或媒体中心（用于媒体服务）的意图。
*/
final class AppMetaData {
    // 应用程序的显示名称
    @Nullable
    private final String mDisplayName;
    // 应用程序的组件名称
    private final ComponentName mComponentName;
    private final Drawable mIcon;
    private final boolean mIsDistractionOptimized;
    private final Consumer<Context> mLaunchCallback;
    private final Consumer<Context> mAlternateLaunchCallback;

    /**
     * 应用元数据 的 构造方法
     *
     * @param displayName            要在启动器中显示的名称
     * @param componentName          组件名称
     * @param icon                   应用程序的图标
     * @param isDistractionOptimized mainLaunchIntent是否安全驾驶
     * @param launchCallback         启动此应用程序要执行的操作
     * @param alternateLaunchCallback 要执行的临时替代操作（例如：媒体应用程序这允许打开他们自己的UI）
     */
    AppMetaData(
            CharSequence displayName,
            ComponentName componentName,
            Drawable icon,
            boolean isDistractionOptimized,
            Consumer<Context> launchCallback,
            Consumer<Context> alternateLaunchCallback) {
        mDisplayName = displayName == null ? "" : displayName.toString();
        mComponentName = componentName;
        mIcon = icon;
        mIsDistractionOptimized = isDistractionOptimized;
        mLaunchCallback = launchCallback;
        mAlternateLaunchCallback = alternateLaunchCallback;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getPackageName() {
        return getComponentName().getPackageName();
    }

    public ComponentName getComponentName() {
        return mComponentName;
    }

    Consumer<Context> getLaunchCallback() {
        return mLaunchCallback;
    }

    Consumer<Context> getAlternateLaunchCallback() {
        return mAlternateLaunchCallback;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    boolean getIsDistractionOptimized() {
        return mIsDistractionOptimized;
    }

    /**
     * 两个AppMetaData的相等性取决于组件名称是否相同。
     * @param o与此AppMetaData对象进行比较的对象
     * 当两个AppMetaData具有相同的组件名称时，@return｛@code true｝
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AppMetaData)) {
            return false;
        } else {
            return ((AppMetaData) o).getComponentName().equals(mComponentName);
        }
    }

    @Override
    public int hashCode() {
        return mComponentName.hashCode();
    }
}
