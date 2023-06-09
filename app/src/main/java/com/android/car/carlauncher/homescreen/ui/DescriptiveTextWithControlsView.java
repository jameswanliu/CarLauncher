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
import android.view.View;

/**
 * 一种显示文本标题行的布局，下面有一个副标题行和三个按钮
 * 控制音频。使用card_content_descriptive_text_with_controls.xml显示
 */
public class DescriptiveTextWithControlsView extends CardContent {

    private Drawable mImage;
    private CharSequence mTitle;
    private CharSequence mSubtitle;

    private Control mLeftControl;
    private Control mCenterControl;
    private Control mRightControl;
    private long mStartTime;

    public DescriptiveTextWithControlsView(Drawable image, CharSequence title,
            CharSequence subtitle) {
        mImage = image;
        mTitle = title;
        mSubtitle = subtitle;
    }

    public DescriptiveTextWithControlsView(Drawable image, CharSequence title,
            CharSequence subtitle,Control leftControl, Control centerControl, Control rightControl) {
        mImage = image;
        mTitle = title;
        mSubtitle = subtitle;
        mLeftControl = leftControl;
        mCenterControl = centerControl;
        mRightControl = rightControl;
    }

    public DescriptiveTextWithControlsView(Drawable image, CharSequence title,
            CharSequence subtitle, long startTime, Control leftControl,
            Control centerControl, Control rightControl) {
        mImage = image;
        mTitle = title;
        mSubtitle = subtitle;
        mStartTime = startTime;
        mLeftControl = leftControl;
        mCenterControl = centerControl;
        mRightControl = rightControl;
    }

    @Override
    public HomeCardContentType getType() {
        return HomeCardContentType.DESCRIPTIVE_TEXT_WITH_CONTROLS;
    }

    public Drawable getImage() {
        return mImage;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getSubtitle() {
        return mSubtitle;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public Control getLeftControl() {
        return mLeftControl;
    }

    public Control getCenterControl() {
        return mCenterControl;
    }

    public Control getRightControl() {
        return mRightControl;
    }

    /**
     * 一种显示文本标题行的布局，下面有一个副标题行和三个按钮
     * 控制音频。使用card_content_descriptive_text_with_controls.xml显示
     */
    public static class Control {

        private Drawable mIcon;
        private View.OnClickListener mOnClickListener;

        public Control(Drawable icon, View.OnClickListener listener) {
            mIcon = icon;
            mOnClickListener = listener;
        }

        public Drawable getIcon() {
            return mIcon;
        }

        public View.OnClickListener getOnClickListener() {
            return mOnClickListener;
        }
    }
}
