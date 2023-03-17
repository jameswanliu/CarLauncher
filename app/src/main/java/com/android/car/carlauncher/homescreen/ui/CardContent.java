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
 * 定义家庭应用卡标题下方显示的内容。
 */
public abstract class CardContent {

    /**
     * 家庭应用程序支持的有限布局集，每个布局对应一个XML文件。
     * 描述文本：card_content_DESCRIPTIVE_TEXT_only.xml
     * DESCRIPTIVE_TEXT_WITH_CONTROLS:card_content_DESCRIPTIVE_TEXT_WITH_CONTROLS.xml
     * TEXT_BLOCK:card_content_TEXT_BLOCK.xml
     */
    public enum HomeCardContentType {
        DESCRIPTIVE_TEXT,
        DESCRIPTIVE_TEXT_WITH_CONTROLS,
        TEXT_BLOCK,
    }

    /**
     * 返回内容布局的类型
     */
    public abstract HomeCardContentType getType();
}
