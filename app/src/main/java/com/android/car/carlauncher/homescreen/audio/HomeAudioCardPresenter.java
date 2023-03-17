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

package com.android.car.carlauncher.homescreen.audio;

import android.view.View;

import com.android.car.carlauncher.homescreen.CardPresenter;
import com.android.car.carlauncher.homescreen.HomeCardInterface;
import com.android.car.media.common.PlaybackControlsActionBar;

import java.util.List;

/**
 * 声卡的｛@link CardPresenter｝。
 * 对于声卡，｛@link AudioFragment｝实现视图并显示来自｛@linkMediaViewModel｝的媒体信息。
 */
public class HomeAudioCardPresenter extends CardPresenter {

    private HomeCardInterface.Model mCurrentModel;
    private List<HomeCardInterface.Model> mModelList;
    private MediaViewModel mMediaViewModel;

    @Override
    public void setModels(List<HomeCardInterface.Model> models) {
        mModelList = models;
    }

    /**
     * 创建视图时调用
     */
    @Override
    public void onViewCreated() {
        for (HomeCardInterface.Model model : mModelList) {
            if (model.getClass() == MediaViewModel.class) {
                mMediaViewModel = (MediaViewModel) model;
            }
            model.setPresenter(this);
            model.onCreate(getFragment().requireContext());
        }
    }

    /**
     * 在视图被破坏时调用
     */
    @Override
    public void onViewDestroyed() {
        if (mModelList != null) {
            for (HomeCardInterface.Model model : mModelList) {
                model.onDestroy(getFragment().requireContext());
            }
        }
    }

    /**
     * 单击视图时调用
     */
    @Override
    public void onViewClicked(View v) {
        mCurrentModel.onClick(v);
    }

    /**
     * 当模型具有新内容时，适当更新视图。
     * 如果更新的模型包含内容，则无论当前显示的内容如何，都会显示该模型卡。
     * 否则，如果显示的模型正在更新为空内容（例如，当调用结束，InCallModel标头和内容更新为空），
     * 默认为显示媒体模型，如果它有内容
     */
    @Override
    public void onModelUpdated(HomeCardInterface.Model model) {
        // 空卡片标题表示模型没有要显示的内容
        if (model.getCardHeader() == null) {
            if (mCurrentModel != null && model.getClass() == mCurrentModel.getClass()) {
                // 如果当前显示的模型正在更新为空内容，请检查是否存在
                // 是要显示的媒体内容。如果没有媒体内容，超级方法是
                // 用空内容调用，这会隐藏卡片。
                if (mMediaViewModel != null && mMediaViewModel.getCardHeader() != null) {
                    mCurrentModel = mMediaViewModel;
                    super.onModelUpdated(mMediaViewModel);
                    return;
                }
            } else {
                // 否则，另一个模型已在显示中，因此不要使用此更新
                // 空内容，因为那样会隐藏卡片。
                return;
            }
        }
        mCurrentModel = model;
        super.onModelUpdated(model);
    }

    void initializeControlsActionBar(View actionBar) {
        // TODO（b/159452592）：实现媒体控制栏，而不是使用PlaybackControlsActionBar
        // PlaybackControlsActionBar需要直接访问PlaybackViewModel
        // 将视图直接连接到模型。为了配合家庭应用程序的设计
        // 应该由HomeAudioCardPresenter进行调解。使用这些现有类
        // 直到逻辑可以被引入MediaViewModel。
        ((PlaybackControlsActionBar) actionBar).setModel(mMediaViewModel.getPlaybackViewModel(),
                getFragment().getViewLifecycleOwner());
    }
}
