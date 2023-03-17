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

package com.android.car.carlauncher.homescreen;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.android.car.carlauncher.homescreen.ui.CardContent;
import com.android.car.carlauncher.homescreen.ui.CardHeader;

import java.util.List;

/**
*定义家庭应用程序上卡的接口。
*卡片遵循模型视图演示者体系结构设计模式，以分离功能
*可扩展且易于测试。
* 
*卡片的布局分为两部分：
*（1）标题-卡片数据的来源。这是源应用程序的名称和应用程序图标。
*（2）内容——数据本身。这可能包括文本、图像等。
*/
public interface HomeCardInterface {

    /**
     * 视图是用户与之交互的内容。
     *
     * 视图公开的方法将由其演示者调用。
     * 视图应仅负责提供UI；用于确定卡的布局的逻辑，以及内容由演示者处理。
     */
    interface View {

        /**
         * 设置将管理此视图的｛@link Presenter｝。
         */
        void setPresenter(Presenter presenter);

        /**
         * 由演示者调用以在没有数据时从视图中删除整个卡显示。
         */
        void hideCard();

        /**
         * 当卡片内容的来源发生变化时，由演示者调用。
         * 这将更新卡上显示的应用程序名称和应用程序图标。
         */
        void updateHeaderView(CardHeader header);

        /**
         * 由演示者调用以更新卡片的内容。
         */
        void updateContentView(CardContent content);

        /**
         * 返回与视图关联的｛@link Fragment｝。
         */
        Fragment getFragment();
    }

    /**
     * 演示者将视图连接到模型。
     *
     * 它访问并格式化模型中的数据，并更新视图以显示数据
     */
    interface Presenter {

        /**
         * 设置｛@link View｝，这是演示者将更新的卡的UI。
         */
        void setView(View view);


        /**
         * 设置演示者将用作内容源的｛@link Model｝列表。
         */
        void setModels(List<Model> models);

        /**
         * 在视图创建后由视图调用。
         * 这向演示者发出信号，要求其初始化将用作数据源的相关模型并开始侦听更新。
         */
        void onViewCreated();

        /**
         * 当视图被破坏时由视图调用，以允许演示者清理任何模型
         */
        void onViewDestroyed();

        /**
         * 单击时由视图调用
         */
        default void onViewClicked(android.view.View v) {};

        /**
         * 当演示者的一个模型更新了要显示的信息时，由其调用卡。
         */
        void onModelUpdated(Model model);
    }

    /**
     * 模型定义了要在主屏幕上显示在卡中的数据。 
     *
     * 卡片的标题与正文不同，因为正文可能会更频繁地更新。
     * 例如，当用户从单个应用程序收听媒体时，标题（源应用程序）在主体（歌曲标题）更改时保持不变。
     */
    interface Model {

        /**
         * 获取要为模型显示的｛@link CardHeader｝。
         * 如果没有要显示的内容，则返回null。
         */
        CardHeader getCardHeader();

        /**
         * 获取要为模型显示的｛@link CardContent｝ 
         */
        CardContent getCardContent();

        /**
         * 设置模型的演示者。模型更新其演示者的更改和演示者管理更新UI。
         */
        void setPresenter(Presenter presenter);

        /**
         * 由演示者调用以在创建视图时创建模型。
         * 应在使用setPresenter设置模型的Presenter后调用
         */
        default void onCreate(Context context) {};

        /**
         * 由演示者调用以在视图被破坏时破坏模型
         */
        default void onDestroy(Context context) {};

        /**
         * 由演示者调用以在单击视图时进行处理
         */
        default void onClick(android.view.View view) {};
    }
}
