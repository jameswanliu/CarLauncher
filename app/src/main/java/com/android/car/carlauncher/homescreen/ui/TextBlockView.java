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

/**
 * 显示潜在多行文本块的布局
 */
public class TextBlockView extends CardContent {

    private CharSequence mText;
    private CharSequence mFooter;

    public TextBlockView(CharSequence text) {
        this(text, /* footer = */ null);
    }

    public TextBlockView(CharSequence text, CharSequence footer) {
        mText = text;
        mFooter = footer;
    }

    @Override
    public HomeCardContentType getType() {
        return HomeCardContentType.TEXT_BLOCK;
    }

    /**
     * 返回文本
     */
    public CharSequence getText() {
        return mText;
    }

    public CharSequence getFooter() {
        return mFooter;
    }
}
