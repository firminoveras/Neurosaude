package com.firmino.neurossaude;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mLoadingText;
    private ProgressBar mProgressBar;
    private ImageView mPlayButton;
    private FrameLayout mAudioLayout;
    private FrameLayout mVideoLayout;
    private FrameLayout mTextLayout;

    private ExoPlayer mPlayer;
    private PDFView mPDFViewer;
    private int volume = 100;
    private long lastPosition = 0;
    private int lastProgress = 0;
    private float touchPoint;
    private boolean isFinished = false;
    private boolean isControlsVisibles = false;
    private boolean isLoaded = false;
    private int mediaType;

    public final static int MEDIA_TYPE_AUDIO = 1;
    public final static int MEDIA_TYPE_VIDEO = 2;
    public final static int MEDIA_TYPE_TEXT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        String url = getIntent().getExtras().getString("url");

        if (url.endsWith(".mp3")) mediaType = MEDIA_TYPE_AUDIO;
        if (url.endsWith(".mp4")) mediaType = MEDIA_TYPE_VIDEO;
        if (url.endsWith(".pdf")) mediaType = MEDIA_TYPE_TEXT;

        lastProgress = getIntent().getExtras().getInt("progress");
        lastPosition = getIntent().getExtras().getInt("value");

        mPlayer = new ExoPlayer.Builder(this).build();
        mPDFViewer = findViewById(R.id.Player_PDFViewer);
        mLoadingText = findViewById(R.id.Player_LoadingText);
        mPlayButton = findViewById(R.id.Player_PlayButtonIcon);
        mProgressBar = findViewById(R.id.Player_ProgressBar);
        mAudioLayout = findViewById(R.id.Player_AudioPlayerLayout);
        mVideoLayout = findViewById(R.id.Player_VideoPlayerLayout);
        mTextLayout = findViewById(R.id.Player_TextPlayerLayout);

        findViewById(R.id.Player_ControlPlay).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack5).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack10).setOnClickListener(this);
        findViewById(R.id.Player_ControlBack30).setOnClickListener(this);
        findViewById(R.id.Player_ControlStop).setOnClickListener(this);
        findViewById(R.id.Player_ControlFinish).setOnClickListener(this);
        findViewById(R.id.Player_VolumeUp).setOnClickListener(this);
        findViewById(R.id.Player_VolumeDown).setOnClickListener(this);

        findViewById(R.id.Text_ControlInit).setOnClickListener(this);
        findViewById(R.id.Text_ControlBack).setOnClickListener(this);
        findViewById(R.id.Text_ControlNext).setOnClickListener(this);
        findViewById(R.id.Text_ControlResetZoom).setOnClickListener(this);
        mPlayButton.setOnClickListener(view -> play());

        mProgressBar.setProgress(0);
        ((StyledPlayerView) findViewById(R.id.Player_VideoPlayer)).setPlayer(mPlayer);
        ((TextView) findViewById(R.id.Player_Week)).setText(getIntent().getExtras().getString("maintitle"));
        ((TextView) findViewById(R.id.Player_Title)).setText(getIntent().getExtras().getString("title"));
        ((TextView) findViewById(R.id.Player_Text)).setText(getIntent().getExtras().getString("text"));

        loadMedia(url);

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
        mPlayer.pause();
        setControlsVisible(false);
        mAudioLayout.setVisibility(View.INVISIBLE);
        mVideoLayout.setVisibility(View.INVISIBLE);
        mTextLayout.setVisibility(View.INVISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);
        mLoadingText.setText(R.string.saving);
        Intent intent = new Intent();
        System.out.println(getProgress());
        intent.putExtra("progress", Math.max(lastProgress, getProgress()));
        intent.putExtra("position", lastPosition);
        setResult(mediaType, intent);
        mPlayer.release();
        mPDFViewer.recycle();
        super.onBackPressed();
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
        else if (view.getId() == R.id.Text_ControlInit) mPDFViewer.jumpTo(0, true);
        else if (view.getId() == R.id.Text_ControlBack)
            mPDFViewer.jumpTo(mPDFViewer.getCurrentPage() - 1, true);
        else if (view.getId() == R.id.Text_ControlNext)
            mPDFViewer.jumpTo(mPDFViewer.getCurrentPage() + 1, true);
        else if (view.getId() == R.id.Text_ControlResetZoom)
            mPDFViewer.fitToWidth(mPDFViewer.getCurrentPage());
    }

    private void loadMedia(String uri) {
        FirebaseStorage.getInstance().getReferenceFromUrl(uri).getDownloadUrl().addOnCompleteListener(task -> {
            Uri downloadLink = Uri.parse(task.getResult().toString());
            if (mediaType == MEDIA_TYPE_TEXT) {
                AsyncTask.execute(() -> {
                    try {
                        final InputStream input = new URL(task.getResult().toString()).openStream();

                        runOnUiThread(() -> {
                            mPDFViewer.fromStream(input)
                                    .enableDoubletap(true)
                                    .defaultPage((int) lastPosition)
                                    .enableSwipe(true)
                                    .pageFitPolicy(FitPolicy.WIDTH)
                                    .fitEachPage(true)
                                    .autoSpacing(true)
                                    .onPageChange((page, pageCount) -> {
                                        mProgressBar.setMax(pageCount);
                                        mProgressBar.setProgress(page + 1);
                                        ((TextView) findViewById(R.id.Text_PageActual)).setText(String.valueOf(page + 1));
                                        ((TextView) findViewById(R.id.Text_PageTotal)).setText(String.format(Locale.getDefault(), "/%d", pageCount));
                                        lastPosition = page;
                                    })
                                    .load();
                            mTextLayout.setVisibility(View.VISIBLE);
                            mLoadingText.setVisibility(View.GONE);
                            isLoaded = true;
                            setControlsVisible(true);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                mPlayer.setMediaItem(MediaItem.fromUri(downloadLink));
                mPlayer.prepare();
                mPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        Player.Listener.super.onIsPlayingChanged(isPlaying);
                        mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), isPlaying ? R.drawable.ic_audio_pause : R.drawable.ic_audio_play, null));
                        ((TextView) findViewById(R.id.Player_ControlPlay)).setCompoundDrawablesWithIntrinsicBounds(0, isPlaying ? R.drawable.ic_audio_pause : R.drawable.ic_audio_play, 0, 0);
                        ((TextView) findViewById(R.id.Player_ControlPlay)).setText(isPlaying ? R.string.pause : R.string.play);
                        if (isPlaying) {
                            new Handler().postDelayed(() -> findViewById(R.id.Player_PlayButtonBack1).startAnimation(AnimationUtils.loadAnimation(PlayActivity.this, R.anim.play_button_animation)), 50);
                            new Handler().postDelayed(() -> findViewById(R.id.Player_PlayButtonBack2).startAnimation(AnimationUtils.loadAnimation(PlayActivity.this, R.anim.play_button_animation2)), 120);
                        } else {
                            findViewById(R.id.Player_PlayButtonBack1).clearAnimation();
                            findViewById(R.id.Player_PlayButtonBack2).clearAnimation();
                        }
                    }

                    @Override
                    public void onRenderedFirstFrame() {
                        Player.Listener.super.onRenderedFirstFrame();
                        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
                        anim.addUpdateListener(valueAnimator -> mVideoLayout.setAlpha((Float) valueAnimator.getAnimatedValue()));
                        anim.setDuration(300);
                        anim.start();
                        isLoaded = true;
                        setControlsVisible(true);
                        mProgressBar.setProgress(0);
                        mLoadingText.setVisibility(View.GONE);
                    }

                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        Player.Listener.super.onPlaybackStateChanged(playbackState);
                        if (playbackState == Player.STATE_READY && !isLoaded) {
                            mPlayer.seekTo(lastPosition);
                            if (mediaType == MEDIA_TYPE_VIDEO) {
                                mVideoLayout.setAlpha(0);
                                mVideoLayout.setVisibility(View.VISIBLE);
                            }
                            if (mediaType == MEDIA_TYPE_AUDIO) {
                                mAudioLayout.setVisibility(View.VISIBLE);
                                isLoaded = true;
                                setControlsVisible(true);
                                mProgressBar.setProgress(0);
                                mLoadingText.setVisibility(View.GONE);
                            }
                        }

                        if (playbackState == Player.STATE_ENDED) {
                            ((TextView) findViewById(R.id.Player_ControlPlay)).setText(R.string.play);
                            findViewById(R.id.Player_PlayButtonBack1).clearAnimation();
                            findViewById(R.id.Player_PlayButtonBack2).clearAnimation();
                            mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done, null));
                            mPlayButton.setOnClickListener(view -> onBackPressed());
                            setControlsVisible(false);
                            isFinished = true;
                            lastPosition = 0;
                        }
                        findViewById(R.id.Player_Buffering).setAlpha(playbackState == Player.STATE_BUFFERING ? 1f : 0.3f);
                    }
                });
                updateTimers();
            }
        });
    }

    private void play() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.play();
        }
    }

    private void stop() {
        back(mPlayer != null ? mPlayer.getCurrentPosition() : 0);
        mPlayer.pause();
        mPlayButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_audio_play, null));
        ((TextView) findViewById(R.id.Player_ControlPlay)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_audio_play, 0, 0);
        findViewById(R.id.Player_PlayButtonBack1).clearAnimation();
        findViewById(R.id.Player_PlayButtonBack2).clearAnimation();

    }

    private void setVolume(int volume) {
        if (volume >= 0 && volume <= 100) {
            this.volume = volume;
            mPlayer.setVolume((float) this.volume / 100);
            ValueAnimator anim = ValueAnimator.ofFloat(1f, 0.33f);
            anim.addUpdateListener(valueAnimator -> findViewById(R.id.Player_Volume).setAlpha((Float) valueAnimator.getAnimatedValue()));
            anim.setDuration(300);
            anim.start();
        }
    }

    private void back(long millis) {
        mPlayer.seekTo(mPlayer.getCurrentPosition() - millis);

    }

    private void back() {
        mPlayer.seekTo(mPlayer.getCurrentPosition() - (mPlayer != null ? mPlayer.getCurrentPosition() : 0));

    }

    private void setControlsVisible(boolean visible) {
        if (isControlsVisibles != visible && !isFinished && isLoaded) {
            isControlsVisibles = visible;
            float heightTop = getResources().getDimension(R.dimen.audio_controllayouttop);
            float heightMiddle = getResources().getDimension(R.dimen.audio_controllayoutmiddle);
            float heightBottom = getResources().getDimension(R.dimen.audio_controllayoutbottom);

            ValueAnimator anim = ValueAnimator.ofInt(visible ? 100 : 0, visible ? 0 : 100);
            anim.addUpdateListener(valueAnimator -> {
                if (mediaType == MEDIA_TYPE_TEXT) {
                    findViewById(R.id.Text_PagesLayout).setAlpha(1f - (Integer.valueOf((int) valueAnimator.getAnimatedValue()).floatValue() / 100));
                    findViewById(R.id.Text_ControlLayoutTop).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightTop) / 100);
                } else {
                    findViewById(R.id.Player_ControlLayoutTop).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightTop) / 100);
                    findViewById(R.id.Player_ControlLayoutMiddle).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightMiddle) / 100);
                    findViewById(R.id.Player_ControlLayoutBottom).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightBottom) / 100);
                    findViewById(R.id.Player_TimerLayout).setAlpha(1f - (Integer.valueOf((int) valueAnimator.getAnimatedValue()).floatValue() / 100));
                }

                findViewById(R.id.Player_ProgressBar).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightTop) / 100);
                findViewById(R.id.Player_ControlLayoutBottom).setTranslationY(((int) valueAnimator.getAnimatedValue() * heightBottom) / 100);
                findViewById(R.id.Player_ControlLabel).setAlpha((Integer.valueOf((int) valueAnimator.getAnimatedValue()).floatValue() / 3) / 100);
            });
            anim.setDuration(300);
            anim.start();
        }
    }

    private int getProgress() {
        if (mediaType == MEDIA_TYPE_TEXT) {
            return Math.round(((float) (mPDFViewer.getCurrentPage() + 1) / (float) mPDFViewer.getPageCount()) * 100);
        } else {
            if (mPlayer.getDuration() != 0) {
                if (isFinished) return 100;
                float percent = ((float) mPlayer.getCurrentPosition() / (float) mPlayer.getDuration()) * 100;
                return Math.round(percent);
            } else {
                return 0;
            }
        }

    }

    public void updateTimers() {
        long duration = mPlayer.getCurrentPosition();
        long total = mPlayer.getDuration();
        mProgressBar.setProgress((int) (((float) mPlayer.getCurrentPosition() / (float) mPlayer.getDuration()) * 1000));
        String actualTime = durationToTime(duration);
        String totalTime = durationToTime(total);
        ((TextView) findViewById(R.id.Player_TimerActual)).setText(String.format("%s", actualTime));
        ((TextView) findViewById(R.id.Player_TimerTotal)).setText(String.format("/%s", totalTime));
        ((TextView) findViewById(R.id.Player_Volume)).setText(String.format(Locale.getDefault(), "%d%%", volume));
        if (duration > lastPosition && !isFinished) lastPosition = duration;
        new Handler().postDelayed(this::updateTimers, 200);
    }

    private String durationToTime(long duration) {
        return String.format(
                Locale.getDefault(),
                "%02d:%02d ",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

}