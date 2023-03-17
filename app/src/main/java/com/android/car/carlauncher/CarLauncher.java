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

import static android.app.ActivityTaskManager.INVALID_TASK_ID;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY;

import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.PendingIntent;
import android.app.TaskStackListener;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.collection.ArraySet;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.android.car.carlauncher.homescreen.HomeCardModule;
import com.android.car.internal.common.UserHelperLite;
import com.android.wm.shell.TaskView;
import com.android.wm.shell.common.HandlerExecutor;

import java.net.URISyntaxException;
import java.util.Set;

import android.widget.Toast; // Derry新增点
import android.util.Log; // Derry新增点

/**
 * Android Automotive的Basic Launcher，演示了使用｛@link TaskView｝托管
 * 映射内容，并使用模型视图演示器结构在卡片中显示内容。
 * ＜p＞使用主活动的给定布局的Launcher的实现
 *（car_launcher.xml）可以通过提供自己的主屏幕卡来定制主屏幕卡
 *｛@link HomeCardModule｝用于R.id.top_card或R.id.bottom_card。
 * 否则使用自己的布局应该定义自己的活动，而不是使用这个。
 * ＜p＞注意：在某些设备上，TaskView可能会以宽度、高度和/或纵横比进行渲染
 * 不符合Android兼容性定义的比率。开发人员应使用内容
 * 所有者确保在扩展或模拟此类时正确呈现内容。
 */
public class CarLauncher extends FragmentActivity {
    public static final String TAG = "CarLauncher";
    private static final boolean DEBUG = false;

    private TaskViewManager mTaskViewManager;
    private TaskView mTaskView;
    private boolean mTaskViewReady;
    // 跟踪此项以检查TaskView中的任务是否在后台崩溃
    private int mTaskViewTaskId = INVALID_TASK_ID;
    private boolean mIsResumed;
    private boolean mFocused;
    private int mCarLauncherTaskId = INVALID_TASK_ID;
    private Set<HomeCardModule> mHomeCardModules;

    /** 在我们记录“活动”已完全绘制后，设置为｛@code true｝。 */
    private boolean mIsReadyLogged;

    // ｛@code mTaskViewListener｝中的回调方法正在MainThread下运行。
    private final TaskView.Listener mTaskViewListener =  new TaskView.Listener() {
        @Override
        public void onInitialized() {
            if (DEBUG) Log.d(TAG, "onInitialized(" + getUserId() + ")");
            mTaskViewReady = true;
            startMapsInTaskView();
            maybeLogReady();
        }

        @Override
        public void onReleased() {
            if (DEBUG) Log.d(TAG, "onReleased(" + getUserId() + ")");
            mTaskViewReady = false;
        }

        @Override
        public void onTaskCreated(int taskId, ComponentName name) {
            if (DEBUG) Log.d(TAG, "onTaskCreated: taskId=" + taskId);
            mTaskViewTaskId = taskId;
        }

        @Override
        public void onTaskRemovalStarted(int taskId) {
            if (DEBUG) Log.d(TAG, "onTaskRemovalStarted: taskId=" + taskId);
            mTaskViewTaskId = INVALID_TASK_ID;
        }
    };

