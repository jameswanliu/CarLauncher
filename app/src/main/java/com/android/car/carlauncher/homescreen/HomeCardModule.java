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

import android.annotation.IdRes;

import androidx.lifecycle.ViewModelProvider;

import com.android.car.carlauncher.homescreen.HomeCardInterface.Presenter;
import com.android.car.carlauncher.homescreen.HomeCardInterface.View;

import java.util.List;

/**
 * HomeCardModule创建并提供卡片下方的模型视图演示者结构在主屏幕上。
 *
 * 该类应构造所需的｛@link HomeCardInterface｝视图、演示者和模型
 * 并调用相应的设置方法。对于视图： 
 * {@link View#setPresenter(Presenter)}. 对于 Presenter:
 * {@link Presenter#setView(View)},
 * {@link Presenter#setModels(List)}
 */
public interface HomeCardModule {

    /**
     * 设置 {@link ViewModelProvider}. 它提供了 {@link androidx.lifecycle.ViewModel}s
     * 对于 {@link androidx.lifecycle.ViewModelStoreOwner} 构造时指定。
     */
    void setViewModelProvider(ViewModelProvider viewModelProvider);

    /**
     * 返回将保存此卡的容器视图的id。
     */
    @IdRes
    int getCardResId();

    /**
     * 返回卡片的｛@link Presenter｝  
     */
    CardPresenter getCardPresenter();


    /**
     * 返回卡的｛@link View｝
     */
    HomeCardFragment getCardView();

}
