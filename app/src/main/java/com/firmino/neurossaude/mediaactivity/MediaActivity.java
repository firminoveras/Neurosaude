package com.firmino.neurossaude.mediaactivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firmino.neurossaude.alerts.MessageAlert;
import com.google.firebase.storage.FirebaseStorage;

public abstract class MediaActivity extends AppCompatActivity implements Runnable, MediaActivityImplements {

    protected String extrasTitle;
    protected String extraSubtitle;
    protected String extraDesciption;
    protected long extraLastPosition = 0;
    protected int extraLastProgress = 0;
    private int extrasMediaType;

    private float touchPoint;
    private long millisOnMedia;
    private long mStartTime = 0L;
    private final Handler mTimeHandler = new Handler();

    protected boolean isFinished = false;
    protected boolean isControlsVisibles = false;
    protected boolean isLoaded = false;

    public final static int MEDIA_TYPE_AUDIO = 1;
    public final static int MEDIA_TYPE_VIDEO = 2;
    public final static int MEDIA_TYPE_TEXT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getExtras().getString("url");
        if (url.endsWith(".mp3")) extrasMediaType = MEDIA_TYPE_AUDIO;
        if (url.endsWith(".mp4")) extrasMediaType = MEDIA_TYPE_VIDEO;
        if (url.endsWith(".md")) extrasMediaType = MEDIA_TYPE_TEXT;
        extraLastProgress = getIntent().getExtras().getInt("progress");
        extraLastPosition = getIntent().getExtras().getInt("value");

        extrasTitle = getIntent().getExtras().getString("maintitle");
        extraSubtitle = getIntent().getExtras().getString("title");
        extraDesciption = getIntent().getExtras().getString("text");

        loadMedia(url);

        if (mStartTime == 0L) {
            mStartTime = SystemClock.uptimeMillis();
            mTimeHandler.removeCallbacks(this);
            mTimeHandler.postDelayed(this, 100);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchPoint = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float deltaY = event.getY() - touchPoint;
                if (Math.abs(deltaY) > 150) setControlsVisible(deltaY < 0);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        mTimeHandler.removeCallbacks(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mTimeHandler.postDelayed(this, 100);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        onFinishMediaActivity();
        setControlsVisible(false);
        Intent intent = new Intent();
        intent.putExtra("progress", Math.max(extraLastProgress, getProgress()));
        intent.putExtra("value",  getPosition());
        intent.putExtra("week", getIntent().getExtras().getInt("week", -1));
        intent.putExtra("mediaType", extrasMediaType);
        intent.putExtra("audioIndex", getIntent().getExtras().getInt("audioIndex", -1));
        intent.putExtra("millisOnMedia", millisOnMedia);
        setResult(extrasMediaType, intent);
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setControlsVisible(false);
        onScreenOrientationChange(newConfig);
    }

    @Override
    public void run() {
        final long start = mStartTime;
        millisOnMedia = SystemClock.uptimeMillis() - start;
        mTimeHandler.postDelayed(this, 1000);
    }

    private void loadMedia(String uri) {
        FirebaseStorage.getInstance().getReferenceFromUrl(uri).getDownloadUrl().addOnCompleteListener(task -> {
            try {
                Uri downloadLink = Uri.parse(task.getResult().toString());
                AsyncTask.execute(() -> loadMediaComplete(downloadLink));
            } catch (Exception ex) {
                MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Ocorreu um erro no carregamento da mídia. Tente novamente mais tarde.");
                new Handler().postDelayed(this::onBackPressed, 2000);
                ex.printStackTrace();
            }
        });
    }

    private void setControlsVisible(boolean visible) {
        if (isControlsVisibles != visible && !isFinished && isLoaded) {
            isControlsVisibles = visible;
            setControlsVisibleComplete(visible);
        }
    }

}