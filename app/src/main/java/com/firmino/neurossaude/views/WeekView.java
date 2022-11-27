package com.firmino.neurossaude.views;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.firmino.neurossaude.PlayActivity;
import com.firmino.neurossaude.R;
import com.firmino.neurossaude.alerts.MessageAlert;
import com.firmino.neurossaude.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public class WeekView extends FrameLayout {
    private TextView mTitle;
    private TextView mSubtitle;
    private ProgressBar mPercent;
    private ConstraintLayout mButtonsLayout;
    private List<WeekViewCoinButton> mCoins;

    private final Context mContext;
    private final int week;
    private final String videoTitle;
    private final String videoText;
    private final String audioTitle;
    private final String audioText;
    private final String textTitle;
    private final String textText;
    private OnCoinClickedListener buttonsVisibleChangeListener = intent -> {

    };
    private boolean isUnlocked = false;
    private boolean isButtonsVisibles = false;
    private boolean isEnabledMode = false;

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

    public void unlock() {
        isUnlocked = true;
        setEnabledMode(true);
    }

    public void update() {
        setEnabledMode(false);
        User.getWeekValues(week, (videoProgress, videoValue, textProgress, textValue, audioProgress, audioValue) -> {
            ((WeekViewCoinButton) findViewById(R.id.mWeekButtonText)).setValues(textProgress, textValue);
            mCoins.get(0).setValues(videoProgress, videoValue);
            mCoins.get(1).setValues(audioProgress.get(0), audioValue.get(0));
            mCoins.get(2).setValues(audioProgress.get(1), audioValue.get(1));
            mCoins.get(3).setValues(audioProgress.get(2), audioValue.get(2));
            mCoins.get(4).setValues(audioProgress.get(3), audioValue.get(3));
            mCoins.get(5).setValues(audioProgress.get(4), audioValue.get(4));
            mCoins.get(6).setValues(audioProgress.get(5), audioValue.get(5));
            ((WeekViewCoinButton) findViewById(R.id.mWeekButtonText)).setUnlocked(true);
            ((WeekViewCoinButton) findViewById(R.id.mWeekButtonVideo)).setUnlocked(true);
            mPercent.setProgress(getProgress());
            setEnabledMode(true);
        });
    }

    public void setEnabledMode(boolean enabled) {
        isEnabledMode = enabled;
        findViewById(R.id.mWeekLockIcon).setVisibility(isUnlocked ? GONE : VISIBLE);
        for (WeekViewCoinButton coin : mCoins) coin.setEnabled(enabled);
    }

    public void setButtonsVisible(boolean isVisible) {
        if (isButtonsVisibles != isVisible && isEnabledMode && isUnlocked) {
            isButtonsVisibles = isVisible;
            ValueAnimator anim = ValueAnimator.ofInt(mButtonsLayout.getHeight(), isVisible ? (int) mContext.getResources().getDimension(R.dimen.weekview_buttons_height) : 0);
            anim.addUpdateListener(valueAnimator -> {
                ViewGroup.LayoutParams lay = mButtonsLayout.getLayoutParams();
                lay.height = (int) valueAnimator.getAnimatedValue();
                mButtonsLayout.setLayoutParams(lay);
            });
            anim.setDuration(300);
            anim.start();
            mPercent.setVisibility(isVisible ? VISIBLE : GONE);
        } else {
            MessageAlert.create(mContext, MessageAlert.TYPE_ALERT, "Semana bloqueada! Continue o curso para desbloqueà-la.");
        }
    }

    private void init() {
        inflate(mContext, R.layout.weekview_layout, this);
        mTitle = findViewById(R.id.mWeekTitle);
        mSubtitle = findViewById(R.id.mWeekSubTitle);

        mPercent = findViewById(R.id.mWeekPercent);
        mButtonsLayout = findViewById(R.id.mWeekButtonsLayout);

        mCoins = new ArrayList<>();
        mCoins.add(findViewById(R.id.mWeekButtonVideo));
        mCoins.add(findViewById(R.id.mWeekButtonAudio1));
        mCoins.add(findViewById(R.id.mWeekButtonAudio2));
        mCoins.add(findViewById(R.id.mWeekButtonAudio3));
        mCoins.add(findViewById(R.id.mWeekButtonAudio4));
        mCoins.add(findViewById(R.id.mWeekButtonAudio5));
        mCoins.add(findViewById(R.id.mWeekButtonAudio6));

        mTitle.setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));

        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonVideo)).setOnCoinClickListener(this::coinVideoClicked);
        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonText)).setOnCoinClickListener(this::coinTextClicked);
        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonAudio1)).setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 1));
        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonAudio2)).setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 2));
        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonAudio3)).setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 3));
        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonAudio4)).setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 4));
        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonAudio5)).setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 5));
        ((WeekViewCoinButton) findViewById(R.id.mWeekButtonAudio6)).setOnCoinClickListener((progress, value) -> coinAudioClicked(progress, value, 6));
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

    public WeekViewCoinButton getLastUnlockedCoin() {
        if (isUnlocked) {
            for (WeekViewCoinButton coin : mCoins) {
                if (coin.isUnlocked()) return coin;
            }
        }
        return null;
    }
    public WeekViewCoinButton getFirstLockedCoin() {
        if (isUnlocked) {
            for (WeekViewCoinButton coin : mCoins) {
                if (!coin.isUnlocked()) return coin;
            }
        }
        return null;
    }

    public boolean isEnabledMode() {
        return isEnabledMode;
    }

    public int getProgress() {
        int progress = 0;
        for (WeekViewCoinButton coin : mCoins) progress += coin.getProgress();
        return progress / mCoins.size();
    }

    public void setOnCoinClicked(OnCoinClickedListener listener) {
        this.buttonsVisibleChangeListener = listener;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public interface OnCoinClickedListener {
        void onCoinClickedListener(Intent intent);
    }

}
