package com.firmino.neurossaude.views;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.firmino.neurossaude.PlayActivity;
import com.firmino.neurossaude.R;
import com.firmino.neurossaude.user.User;

import java.util.Locale;

@SuppressLint("ViewConstructor")
public class WeekView extends FrameLayout {

    private final Context mContext;

    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mPercent;
    private ConstraintLayout mButtonsLayout;
    private WeekViewCoinButton mCoinText;
    private WeekViewCoinButton mCoinVideo;
    private WeekViewCoinButton mCoinAudio1;
    private WeekViewCoinButton mCoinAudio2;
    private WeekViewCoinButton mCoinAudio3;
    private WeekViewCoinButton mCoinAudio4;
    private WeekViewCoinButton mCoinAudio5;
    private WeekViewCoinButton mCoinAudio6;

    private final int week;
    private boolean isButtonsVisibles = false;
    private boolean isEnabledMode = false;
    private OnButtonsVisibleChangeListener onButtonsVisibleChangeListener;
    private final String videoTitle;
    private final String videoText;
    private final String audioTitle;
    private final String audioText;
    private final String textTitle;
    private final String textText;
    private OnCoinClickedListener buttonsVisibleChangeListener = intent -> {

    };

    public WeekView(@NonNull Context context, int week, String title, String videoTitle, String videoText, String audioTitle, String audioText, String textTitle, String textText) {
        super(context);
        mContext = context;
        init();
        setEnabledMode(false);
        this.week = week;
        mTitle.setText(String.format(Locale.getDefault(), "Semana %d", week));
        mSubtitle.setText(title);
        this.videoTitle = videoTitle;
        this.videoText = videoText;
        this.audioTitle = audioTitle;
        this.audioText = audioText;
        this.textTitle = textTitle;
        this.textText = textText;
        update();
    }

    public void update() {
        setEnabledMode(false);
        User.getWeekValues(week, (videoProgress, videoValue, textProgress, textValue, audioProgress, audioValue) -> {
            mCoinVideo.setValues(videoProgress, videoValue);
            mCoinText.setValues(textProgress, textValue);
            mCoinAudio1.setValues(audioProgress.get(0), audioValue.get(0));
            mCoinAudio2.setValues(audioProgress.get(1), audioValue.get(1));
            mCoinAudio3.setValues(audioProgress.get(2), audioValue.get(2));
            mCoinAudio4.setValues(audioProgress.get(3), audioValue.get(3));
            mCoinAudio5.setValues(audioProgress.get(4), audioValue.get(4));
            mCoinAudio6.setValues(audioProgress.get(5), audioValue.get(5));
            mPercent.setText(String.format(Locale.getDefault(), "%d%%", getProgress()));
            setEnabledMode(true);
        });
    }

    public void setEnabledMode(boolean enabled) {
        isEnabledMode = enabled;
        findViewById(R.id.mWeekLockIcon).setVisibility(enabled ? GONE : VISIBLE);
        mCoinText.setEnabled(enabled);
        mCoinVideo.setEnabled(enabled);
        mCoinAudio1.setEnabled(enabled);
        mCoinAudio2.setEnabled(enabled);
        mCoinAudio3.setEnabled(enabled);
        mCoinAudio4.setEnabled(enabled);
        mCoinAudio5.setEnabled(enabled);
        mCoinAudio6.setEnabled(enabled);
    }

    public void setButtonsVisible(boolean isVisible) {
        if (isButtonsVisibles != isVisible && isEnabledMode) {
            if (onButtonsVisibleChangeListener != null)
                onButtonsVisibleChangeListener.onButtonsVisibleChangeListener(this);
            isButtonsVisibles = isVisible;
            ValueAnimator anim = ValueAnimator.ofInt(mButtonsLayout.getHeight(), isVisible ? (int) mContext.getResources().getDimension(R.dimen.weekview_buttons_height) : 0);
            anim.addUpdateListener(valueAnimator -> {
                ViewGroup.LayoutParams lay = mButtonsLayout.getLayoutParams();
                lay.height = (int) valueAnimator.getAnimatedValue();
                mButtonsLayout.setLayoutParams(lay);
                System.out.println(lay.height);
            });
            anim.setDuration(300);
            anim.start();
        }
    }

