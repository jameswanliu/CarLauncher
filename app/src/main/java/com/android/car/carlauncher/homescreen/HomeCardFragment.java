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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.car.apps.common.CrossfadeImageView;
import com.android.car.carlauncher.R;
import com.android.car.carlauncher.homescreen.ui.CardContent;
import com.android.car.carlauncher.homescreen.ui.CardHeader;
import com.android.car.carlauncher.homescreen.ui.DescriptiveTextView;
import com.android.car.carlauncher.homescreen.ui.DescriptiveTextWithControlsView;
import com.android.car.carlauncher.homescreen.ui.TextBlockView;

/**
 * 实现Home App的View接口的｛@link Fragment｝的抽象类。
 *｛@link HomeCardInterface.View｝扩展HomeCardFragment的类将覆盖更新
 * 他们支持的CardContent类型的方法。每个CardContent类对应于
 * 卡片上显示的布局。布局是xml文件的组合。
 *｛@link descriptive TextWithControlsView｝：card_content_scriptive_text，card_content-button_trio
 *｛@link DescriptiveTextView｝：card_content_scriptive_text，card_conten_tap_for_more_text
 *｛@link TextBlockView｝：card_content_text_block，card_conten_tap_for_more_text
*/
public class HomeCardFragment extends Fragment implements HomeCardInterface.View {

    private static final String TAG = "HomeFragment";
    private HomeCardInterface.Presenter mPresenter;
    private Size mSize;
    private View mCardBackground;
    private CrossfadeImageView mCardBackgroundImage;
    private View mRootView;
    private TextView mCardTitle;
    private ImageView mCardIcon;

    // 来自的视图 card_content_text_block.xml
    private View mTextBlockLayoutView;
    private TextView mTextBlock;
    private TextView mTextBlockTapForMore;

    // 来自的视图 card_content_descriptive_text_only.xml
    private View mDescriptiveTextOnlyLayoutView;
    private ImageView mDescriptiveTextOnlyOptionalImage;
    private TextView mDescriptiveTextOnlyTitle;
    private TextView mDescriptiveTextOnlySubtitle;
    private TextView mDescriptiveTextOnlyTapForMore;

    // 来自的视图 card_content_descriptive_text_with_controls.xml
    private View mDescriptiveTextWithControlsLayoutView;
    private ImageView mDescriptiveTextWithControlsOptionalImage;
    private TextView mDescriptiveTextWithControlsTitle;
    private TextView mDescriptiveTextWithControlsSubtitle;
    private View mControlBarView;
    private ImageButton mControlBarLeftButton;
    private ImageButton mControlBarCenterButton;
    private ImageButton mControlBarRightButton;

