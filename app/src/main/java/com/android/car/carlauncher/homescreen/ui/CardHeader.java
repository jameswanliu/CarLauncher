/*
 * Copyright (C) 2020 Google Inc.
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

package com.android.car.carlauncher.homescreen.ui;

import android.graphics.drawable.Drawable;

/**
 * 家庭应用卡的标题显示提供的应用的名称和图标
 * 显示的数据。
 */
public final class CardHeader {
    private final CharSequence mCardTitle;
    private final Drawable mCardIcon;

    public CardHeader(CharSequence appName, Drawable cardIcon) {
        mCardTitle = appName;
        mCardIcon = cardIcon;
    }

    /**
     * 返回源应用程序的名称 
     */
    public CharSequence getCardTitle() {
        return mCardTitle;
    }

    /**
     * 返回源应用程序的图标
     */
    public Drawable getCardIcon() {
        return mCardIcon;
    }
}