    private void init() {
        inflate(mContext, R.layout.weekview_layout, this);
        mTitle = findViewById(R.id.mWeekTitle);
        mSubtitle = findViewById(R.id.mWeekSubTitle);
        mCoinText = findViewById(R.id.mWeekButtonText);
        mCoinVideo = findViewById(R.id.mWeekButtonVideo);
        mCoinAudio1 = findViewById(R.id.mWeekButtonAudio1);
        mCoinAudio2 = findViewById(R.id.mWeekButtonAudio2);
        mCoinAudio3 = findViewById(R.id.mWeekButtonAudio3);
        mCoinAudio4 = findViewById(R.id.mWeekButtonAudio4);
        mCoinAudio5 = findViewById(R.id.mWeekButtonAudio5);
        mCoinAudio6 = findViewById(R.id.mWeekButtonAudio6);
        mPercent = findViewById(R.id.mWeekPercent);
        mButtonsLayout = findViewById(R.id.mWeekButtonsLayout);

        mTitle.setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));

        mCoinVideo.setOnCoinClickListener(this::coinVideoClicked);
        mCoinText.setOnCoinClickListener(this::coinTextClicked);
        mCoinAudio1.setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 1));
        mCoinAudio2.setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 2));
        mCoinAudio3.setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 3));
        mCoinAudio4.setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 4));
        mCoinAudio5.setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 5));
        mCoinAudio6.setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 6));
    }

    private void coinVideoClicked(int progress, int value) {
        Intent intent = new Intent(mContext, PlayActivity.class);
        intent.putExtra("maintitle", "Semana " + week);
        intent.putExtra("value", value);
        intent.putExtra("progress", progress);
        intent.putExtra("title", videoTitle);
        intent.putExtra("week", week);
        intent.putExtra("text", videoText);
        intent.putExtra("url", "gs://neurosaude-firmino.appspot.com/video/video_week" + week + ".mp4");
        buttonsVisibleChangeListener.onCoinClickedListener(intent);
    }

    private void coinTextClicked(int progress, int value) {
        Intent intent = new Intent(mContext, PlayActivity.class);
        intent.putExtra("maintitle", "Semana " + week);
        intent.putExtra("value", value);
        intent.putExtra("progress", progress);
        intent.putExtra("title", textTitle);
        intent.putExtra("week", week);
        intent.putExtra("text", textText);
        intent.putExtra("url", "gs://neurosaude-firmino.appspot.com/text/text_week" + week + ".pdf");

        buttonsVisibleChangeListener.onCoinClickedListener(intent);
    }

    private void coinAudioClicked(int progress, int value, int audioIndex) {
        Intent intent = new Intent(mContext, PlayActivity.class);
        intent.putExtra("maintitle", "Semana " + week);
        intent.putExtra("value", value);
        intent.putExtra("progress", progress);
        intent.putExtra("title", audioTitle);
        intent.putExtra("text", audioText);
        intent.putExtra("week", week);
        intent.putExtra("audioIndex", audioIndex);
        intent.putExtra("url", "gs://neurosaude-firmino.appspot.com/audio/audio_week" + week + ".mp3");

        buttonsVisibleChangeListener.onCoinClickedListener(intent);
    }

    public int getProgress() {
        int progress = 0;
        progress += mCoinVideo.getProgress();
        progress += mCoinText.getProgress();
        progress += mCoinAudio1.getProgress();
        progress += mCoinAudio2.getProgress();
        progress += mCoinAudio3.getProgress();
        progress += mCoinAudio4.getProgress();
        progress += mCoinAudio5.getProgress();
        progress += mCoinAudio6.getProgress();
        return progress / 8;
    }

    public void setOnButtonsVisibleChangeListener(OnButtonsVisibleChangeListener onButtonsVisibleChangeListener) {
        this.onButtonsVisibleChangeListener = onButtonsVisibleChangeListener;
    }

    public void setOnCoinClicked(OnCoinClickedListener listener) {
        this.buttonsVisibleChangeListener = listener;
    }

    public interface OnButtonsVisibleChangeListener {
        void onButtonsVisibleChangeListener(WeekView view);
    }

    public interface OnCoinClickedListener {
        void onCoinClickedListener(Intent intent);
    }
}