    private final TaskStackListener mTaskStackListener = new TaskStackListener() {
        @Override
        public void onTaskFocusChanged(int taskId, boolean focused) {
            mFocused = taskId == mCarLauncherTaskId && focused;
            if (DEBUG) {
                Log.d(TAG, "onTaskFocusChanged: mFocused=" + mFocused
                        + ", mTaskViewTaskId=" + mTaskViewTaskId);
            }
            if (mFocused && mTaskViewTaskId == INVALID_TASK_ID) {
                startMapsInTaskView();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCarLauncherTaskId = getTaskId();
        ActivityTaskManager.getInstance().registerTaskStackListener(mTaskStackListener);

        // 设置为可信覆盖，让触摸通过。
        getWindow().addPrivateFlags(PRIVATE_FLAG_TRUSTED_OVERLAY);
        // 将触摸传递给下面的任务。
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        // 在多窗口模式下『car_launcher_multiwindow』不显示“地图”面板。
        // 注意：拆分屏幕的CTS测试与启动器默认活动的活动视图不兼容
        if (isInMultiWindowMode() || isInPictureInPictureMode()) {
            setContentView(R.layout.car_launcher_multiwindow); // Derry新增背景红色
        } else {
            setContentView(R.layout.car_launcher); // Derry新增背景绿色
            // We don't want to show Map card unnecessarily for the headless user 0.
            if (!UserHelperLite.isHeadlessSystemUser(getUserId())) {
                ViewGroup mapsCard = findViewById(R.id.maps_card);
                if (mapsCard != null) {
                    setUpTaskView(mapsCard);
                }
            }
        }

        // 此方法用于 初始化『天气』和『音乐』fragment 区域信息
        initializeCards();
        
        // Derry新增点
        Toast.makeText(this, "Derry-欢迎进入 CarLauncher onCreate", Toast.LENGTH_SHORT).show();
        Log.d("Derry", "Derry-欢迎进入 CarLauncher onCreate");
    }

    private void setUpTaskView(ViewGroup parent) {
        mTaskViewManager = new TaskViewManager(this,
                new HandlerExecutor(getMainThreadHandler()));
        mTaskViewManager.createTaskView(taskView -> {
            taskView.setListener(getMainExecutor(), mTaskViewListener);
            parent.addView(taskView);
            mTaskView = taskView;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;
        maybeLogReady();
        if (DEBUG) {
            Log.d(TAG, "onResume: mFocused=" + mFocused + ", mTaskViewTaskId=" + mTaskViewTaskId);
        }
        if (mFocused && mTaskViewTaskId == INVALID_TASK_ID) {
            // 如果TaskView中的任务在CarLauncher为后台时崩溃，
            // 我们希望在CarLauncher成为前台时重新启动它。
            startMapsInTaskView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityTaskManager.getInstance().unregisterTaskStackListener(mTaskStackListener);
        if (mTaskView != null && mTaskViewReady) {
            mTaskView.release();
            mTaskView = null;
        }
    }

    private void startMapsInTaskView() {
        if (mTaskView == null || !mTaskViewReady) {
            return;
        }
        // 如果我们碰巧重新出现在多显示模式中，我们将跳过启动内容
        // 在活动视图中，因为无论如何都会重新创建。
        if (isInMultiWindowMode() || isInPictureInPictureMode()) {
            return;
        }
        // 当ActivityVisibilityTests的显示器关闭时，不要启动地图。
        if (getDisplay().getState() != Display.STATE_ON) {
            return;
        }
        try {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this,
                    /* enterResId= */ 0, /* exitResId= */ 0);

            // 要在TaskView中显示“活动”，“活动”应位于中的主机任务上方
            // 活动堆栈。此选项仅影响主机“活动”正在恢复。
            options.setTaskAlwaysOnTop(true);
            mTaskView.startActivity(
                    PendingIntent.getActivity(this, /* requestCode= */ 0, getMapsIntent(),
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT),
                    /* fillInIntent= */ null, options, null /* launchBounds */);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Maps activity not found", e);
        }
    }

    private Intent getMapsIntent() {
        Intent defaultIntent =
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MAPS);
        PackageManager pm = getPackageManager();
        ComponentName defaultActivity = defaultIntent.resolveActivity(pm);

        for (String intentUri : getResources().getStringArray(
                R.array.config_homeCardPreferredMapActivities)) {
            Intent preferredIntent;
            try {
                preferredIntent = Intent.parseUri(intentUri, Intent.URI_ANDROID_APP_SCHEME);
            } catch (URISyntaxException se) {
                Log.w(TAG, "config_homeCardPreferredMapActivities中的intent URI无效", se);
                continue;
            }

            if (defaultActivity != null && !defaultActivity.getPackageName().equals(
                    preferredIntent.getPackage())) {
                continue;
            }

            if (preferredIntent.resolveActivityInfo(pm, /* flags= */ 0) != null) {
                return preferredIntent;
            }
        }
        return defaultIntent;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initializeCards();
    }

    private void initializeCards() {
        if (mHomeCardModules == null) {
            mHomeCardModules = new ArraySet<>();
            for (String providerClassName : getResources().getStringArray(
                    R.array.config_homeCardModuleClasses)) {
                try {
                    long reflectionStartTime = System.currentTimeMillis();
                    HomeCardModule cardModule = (HomeCardModule) Class.forName(
                            providerClassName).newInstance();
                    cardModule.setViewModelProvider(new ViewModelProvider( /* owner= */this));
                    mHomeCardModules.add(cardModule);
                    if (DEBUG) {
                        long reflectionTime = System.currentTimeMillis() - reflectionStartTime;
                        Log.d(TAG, "HomeCardModule类的初始化 " + providerClassName
                                + " took " + reflectionTime + " ms");
                    }
                } catch (IllegalAccessException | InstantiationException |
                        ClassNotFoundException e) {
                    Log.w(TAG, "无法创建HomeCardProvider类 " + providerClassName, e);
                }
            }
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (HomeCardModule cardModule : mHomeCardModules) {
            transaction.replace(cardModule.getCardResId(), cardModule.getCardView());
        }
        transaction.commitNow();
    }

    /** 记录“活动”已就绪。用于启动时诊断。 */
    private void maybeLogReady() {
        if (DEBUG) {
            Log.d(TAG, "maybeLogReady(" + getUserId() + "): activityReady=" + mTaskViewReady
                    + ", started=" + mIsResumed + ", alreadyLogged: " + mIsReadyLogged);
        }
        if (mTaskViewReady && mIsResumed) {
            // 我们应该每次都报告-Android框架将负责日志记录
            reportFullyDrawn();
            if (!mIsReadyLogged) {
                // 我们希望手动检查下面的Log.i（这对于显示用户id）只记录一次（否则每次用户点击主页）
                Log.i(TAG, "用户启动器 " + getUserId() + " is ready");
                mIsReadyLogged = true;
            }
        }
    }
}
