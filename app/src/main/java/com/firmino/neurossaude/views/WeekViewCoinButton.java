package com.firmino.neurossaude.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firmino.neurossaude.R;

public class WeekViewCoinButton extends FrameLayout {
    private final Context mContext;
    private ImageView mImage;
    private ProgressBar mProgress;
    private int type;
    private int progress;
    private int value;


    private OnCoinClickListener listener = (progress, value) -> {
    };

    public final static int COIN_TYPE_AUDIO = 0;
    public final static int COIN_TYPE_VIDEO = 1;
    public final static int COIN_TYPE_TEXT = 2;

    public WeekViewCoinButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeekViewCoinButton, 0, 0);
        setType(ta.getInt(R.styleable.WeekViewCoinButton_coin_type, -1));
        ta.recycle();
    }

    private void init() {
        inflate(mContext, R.layout.weekview_coin_layout, this);
        mImage = findViewById(R.id.Coin_Button);
        mProgress = findViewById(R.id.Coin_Progress);
        setOnClickListener(view -> listener.onCoinClickListener(progress,value));
    }

    public void setType(int coinType) {
        type = coinType;
        updateCoinDrawable();
    }

    private void updateCoinDrawable() {
        mProgress.setVisibility(progress == 100 ? GONE : VISIBLE);
        switch (type) {
            case COIN_TYPE_AUDIO:
                mImage.setImageResource(progress == 100 ? R.drawable.bg_audio_complete : R.drawable.bg_audio_incomplete);
                break;
            case COIN_TYPE_TEXT:
                mImage.setImageResource(progress == 100 ? R.drawable.bg_text_complete : R.drawable.bg_text_incomplete);
                break;
            case COIN_TYPE_VIDEO:
                mImage.setImageResource(progress == 100 ? R.drawable.bg_video_complete : R.drawable.bg_video_incomplete);
                break;
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        mProgress.setProgress(progress);
        updateCoinDrawable();
    }

    public int getProgress() {
        return this.progress;
    }

    public void setValues(int progress, int value) {
        this.value = value;
        setProgress(progress);
    }

    public void setOnCoinClickListener(OnCoinClickListener listener) {
        this.listener = listener;
    }


    public interface OnCoinClickListener {
        void onCoinClickListener(int progress, int value);
    }
}
