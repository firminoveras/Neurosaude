package com.firmino.neurossaude.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firmino.neurossaude.R;
import com.firmino.neurossaude.PlayActivity;
import com.firmino.neurossaude.user.User;

import java.util.Locale;

public class WeekView extends FrameLayout {
    private final Context mContext;

    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mPercentVideo;
    private TextView mPercentText;
    private TextView mPercentAudio;
    private Button mButtonVideo;
    private Button mButtonText;
    private Button mButtonAudio;
    private ProgressBar mProgress;
    private FrameLayout mButtonsLayout;

    private int progressVideo = 0;
    private int progressText = 0;
    private int progressAudio = 0;

    private final int week;
    private boolean isButtonsVisibles = false;

    private OnButtonsVisibleChangeListener onButtonsVisibleChangeListener;
    private int audioValue = 0;
    private int videoValue = 0;
    private int textValue = 0;

    public WeekView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        week = ta.getInt(R.styleable.WeekView_week, 0);
        mTitle.setText(String.format(Locale.getDefault(), "Semana %d", week));
        mSubtitle.setText(ta.getString(R.styleable.WeekView_subtitle));
        ta.recycle();
        update();
    }

    public void update() {
        setEnabledMode(false);
        User.getValues(week, (videoLastMillis, textLastPage, audioLastMillis) -> {
            audioValue = audioLastMillis;
            videoValue = videoLastMillis;
            textValue = textLastPage;

            User.getProgress(week, (videoProgress, textProgress, audioProgress) -> {
                progressVideo = videoProgress;
                progressText = textProgress;
                progressAudio = audioProgress;

                mPercentVideo.setText(String.format(Locale.getDefault(), "%d%%", progressVideo));
                mPercentText.setText(String.format(Locale.getDefault(), "%d%%", progressText));
                mPercentAudio.setText(String.format(Locale.getDefault(), "%d%%", progressAudio));

                if (progressVideo >= 100)
                    mPercentVideo.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, 50, 200, 50)));
                if (progressText >= 100)
                    mPercentText.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, 50, 200, 50)));
                if (progressAudio >= 100)
                    mPercentAudio.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, 50, 200, 50)));

                setEnabledMode(true);
                mProgress.setProgress(getProgress());
            });
        });

    }


    public void setEnabledMode(boolean enabled) {
        mProgress.setIndeterminate(!enabled);
        mButtonVideo.setEnabled(enabled);
        mButtonAudio.setEnabled(enabled);
        mButtonText.setEnabled(enabled);
    }

    public void setButtonsVisible(boolean isVisible) {
        if (isButtonsVisibles != isVisible) {
            if (onButtonsVisibleChangeListener != null)
                onButtonsVisibleChangeListener.onButtonsVisibleChangeListener(this);
            isButtonsVisibles = isVisible;
            ValueAnimator anim = ValueAnimator.ofInt(mButtonsLayout.getHeight(), isVisible ? (int) mContext.getResources().getDimension(R.dimen.weekview_buttons_height) : 0);
            anim.addUpdateListener(valueAnimator -> {
                ViewGroup.LayoutParams lay = mButtonsLayout.getLayoutParams();
                lay.height = (int) valueAnimator.getAnimatedValue();
                mButtonsLayout.setLayoutParams(lay);
            });
            anim.setDuration(300);
            anim.start();

        }
    }


    private void init() {
        inflate(mContext, R.layout.weekview_layout, this);
        mTitle = findViewById(R.id.mWeekTitle);
        mSubtitle = findViewById(R.id.mWeekSubTitle);
        mPercentVideo = findViewById(R.id.mWeekPercentVideo);
        mPercentText = findViewById(R.id.mWeekPercentText);
        mPercentAudio = findViewById(R.id.mWeekPercentAudio);
        mButtonVideo = findViewById(R.id.mWeekButtonVideo);
        mButtonText = findViewById(R.id.mWeekButtonText);
        mButtonAudio = findViewById(R.id.mWeekButtonAudio);
        mProgress = findViewById(R.id.mWeekProgress);
        mButtonsLayout = findViewById(R.id.mWeekButtonsLayout);

        mTitle.setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));
        mSubtitle.setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));
        setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));


        mButtonVideo.setOnClickListener(view -> {
            Intent audioActivityIntent = new Intent(mContext, PlayActivity.class);
            audioActivityIntent.putExtra("week",week);
            audioActivityIntent.putExtra("value",audioValue);
            audioActivityIntent.putExtra("mediaType", PlayActivity.MEDIA_TYPE_VIDEO);
            mContext.startActivity(audioActivityIntent);
        });

        mButtonAudio.setOnClickListener(view -> {
            Intent audioActivityIntent = new Intent(mContext, PlayActivity.class);
            audioActivityIntent.putExtra("week",week);
            audioActivityIntent.putExtra("value",audioValue);
            audioActivityIntent.putExtra("progress",progressAudio);
            audioActivityIntent.putExtra("mediaType", PlayActivity.MEDIA_TYPE_AUDIO);
            mContext.startActivity(audioActivityIntent);
        });


    }

    public int getProgress() {
        return (progressVideo + progressText + progressAudio);
    }

    public void setOnButtonsVisibleChangeListener(OnButtonsVisibleChangeListener onButtonsVisibleChangeListener) {
        this.onButtonsVisibleChangeListener = onButtonsVisibleChangeListener;
    }

    public interface OnButtonsVisibleChangeListener {
        void onButtonsVisibleChangeListener(WeekView view);
    }
}
