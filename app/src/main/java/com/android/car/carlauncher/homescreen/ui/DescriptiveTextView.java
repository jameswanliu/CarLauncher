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
 * 显示文本标题行并在其下方显示字幕行的布局
 */
public class DescriptiveTextView extends CardContent {

    private Drawable mImage;
    private CharSequence mTitle;
    private CharSequence mSubtitle;
    private CharSequence mFooter;

    public DescriptiveTextView(Drawable image, CharSequence title, CharSequence subtitle) {
        this(image, title, subtitle, /* footer = */ null);
    }

    public DescriptiveTextView(Drawable image, CharSequence title, CharSequence subtitle,
            CharSequence footer) {
        mImage = image;
        mTitle = title;
        mSubtitle = subtitle;
        mFooter = footer;
    }

    @Override
    public HomeCardContentType getType() {
        return HomeCardContentType.DESCRIPTIVE_TEXT;
    }

    /**
     * 返回标题文本
     */
    public CharSequence getTitle() {
        return mTitle;
    }

    /**
     * 返回副标题文本
     */
    public CharSequence getSubtitle() {
        return mSubtitle;
    }

    public CharSequence getFooter() {
        return mFooter;
    }

    public Drawable getImage() {
        return mImage;
    }
}
