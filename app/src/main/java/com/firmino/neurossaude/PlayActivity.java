package com.firmino.neurossaude;

import android.animation.ValueAnimator;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.firmino.neurossaude.alerts.MessageAlert;
import com.firmino.neurossaude.user.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayActivity extends AppCompatActivity implements Runnable, View.OnClickListener {

    private MediaPlayer mMediaPlayer;
    private TextView mLoadingText;
    private ProgressBar mProgress;
    private VideoView mVideo;
    private ImageView mPlayButton;

    private Handler backgroundTask;

    private int progressStatus = 0;
    private int duration = 0;
    private int volume = 100;
    private int week;
    private int lastMillis = 0;
    private int lastProgress = 0;
    private float touchPoint;
    private boolean isReady = false;
    private boolean isFinished = false;
    private boolean isControlsVisibles = false;
    private boolean isVideo = false;

    public final static String MEDIA_TYPE_AUDIO = "mp3";
    public final static String MEDIA_TYPE_VIDEO = "mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);

        week = getIntent().getExtras().getInt("week");
        lastProgress = getIntent().getExtras().getInt("progress");
        lastMillis = getIntent().getExtras().getInt("value");
        isVideo = getIntent().getExtras().getString("mediaType").equals(MEDIA_TYPE_VIDEO);

        backgroundTask = new Handler();
        new Thread(this).start();

        mLoadingText = findViewById(R.id.Player_LoadingText);
        mProgress = findViewById(R.id.Player_PlayButtonProgress);
        mPlayButton = findViewById(R.id.Player_PlayButtonIcon);
        mVideo = findViewById(R.id.Player_VideoPlayer);

        findViewById(R.id.Player_ControlPlay).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack5).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack10).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack30).setOnClickListener(this);
        findViewById(R.id.Player_ControlStop).setOnClickListener(this);
        findViewById(R.id.Player_ControlFinish).setOnClickListener(this);
        findViewById(R.id.Player_VolumeUp).setOnClickListener(this);
        findViewById(R.id.Player_VolumeDown).setOnClickListener(this);

        mProgress.setProgress(0);

        loadMedia(isVideo ? "gs://neurosaude-firmino.appspot.com/video/video_week" + week + ".mp4" : "gs://neurosaude-firmino.appspot.com/audio/audio_week" + week + ".mp3");
        loadTexts();
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
    public void onBackPressed() {
        if (isReady) {
            mMediaPlayer.pause();
            setControlsVisible(false);
            findViewById(R.id.Player_PlayButtonLayout).setAlpha(0);
            mLoadingText.setVisibility(View.VISIBLE);
            mLoadingText.setText(R.string.saving);
            User.setAudioProgressAndValue(week, Math.max(lastProgress, getProgress()), lastMillis, () -> {
                isReady = false;
                mMediaPlayer.release();
                super.onBackPressed();
            });
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void run() {
        while (progressStatus <= duration) {
            if (isReady) {
                progressStatus += 1;
                backgroundTask.post(() -> {
                    int progress = getProgress();
                    mProgress.setProgress(progress);
                    int duration = mMediaPlayer.getCurrentPosition();
                    int total = mMediaPlayer.getDuration();
                    String actualTime = String.format(Locale.getDefault(),
                            "%02d:%02d ",
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                    String totalTime = String.format(Locale.getDefault(),
                            "%02d:%02d ",
                            TimeUnit.MILLISECONDS.toMinutes(total),
                            TimeUnit.MILLISECONDS.toSeconds(total) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(total)));
                    ((TextView) findViewById(R.id.Player_TimerActual)).setText(String.format("%s", actualTime));
                    ((TextView) findViewById(R.id.Player_TimerTotal)).setText(String.format("/%s", totalTime));
                    ((TextView) findViewById(R.id.Player_Volume)).setText(String.format(Locale.getDefault(), "%d%%", volume));
                    if (duration > lastMillis && !isFinished) lastMillis = duration;
                });
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadTexts() {
        FirebaseFirestore.getInstance().collection("texts/audio/data").document("week" + week).get().addOnCompleteListener(task -> {
            ((TextView) findViewById(R.id.Player_Title)).setText(task.getResult().getString("title"));
            ((TextView) findViewById(R.id.Player_Subtitle)).setText(task.getResult().getString("subtitle"));
            ((TextView) findViewById(R.id.Player_Text)).setText(task.getResult().getString("text"));
        });
    }

    private void loadMedia(String uri) {
        FirebaseStorage.getInstance().getReferenceFromUrl(uri).getDownloadUrl().addOnCompleteListener(task -> {
            try {
                mMediaPlayer = new MediaPlayer();
                if (isVideo) {
                    mVideo.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                            try {
                                mMediaPlayer.setDataSource(task.getResult().toString());
                                mMediaPlayer.setDisplay(surfaceHolder);
                                mVideo.setVisibility(View.VISIBLE);
                                mMediaPlayer.prepareAsync();
                                mMediaPlayer.setOnBufferingUpdateListener((mediaPlayer, i) -> {
                                    mLoadingText.setText(String.format(Locale.getDefault(), "Baixando\n%d %%", i));
                                    mProgress.setProgress(i);
                                    if (i == 100) {
                                        audioFullLoaded();
                                        mMediaPlayer.setOnBufferingUpdateListener(null);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                        }

                        @Override
                        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

                        }
                    });
                } else {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setDataSource(task.getResult().toString());
                    mMediaPlayer.prepareAsync();
                    mMediaPlayer.setOnBufferingUpdateListener((mediaPlayer, i) -> {
                        mLoadingText.setText(String.format(Locale.getDefault(), "Baixando\n%d %%", i));
                        mProgress.setProgress(i);
                        if (i == 100) {
                            audioFullLoaded();
                            mMediaPlayer.setOnBufferingUpdateListener(null);
                        }
                    });
                }


                mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    ((TextView) findViewById(R.id.Player_ControlPlay)).setText(R.string.play);
                    findViewById(R.id.Player_PlayButtonBack1).clearAnimation();
                    findViewById(R.id.Player_PlayButtonBack2).clearAnimation();
                    mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done, null));
                    mPlayButton.setOnClickListener(view -> onBackPressed());
                    setControlsVisible(false);
                    isFinished = true;
                    lastMillis = 0;
                });
            } catch (IOException e) {
                e.printStackTrace();
                MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Erro ao baixar o audio. Verifique sua conexão.");
                mLoadingText.setText("");
            }
        });
    }

    private void audioFullLoaded() {
        findViewById(R.id.Player_PlayButtonLayout).setAlpha(1);
        mProgress.setProgress(0);
        mLoadingText.setVisibility(View.GONE);
        duration = mMediaPlayer.getDuration();
        mMediaPlayer.seekTo(lastMillis);
        isReady = true;
        setControlsVisible(true);
    }

    private void play() {
        if (isReady) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_audio_play, null));
                ((TextView) findViewById(R.id.Player_ControlPlay)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_audio_play, 0, 0);
                ((TextView) findViewById(R.id.Player_ControlPlay)).setText(R.string.play);
                findViewById(R.id.Player_PlayButtonBack1).clearAnimation();
                findViewById(R.id.Player_PlayButtonBack2).clearAnimation();
            } else {
                mMediaPlayer.start();
                mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_audio_pause, null));
                ((TextView) findViewById(R.id.Player_ControlPlay)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_audio_pause, 0, 0);
                ((TextView) findViewById(R.id.Player_ControlPlay)).setText(R.string.pause);
                new Handler().postDelayed(() -> findViewById(R.id.Player_PlayButtonBack1).startAnimation(AnimationUtils.loadAnimation(this, R.anim.play_button_animation)), 50);
                new Handler().postDelayed(() -> findViewById(R.id.Player_PlayButtonBack2).startAnimation(AnimationUtils.loadAnimation(this, R.anim.play_button_animation2)), 120);
            }
        }
    }

    private void stop() {
        if (isReady) {
            back(mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0);
            mMediaPlayer.pause();
            mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_audio_play, null));
            ((TextView) findViewById(R.id.Player_ControlPlay)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_audio_play, 0, 0);
            findViewById(R.id.Player_PlayButtonBack1).clearAnimation();
            findViewById(R.id.Player_PlayButtonBack2).clearAnimation();
        }
    }

    private void setVolume(int volume) {
        if (isReady && volume >= 0 && volume <= 100) {
            this.volume = volume;
            mMediaPlayer.setVolume((float) this.volume / 100, (float) this.volume / 100);
            ValueAnimator anim = ValueAnimator.ofFloat(1f, 0.33f);
            anim.addUpdateListener(valueAnimator -> findViewById(R.id.Player_Volume).setAlpha((Float) valueAnimator.getAnimatedValue()));
            anim.setDuration(300);
            anim.start();
        }
    }

    private void back(int millis) {
        if (isReady) {
            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - millis);
        }
    }

    private void back() {
        if (isReady) {
            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - (mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0));
        }
    }

    private void setControlsVisible(boolean visible) {
        if (isControlsVisibles != visible && isReady && !isFinished) {
            isControlsVisibles = visible;
            float heightTop = getResources().getDimension(R.dimen.audio_controllayouttop);
            float heightMiddle = getResources().getDimension(R.dimen.audio_controllayoutmiddle);
            float heightBottom = getResources().getDimension(R.dimen.audio_controllayoutbottom);
            LinearLayout top = findViewById(R.id.Player_ControlLayoutTop);
            LinearLayout middle = findViewById(R.id.Player_ControlLayoutMiddle);
            LinearLayout bottom = findViewById(R.id.Player_ControlLayoutBottom);

            ValueAnimator anim = ValueAnimator.ofInt(visible ? 100 : 0, visible ? 0 : 100);
            anim.addUpdateListener(valueAnimator -> {
                top.setTranslationY(((int) valueAnimator.getAnimatedValue() * heightTop) / 100);
                middle.setTranslationY(((int) valueAnimator.getAnimatedValue() * heightMiddle) / 100);
                bottom.setTranslationY(((int) valueAnimator.getAnimatedValue() * heightBottom) / 100);
                findViewById(R.id.Player_TimerLayout).setAlpha(1f - (Integer.valueOf((int) valueAnimator.getAnimatedValue()).floatValue() / 100));
                findViewById(R.id.Player_ControlLabel).setAlpha((Integer.valueOf((int) valueAnimator.getAnimatedValue()).floatValue() / 3) / 100);
            });
            anim.setDuration(300);
            anim.start();
        }
    }

    private int getProgress() {
        if (isReady && mMediaPlayer.getDuration() != 0) {
            if (isFinished) return 100;
            float percent = ((float) mMediaPlayer.getCurrentPosition() / (float) mMediaPlayer.getDuration()) * 100;
            return Math.round(percent);
        } else {
            return 0;
        }
    }

    @Override
    public void onClick(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_alpha));
        if (view.getId() == R.id.Player_PlayButtonIcon) play();
        else if (view.getId() == R.id.Player_ControlBack) back();
        else if (view.getId() == R.id.Player_ControlBack5) back(5000);
        else if (view.getId() == R.id.Player_ControlBack10) back(10000);
        else if (view.getId() == R.id.Player_ControlBack30) back(30000);
        else if (view.getId() == R.id.Player_ControlStop) stop();
        else if (view.getId() == R.id.Player_ControlPlay) play();
        else if (view.getId() == R.id.Player_VolumeUp) setVolume(volume + 10);
        else if (view.getId() == R.id.Player_VolumeDown) setVolume(volume - 10);
        else if (view.getId() == R.id.Player_ControlFinish) onBackPressed();
    }
}