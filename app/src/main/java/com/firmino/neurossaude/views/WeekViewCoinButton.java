package com.firmino.neurossaude.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firmino.neurossaude.R;
import com.firmino.neurossaude.alerts.MessageAlert;

public class WeekViewCoinButton extends FrameLayout {
    private final Context mContext;
    private ImageView mImage;
    private ProgressBar mProgress;
    private int type;
    private int progress;
    private int value;
    public final static int COIN_TYPE_AUDIO = 0;
    public final static int COIN_TYPE_VIDEO = 1;
    public final static int COIN_TYPE_TEXT = 2;
    private boolean isUnlocked = false;
    private OnCoinClickListener listenerClick = (progress, value) -> {
    };
    private OnCoinCompleteListener listenerComplete = () -> {
    };


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
        setUnlocked(false);
        setOnClickListener(view -> {
            if (isUnlocked) listenerClick.onCoinClickListener(progress, value);
            else MessageAlert.create(mContext, MessageAlert.TYPE_ALERT,"Ação bloqueada, aguarde até o dia correto para abrir essa mídia.");
        });
    }

    public void setType(int coinType) {
        type = coinType;
        updateCoinDrawable();
    }

    public int getType() {
        return type;
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
        if(progress==100) {
            listenerComplete.onCoinCompleteListener();
            setUnlocked(true);
        }
    }

    public int getProgress() {
        return this.progress;
    }

    public void setValues(int progress, int value) {
        this.value = value;
        setProgress(progress);
    }

    public void setOnCoinClickListener(OnCoinClickListener listener) {
        this.listenerClick = listener;
    }

    public void setListenerComplete(OnCoinCompleteListener listenerComplete) {
        this.listenerComplete = listenerComplete;
    }

    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
        setAlpha(unlocked ? 1f : 0.3f);
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }


    public interface OnCoinClickListener {
        void onCoinClickListener(int progress, int value);
    }

    public interface OnCoinCompleteListener {
        void onCoinCompleteListener();
    }
}
