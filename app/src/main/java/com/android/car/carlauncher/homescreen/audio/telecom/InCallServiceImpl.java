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

package com.android.car.carlauncher.homescreen.audio.telecom;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

import com.android.car.carlauncher.homescreen.audio.InCallModel;

import java.util.ArrayList;

/**
 *｛@link InCallService｝的实现，这是一个｛@linkandroid.televator｝服务，必须是
 * 由希望提供管理电话呼叫功能的应用程序实现。此服务
 * 由android电信和｛@link InCallModel｝绑定。
 */
public class InCallServiceImpl extends InCallService {
    private static final String TAG = "Home.InCallServiceImpl";
    private static final boolean DEBUG = false;

    /**
     * 指示绑定来自本地组件的操作。本地组件必须使用此操作才能绑定服务。
     */
    public static final String ACTION_LOCAL_BIND = "local_bind";

    private ArrayList<InCallListener> mInCallListeners = new ArrayList<>();

    @Override
    public void onCallAdded(Call call) {
        if (DEBUG) Log.d(TAG, "onCallAdded: " + call);
        for (InCallListener listener : mInCallListeners) {
            listener.onCallAdded(call);
        }
    }

    @Override
    public void onCallRemoved(Call call) {
        if (DEBUG) Log.d(TAG, "onCallRemoved: " + call);
        for (InCallListener listener : mInCallListeners) {
            listener.onCallRemoved(call);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onBind, intent: " + intent);
        return ACTION_LOCAL_BIND.equals(intent.getAction())
                ? new LocalBinder()
                : super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onUnbind, intent: " + intent);
        if (ACTION_LOCAL_BIND.equals(intent.getAction())) {
            return false;
        }
        return super.onUnbind(intent);
    }

    /**
     * 为｛@link InCallService｝事件添加侦听器
     */
    public void addListener(InCallListener listener) {
        mInCallListeners.add(listener);
    }

    /**
     * 这个类，用于客户端Binder访问服务。
     */
    public class LocalBinder extends Binder {

        /**
         * 如果在Home App进程中运行，则返回｛@link InCallServiceImpl｝的此实例，否则为null
         */
        public InCallServiceImpl getService() {
            if (getCallingPid() == Process.myPid()) {
                return InCallServiceImpl.this;
            }
            return null;
        }
    }

    /**
     * 侦听｛@link#onCallAdded（Call）｝和｛@link#onCallRemoved（Call）}事件
     */
    public interface InCallListener {
        /**
         * 当｛@link Call｝已添加到此呼叫中会话时调用，通常表示已收到呼叫。
         */
        void onCallAdded(Call call);

        /**
         * 当｛@link Call｝已从此呼叫会话中删除时调用，通常表示呼叫已结束。
         */
        void onCallRemoved(Call call);
    }
}
