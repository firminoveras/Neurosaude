package com.firmino.neurossaude.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firmino.neurossaude.R;

import java.util.ArrayList;
import java.util.List;

public class MediaControl extends FrameLayout {

    private final Context mContext;
    private final List<TextView> mMediaControls = new ArrayList<>();
    private ProgressBar mControlProgress;
    private OnClickMediaControlButton onClickMediaControlButton;
    private boolean isControlsVisibles = false;

    public MediaControl(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MediaControl, 0, 0);
        mMediaControls.get(0).setText(ta.getString(R.styleable.MediaControl_control1_text));
        mMediaControls.get(1).setText(ta.getString(R.styleable.MediaControl_control2_text));
        mMediaControls.get(2).setText(ta.getString(R.styleable.MediaControl_control3_text));
        mMediaControls.get(3).setText(ta.getString(R.styleable.MediaControl_control4_text));
        mMediaControls.get(4).setText(ta.getString(R.styleable.MediaControl_control5_text));
        mMediaControls.get(5).setText(ta.getString(R.styleable.MediaControl_control6_text));
        mMediaControls.get(6).setText(ta.getString(R.styleable.MediaControl_control7_text));
        mMediaControls.get(7).setText(ta.getString(R.styleable.MediaControl_control8_text));
        mMediaControls.get(8).setText(ta.getString(R.styleable.MediaControl_control9_text));
        mMediaControls.get(0).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control1_drawable, 0), 0, 0);
        mMediaControls.get(1).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control2_drawable, 0), 0, 0);
        mMediaControls.get(2).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control3_drawable, 0), 0, 0);
        mMediaControls.get(3).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control4_drawable, 0), 0, 0);
        mMediaControls.get(4).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control5_drawable, 0), 0, 0);
        mMediaControls.get(5).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control6_drawable, 0), 0, 0);
        mMediaControls.get(6).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control7_drawable, 0), 0, 0);
        mMediaControls.get(7).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control8_drawable, 0), 0, 0);
        mMediaControls.get(8).setCompoundDrawablesWithIntrinsicBounds(0, ta.getResourceId(R.styleable.MediaControl_control9_drawable, 0), 0, 0);
        ta.recycle();
    }

    private void init() {
        inflate(mContext, R.layout.layout_media_control, this);

        onClickMediaControlButton = new OnClickMediaControlButton() {
            @Override
            public void onMediaControl1Clicked(TextView view) {

            }

            @Override
            public void onMediaControl2Clicked(TextView view) {

            }

            @Override
            public void onMediaControl3Clicked(TextView view) {

            }

            @Override
            public void onMediaControl4Clicked(TextView view) {

            }

            @Override
            public void onMediaControl5Clicked(TextView view) {

            }

            @Override
            public void onMediaControl6Clicked(TextView view) {

            }

            @Override
            public void onMediaControl7Clicked(TextView view) {

            }

            @Override
            public void onMediaControl8Clicked(TextView view) {

            }

            @Override
            public void onMediaControl9Clicked(TextView view) {

            }
        };

        mMediaControls.add(findViewById(R.id.MediaControl_Control1));
        mMediaControls.add(findViewById(R.id.MediaControl_Control2));
        mMediaControls.add(findViewById(R.id.MediaControl_Control3));
        mMediaControls.add(findViewById(R.id.MediaControl_Control4));
        mMediaControls.add(findViewById(R.id.MediaControl_Control5));
        mMediaControls.add(findViewById(R.id.MediaControl_Control6));
        mMediaControls.add(findViewById(R.id.MediaControl_Control7));
        mMediaControls.add(findViewById(R.id.MediaControl_Control8));
        mMediaControls.add(findViewById(R.id.MediaControl_Control9));
        mControlProgress = findViewById(R.id.MediaControl_ProgressBar);

        mMediaControls.get(0).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl1Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(1).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl2Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(2).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl3Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(3).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl4Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(4).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl5Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(5).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl6Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(6).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl7Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(7).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl8Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });
        mMediaControls.get(8).setOnClickListener(v -> {
            onClickMediaControlButton.onMediaControl9Clicked((TextView) v);
            v.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_alpha));
        });


    }

    public void setControlsVisible(boolean visible) {
        if (isControlsVisibles != visible) {
            isControlsVisibles = visible;
            float heightTop = getResources().getDimension(R.dimen.audio_controllayouttop);
            float heightMiddle = getResources().getDimension(R.dimen.audio_controllayoutmiddle);
            float heightBottom = getResources().getDimension(R.dimen.audio_controllayoutbottom);

            ValueAnimator anim = ValueAnimator.ofInt(visible ? 100 : 0, visible ? 0 : 100);
            anim.addUpdateListener(valueAnimator -> {
                findViewById(R.id.MediaControl_ControlLayoutTop).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightTop) / 100);
                findViewById(R.id.MediaControl_ControlLayoutMiddle).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightMiddle) / 100);
                findViewById(R.id.MediaControl_ControlLayoutBottom).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightBottom) / 100);
                findViewById(R.id.MediaControl_DetailsLayout).setAlpha(1f - (Integer.valueOf((int) valueAnimator.getAnimatedValue()).floatValue() / 100));
                findViewById(R.id.MediaControl_ProgressBar).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightTop) / 100);
                findViewById(R.id.MediaControl_Label).setAlpha((Integer.valueOf((int) valueAnimator.getAnimatedValue()).floatValue() / 3) / 100);
            });
            anim.setDuration(300);
            anim.start();
        }
    }

    public void setOnClickMediaControlButton(OnClickMediaControlButton onClickMediaControlButton) {
        this.onClickMediaControlButton = onClickMediaControlButton;
    }

    public void setProgress(int progress) {
        if (progress > 1000) progress = 1000;
        if (progress < 0) progress = 0;
        mControlProgress.setProgress(progress);
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (this.getChildCount() == 0)
            super.addView(child, index, params);
        else
            ((LinearLayout) findViewById(R.id.MediaControl_DetailsLayout)).addView(child, params);
    }

    public TextView getButton(int index) {
        return mMediaControls.get(index);
    }

    public interface OnClickMediaControlButton {
        void onMediaControl1Clicked(TextView view);

        void onMediaControl2Clicked(TextView view);

        void onMediaControl3Clicked(TextView view);

        void onMediaControl4Clicked(TextView view);

        void onMediaControl5Clicked(TextView view);

        void onMediaControl6Clicked(TextView view);

        void onMediaControl7Clicked(TextView view);

        void onMediaControl8Clicked(TextView view);

        void onMediaControl9Clicked(TextView view);
    }

}