    @Override
    public void setPresenter(HomeCardInterface.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.card_fragment, container, false);
        mCardTitle = mRootView.findViewById(R.id.card_name);
        mCardIcon = mRootView.findViewById(R.id.card_icon);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.onViewCreated();
        mRootView.setOnClickListener(v -> mPresenter.onViewClicked(v));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onViewDestroyed();
        }
        mSize = null;
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    /**
     * 返回卡片的大小，如果视图尚未布局，则返回null
     */
    protected Size getCardSize() {
        if (mSize == null && mRootView.isLaidOut()) {
            mSize = new Size(mRootView.getWidth(), mRootView.getHeight());
        }
        return mSize;
    }

    /**
     * 从视图中移除声卡
     */
    @Override
    public void hideCard() {
        hideAllViews();
        mRootView.setVisibility(View.GONE);
    }

    /**
     * 更新卡片标题：源应用程序的名称和图标
     */
    @Override
    public void updateHeaderView(CardHeader header) {
        requireActivity().runOnUiThread(() -> {
            mRootView.setVisibility(View.VISIBLE);
            mCardTitle.setText(header.getCardTitle());
            mCardIcon.setImageDrawable(header.getCardIcon());
        });
    }

    @Override
    public final void updateContentView(CardContent content) {
        requireActivity().runOnUiThread(() -> {
            hideAllViews();
            updateContentViewInternal(content);
        });
    }

    /**
     * 子类可以重写此方法以更新其特定类型的卡片内容
     */
    protected void updateContentViewInternal(CardContent content) {
        switch (content.getType()) {
            case DESCRIPTIVE_TEXT:
                DescriptiveTextView descriptiveTextContent = (DescriptiveTextView) content;
                updateDescriptiveTextOnlyView(descriptiveTextContent.getTitle(),
                        descriptiveTextContent.getSubtitle(), descriptiveTextContent.getImage(),
                        descriptiveTextContent.getFooter());
                break;
            case DESCRIPTIVE_TEXT_WITH_CONTROLS:
                DescriptiveTextWithControlsView
                        descriptiveTextWithControlsContent =
                        (DescriptiveTextWithControlsView) content;
                updateDescriptiveTextWithControlsView(descriptiveTextWithControlsContent.getTitle(),
                        descriptiveTextWithControlsContent.getSubtitle(),
                        descriptiveTextWithControlsContent.getImage(),
                        descriptiveTextWithControlsContent.getLeftControl(),
                        descriptiveTextWithControlsContent.getCenterControl(),
                        descriptiveTextWithControlsContent.getRightControl());
                break;
            case TEXT_BLOCK:
                TextBlockView textBlockContent = (TextBlockView) content;
                updateTextBlock(textBlockContent.getText(), textBlockContent.getFooter());
                break;
        }
    }

    protected final void updateDescriptiveTextOnlyView(CharSequence primaryText,
            CharSequence secondaryText, Drawable optionalImage, CharSequence tapForMoreText) {
        getDescriptiveTextOnlyLayoutView().setVisibility(View.VISIBLE);
        mDescriptiveTextOnlyTitle.setText(primaryText);
        mDescriptiveTextOnlySubtitle.setText(secondaryText);
        mDescriptiveTextOnlyOptionalImage.setImageDrawable(optionalImage);
        mDescriptiveTextOnlyOptionalImage.setVisibility(
                optionalImage == null ? View.GONE : View.VISIBLE);
        mDescriptiveTextOnlyTapForMore.setText(tapForMoreText);
        mDescriptiveTextOnlyTapForMore.setVisibility(
                tapForMoreText == null ? View.GONE : View.VISIBLE);
    }

    protected final void updateDescriptiveTextWithControlsView(CharSequence primaryText,
            CharSequence secondaryText, Drawable optionalImage,
            DescriptiveTextWithControlsView.Control leftButton,
            DescriptiveTextWithControlsView.Control centerButton,
            DescriptiveTextWithControlsView.Control rightButton) {
        getDescriptiveTextWithControlsLayoutView().setVisibility(View.VISIBLE);
        mDescriptiveTextWithControlsTitle.setText(primaryText);
        mDescriptiveTextWithControlsSubtitle.setText(secondaryText);
        mDescriptiveTextWithControlsOptionalImage.setImageDrawable(optionalImage);
        mDescriptiveTextWithControlsOptionalImage.setVisibility(
                optionalImage == null ? View.GONE : View.VISIBLE);

        updateControlBarButton(leftButton, mControlBarLeftButton);
        updateControlBarButton(centerButton, mControlBarCenterButton);
        updateControlBarButton(rightButton, mControlBarRightButton);
    }

    private void updateControlBarButton(DescriptiveTextWithControlsView.Control buttonContent,
            ImageButton buttonView) {
        if (buttonContent != null) {
            buttonView.setImageDrawable(buttonContent.getIcon());
            buttonView.setOnClickListener(buttonContent.getOnClickListener());
            buttonView.setVisibility(View.VISIBLE);
        } else {
            buttonView.setVisibility(View.GONE);
        }
    }

    protected final void updateTextBlock(CharSequence mainText, CharSequence tapForMoreText) {
        getTextBlockLayoutView().setVisibility(View.VISIBLE);
        mTextBlock.setText(mainText);
        mTextBlockTapForMore.setText(tapForMoreText);
        mTextBlockTapForMore.setVisibility(tapForMoreText == null ? View.GONE : View.VISIBLE);
    }

    protected void hideAllViews() {
        getTextBlockLayoutView().setVisibility(View.GONE);
        getDescriptiveTextOnlyLayoutView().setVisibility(View.GONE);
        getDescriptiveTextWithControlsLayoutView().setVisibility(View.GONE);
    }

    protected final View getRootView() {
        return mRootView;
    }

    protected final View getCardBackground() {
        if (mCardBackground == null) {
            mCardBackground = getRootView().findViewById(R.id.card_background);
        }
        return mCardBackground;
    }

    protected final CrossfadeImageView getCardBackgroundImage() {
        if (mCardBackgroundImage == null) {
            mCardBackgroundImage = getCardBackground().findViewById(R.id.card_background_image);
        }
        return mCardBackgroundImage;
    }

    protected final View getDescriptiveTextOnlyLayoutView() {
        if (mDescriptiveTextOnlyLayoutView == null) {
            ViewStub stub = mRootView.findViewById(R.id.descriptive_text_layout);
            mDescriptiveTextOnlyLayoutView = stub.inflate();
            mDescriptiveTextOnlyTitle = mDescriptiveTextOnlyLayoutView.findViewById(
                    R.id.primary_text);
            mDescriptiveTextOnlySubtitle = mDescriptiveTextOnlyLayoutView.findViewById(
                    R.id.secondary_text);
            mDescriptiveTextOnlyOptionalImage = mDescriptiveTextOnlyLayoutView.findViewById(
                    R.id.optional_image);
            mDescriptiveTextOnlyTapForMore = mDescriptiveTextOnlyLayoutView.findViewById(
                    R.id.tap_for_more_text);
        }
        return mDescriptiveTextOnlyLayoutView;
    }

    protected final View getDescriptiveTextWithControlsLayoutView() {
        if (mDescriptiveTextWithControlsLayoutView == null) {
            ViewStub stub = mRootView.findViewById(R.id.descriptive_text_with_controls_layout);
            mDescriptiveTextWithControlsLayoutView = stub.inflate();
            mDescriptiveTextWithControlsTitle = mDescriptiveTextWithControlsLayoutView.findViewById(
                    R.id.primary_text);
            mDescriptiveTextWithControlsSubtitle =
                    mDescriptiveTextWithControlsLayoutView.findViewById(R.id.secondary_text);
            mDescriptiveTextWithControlsOptionalImage =
                    mDescriptiveTextWithControlsLayoutView.findViewById(R.id.optional_image);
            mControlBarView = mDescriptiveTextWithControlsLayoutView.findViewById(R.id.button_trio);
            mControlBarLeftButton = mDescriptiveTextWithControlsLayoutView.findViewById(
                    R.id.button_left);
            mControlBarCenterButton = mDescriptiveTextWithControlsLayoutView.findViewById(
                    R.id.button_center);
            mControlBarRightButton = mDescriptiveTextWithControlsLayoutView.findViewById(
                    R.id.button_right);
        }
        return mDescriptiveTextWithControlsLayoutView;
    }

    private View getTextBlockLayoutView() {
        if (mTextBlockLayoutView == null) {
            ViewStub stub = mRootView.findViewById(R.id.text_block_layout);
            mTextBlockLayoutView = stub.inflate();
            mTextBlock = mTextBlockLayoutView.findViewById(R.id.text_block);
            mTextBlockTapForMore = mTextBlockLayoutView.findViewById(R.id.tap_for_more_text);
        }
        return mTextBlockLayoutView;
    }
}
