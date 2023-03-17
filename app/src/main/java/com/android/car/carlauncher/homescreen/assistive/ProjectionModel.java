/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.car.carlauncher.homescreen.assistive;

import android.annotation.Nullable;
import android.car.Car;
import android.car.CarProjectionManager;
import android.car.projection.ProjectionStatus;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.car.carlauncher.homescreen.HomeCardInterface;
import com.android.car.carlauncher.R;
import com.android.car.carlauncher.homescreen.ui.CardContent;
import com.android.car.carlauncher.homescreen.ui.CardHeader;
import com.android.car.carlauncher.homescreen.ui.DescriptiveTextView;

import java.util.List;

/**
 * 投影状态的｛@link HomeCardInterface.Model｝
 */
public class ProjectionModel implements CarProjectionManager.ProjectionStatusListener,
        HomeCardInterface.Model {

    private static final String TAG = "ProjectionModel";

    private HomeCardInterface.Presenter mPresenter;
    private Car mCar;
    private CarProjectionManager mCarProjectionManager;
    private PackageManager mPackageManager;
    private Resources mResources;

    private CharSequence mAppName;
    private Drawable mAppIcon;
    private CharSequence mLaunchMessage;
    private CharSequence mStatusMessage;
    private CharSequence mTapToLaunchText;
    private Intent mIntent;

    @Override
    public void onCreate(Context context) {
        if (mCar == null) {
            mCar = Car.createCar(context);
            mCarProjectionManager = (CarProjectionManager) mCar.getCarManager(
                    Car.PROJECTION_SERVICE);
        }
        mPackageManager = context.getPackageManager();
        mResources = context.getResources();

        mCarProjectionManager.registerProjectionStatusListener(this);
        mLaunchMessage = context.getResources().getString(R.string.projected_launch_text);
        mTapToLaunchText = context.getResources().getString(R.string.tap_to_launch_text);
    }

    @Override
    public void onDestroy(Context context) {
        if (mCar != null) {
            mCar.disconnect();
        }
        if (mCarProjectionManager != null) {
            mCarProjectionManager.unregisterProjectionStatusListener(this);
        }
    }

    @Override
    public CardHeader getCardHeader() {
        return mAppName == null ? null : new CardHeader(mAppName, mAppIcon);
    }

    @Override
    public CardContent getCardContent() {
        return mAppName == null ? null : new DescriptiveTextView(mAppIcon, mLaunchMessage,
                mStatusMessage, mTapToLaunchText);
    }

    @Override
    public void onClick(View v) {
        if (mIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
            v.getContext().startActivity(mIntent);
        } else {
            Log.e(TAG, "No activity component found to handle intent with action: "
                    + mIntent.getAction());
            Toast.makeText(v.getContext(),
                    mResources.getString(R.string.projected_onclick_launch_error_toast_text),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setPresenter(HomeCardInterface.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onProjectionStatusChanged(int state, String packageName,
            List<ProjectionStatus> details) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onProjectionStatusChanged state=" + state + " package=" + packageName);
        }
        if (state == ProjectionStatus.PROJECTION_STATE_INACTIVE || packageName == null) {
            if (mAppName != null) {
                mAppName = null;
                mPresenter.onModelUpdated(this);
            }
            return;
        }

        ApplicationInfo applicationInfo;
        try {
            applicationInfo = mPackageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not load projection package information", e);
            return;
        }

        mAppName = applicationInfo.loadLabel(mPackageManager);
        mAppIcon = applicationInfo.loadIcon(mPackageManager);
        mStatusMessage = getStatusMessage(packageName, details);
        mIntent = mPackageManager.getLaunchIntentForPackage(packageName);
        mPresenter.onModelUpdated(this);
    }

    @Nullable
    private String getStatusMessage(String packageName, List<ProjectionStatus> details) {
        for (ProjectionStatus status : details) {
            if (packageName.equals(status.getPackageName())) {
                return getStatusMessage(status);
            }
        }
        return null;
    }

    @Nullable
    private String getStatusMessage(ProjectionStatus status) {
        // 状态消息如下：
        // -如果存在明确的“最佳”设备，则该设备的名称。
        // “不明确”定义为只有一个投影设备，或没有投影设备
        // 并且只有一个非投影装置。
        // -如果有多个投影装置或非投影装置，“N个装置”，其中N
        // 是投影和非投影设备的总数。
        // -如果根本没有设备，则没有消息。如果投影
        // 应用程序运行正常，但在发生投影应用程序错误时可能会发生这种情况。
        String projectingDevice = null;
        String nonProjectingDevice = null;
        int projectingDeviceCount = 0;
        int nonProjectingDeviceCount = 0;
        for (ProjectionStatus.MobileDevice device : status.getConnectedMobileDevices()) {
            if (device.isProjecting()) {
                projectingDevice = device.getName();
                projectingDeviceCount++;
            } else {
                nonProjectingDevice = device.getName();
                nonProjectingDeviceCount++;
            }
        }

        if (projectingDeviceCount == 1) {
            return projectingDevice;
        } else if (projectingDeviceCount == 0 && nonProjectingDeviceCount == 1) {
            return nonProjectingDevice;
        }

        int totalDeviceCount = projectingDeviceCount + nonProjectingDeviceCount;
        if (totalDeviceCount > 0) {
            return mResources.getQuantityString(R.plurals.projection_devices, totalDeviceCount,
                    totalDeviceCount);
        } else {
            return null;
        }
    }
}
