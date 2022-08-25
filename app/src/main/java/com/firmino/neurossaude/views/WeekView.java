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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firmino.neurossaude.MainActivity;
import com.firmino.neurossaude.PlayActivity;
import com.firmino.neurossaude.R;
import com.firmino.neurossaude.user.User;

import java.util.Locale;

public class WeekView extends FrameLayout {
    private final Context mContext;

    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mPercentVideo;
    private TextView mPercentText;
    private TextView mPercentAudio;
    private ProgressBar mProgress;
    private FrameLayout mButtonsLayout;

    private int videoProgress = 0;
    private int textProgress = 0;
    private int audioProgress = 0;

    private final int week;
    private boolean isButtonsVisibles = false;

    private OnButtonsVisibleChangeListener onButtonsVisibleChangeListener;
    private int audioValue = 0;
    private int videoValue = 0;
    private int textValue = 0;

    private String videoTitle;
    private String videoText;
    private String audioTitle;
    private String audioText;
    private String textTitle;
    private String textText;
    private boolean isEnabledMode = false;


    public WeekView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        setEnabledMode(false);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        week = ta.getInt(R.styleable.WeekView_week, 0);
        mTitle.setText(String.format(Locale.getDefault(), "Semana %d", week));
        ta.recycle();
        User.getWeekTexts(week, (title, videoTitle, videoText, audioTitle, audioText, textTitle, textText) -> {
            mSubtitle.setText(title);
            this.videoTitle = videoTitle;
            this.videoText = videoText;
            this.audioTitle = audioTitle;
            this.audioText = audioText;
            this.textTitle = textTitle;
            this.textText = textText;
            update();
        });
    }

    public void update() {
        User.getValues(week, (videoLastMillis, textLastPage, audioLastMillis) -> {
            audioValue = audioLastMillis;
            videoValue = videoLastMillis;
            textValue = textLastPage;
            User.getProgress(week, (videoProgress, textProgress, audioProgress) -> {
                this.videoProgress = videoProgress;
                this.textProgress = textProgress;
                this.audioProgress = audioProgress;

                mPercentVideo.setText(String.format(Locale.getDefault(), "%d%%", this.videoProgress));
                mPercentText.setText(String.format(Locale.getDefault(), "%d%%", this.textProgress));
                mPercentAudio.setText(String.format(Locale.getDefault(), "%d%%", this.audioProgress));

                if (this.videoProgress >= 100)
                    mPercentVideo.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, 50, 200, 50)));
                if (this.textProgress >= 100)
                    mPercentText.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, 50, 200, 50)));
                if (audioProgress >= 100)
                    mPercentAudio.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, 50, 200, 50)));

                setEnabledMode(true);
                mProgress.setProgress(getProgress());
            });
        });

    }

    public void setEnabledMode(boolean enabled) {
        isEnabledMode = enabled;
        ((ProgressBar) findViewById(R.id.mWeekProgress)).setIndeterminate(!enabled);
        findViewById(R.id.mWeekButtonVideo).setEnabled(enabled);
        findViewById(R.id.mWeekButtonText).setEnabled(enabled);
        findViewById(R.id.mWeekButtonAudio).setEnabled(enabled);
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
        Button mButtonVideo = findViewById(R.id.mWeekButtonVideo);
        Button mButtonText = findViewById(R.id.mWeekButtonText);
        Button mButtonAudio = findViewById(R.id.mWeekButtonAudio);
        mProgress = findViewById(R.id.mWeekProgress);
        mButtonsLayout = findViewById(R.id.mWeekButtonsLayout);

        mTitle.setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));
        mSubtitle.setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));
        setOnClickListener(view -> setButtonsVisible(!isButtonsVisibles));


        ActivityResultLauncher<Intent> someActivityResultLauncher = ((MainActivity) mContext).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    Intent data = result.getData();
                    if (data != null) {
                        int progress = data.getIntExtra("progress", -1);
                        long position = data.getLongExtra("position", -1);
                        switch (result.getResultCode()) {
                            case PlayActivity.MEDIA_TYPE_VIDEO:
                                User.setVideoProgressAndValue(week, progress, position, this::update);
                                break;
                            case PlayActivity.MEDIA_TYPE_TEXT:
                                User.setTextProgressAndValue(week, progress, position, this::update);
                                break;
                            case PlayActivity.MEDIA_TYPE_AUDIO:
                                User.setAudioProgressAndValue(week, progress, position, this::update);
                                break;
                        }


                    }
                });

        mButtonVideo.setOnClickListener(view -> {
            Intent videooActivityIntent = new Intent(mContext, PlayActivity.class);
            videooActivityIntent.putExtra("maintitle", "Semana " + week);
            videooActivityIntent.putExtra("value", videoValue);
            videooActivityIntent.putExtra("progress", videoProgress);
            videooActivityIntent.putExtra("title", videoTitle);
            videooActivityIntent.putExtra("text", videoText);
            videooActivityIntent.putExtra("url", "gs://neurosaude-firmino.appspot.com/video/video_week" + week + ".mp4");
            someActivityResultLauncher.launch(videooActivityIntent);
        });

        mButtonText.setOnClickListener(view -> {
            Intent textActivityIntent = new Intent(mContext, PlayActivity.class);
            textActivityIntent.putExtra("value", textValue);
            textActivityIntent.putExtra("progress", textProgress);
            textActivityIntent.putExtra("title", textTitle);
            textActivityIntent.putExtra("text", textText);
            textActivityIntent.putExtra("url", "gs://neurosaude-firmino.appspot.com/text/text_week" + week + ".pdf");
            textActivityIntent.putExtra("maintitle", "Semana " + week);
            someActivityResultLauncher.launch(textActivityIntent);
        });

        mButtonAudio.setOnClickListener(view -> {
            Intent audioActivityIntent = new Intent(mContext, PlayActivity.class);
            audioActivityIntent.putExtra("maintitle", "Semana " + week);
            audioActivityIntent.putExtra("value", audioValue);
            audioActivityIntent.putExtra("progress", audioProgress);
            audioActivityIntent.putExtra("title", audioTitle);
            audioActivityIntent.putExtra("text", audioText);
            audioActivityIntent.putExtra("url", "gs://neurosaude-firmino.appspot.com/audio/audio_week" + week + ".mp3");
            someActivityResultLauncher.launch(audioActivityIntent);
        });


    }

    public int getProgress() {
        return (videoProgress + textProgress + audioProgress);
    }

    public void setOnButtonsVisibleChangeListener(OnButtonsVisibleChangeListener onButtonsVisibleChangeListener) {
        this.onButtonsVisibleChangeListener = onButtonsVisibleChangeListener;
    }

    public interface OnButtonsVisibleChangeListener {
        void onButtonsVisibleChangeListener(WeekView view);
    }
}
