package com.firmino.neurossaude;

import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.firmino.neurossaude.alerts.MessageAlert;
import com.firmino.neurossaude.mediaactivity.MediaActivity;
import com.firmino.neurossaude.views.MediaControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VideoMediaActivity extends MediaActivity {

    private MediaControl mMediaControl;
    private ExoPlayer mPlayer;
    private int volume = 100;
    private float mPlaybackSpeed = 1F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_media);

        mMediaControl = findViewById(R.id.Video_MediaControl);

        ((TextView) findViewById(R.id.Video_Week)).setText(extrasTitle);
        ((TextView) findViewById(R.id.Video_Title)).setText(extraSubtitle);
        ((TextView) findViewById(R.id.Video_Description)).setText(extraDesciption);

        mPlayer = new ExoPlayer.Builder(this).build();
        ((StyledPlayerView) findViewById(R.id.Video_Player)).setPlayer(mPlayer);

        setVolume(getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).getInt("lastVolume", 100));

        mMediaControl.setOnClickMediaControlButton(new MediaControl.OnClickMediaControlButton() {
            @Override
            public void onMediaControl1Clicked(TextView view) {
                mPlayer.seekTo(mPlayer.getCurrentPosition() - (mPlayer != null ? mPlayer.getCurrentPosition() : 0));
            }

            @Override
            public void onMediaControl2Clicked(TextView view) {
                mPlayer.seekTo(mPlayer.getCurrentPosition() - 30000);
            }

            @Override
            public void onMediaControl3Clicked(TextView view) {
                if (extraLastProgress >= 99)
                    mPlayer.seekTo(mPlayer.getCurrentPosition() + (long) 30000);
                else
                    MessageAlert.create(VideoMediaActivity.this, MessageAlert.TYPE_ALERT, "Você só pode avançar a mídia se já tiver assistido-a por completo previamente.");

            }

            @Override
            public void onMediaControl4Clicked(TextView view) {
                if (mPlaybackSpeed < 2) mPlaybackSpeed += 0.25;
                else mPlaybackSpeed = 1;
                mPlayer.setPlaybackSpeed(mPlaybackSpeed);
                ((TextView) view).setText(String.format("Velo. %sx", mPlaybackSpeed));
            }

            @Override
            public void onMediaControl5Clicked(TextView view) {
                mPlayer.seekTo(mPlayer.getCurrentPosition() - (mPlayer != null ? mPlayer.getCurrentPosition() : 0));
                mPlayer.pause();
                mMediaControl.getButton(5).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_audio_play, 0, 0);
            }

            @Override
            public void onMediaControl6Clicked(TextView view) {
                play();
            }

            @Override
            public void onMediaControl7Clicked(TextView view) {
                setVolume(volume - 10);
            }

            @Override
            public void onMediaControl8Clicked(TextView view) {
                setVolume(volume + 10);
            }

            @Override
            public void onMediaControl9Clicked(TextView view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        mPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mPlayer.pause();
        super.onStop();
    }

    @Override
    public void loadMediaComplete(Uri downloadLink) {
        runOnUiThread(() -> {
            mPlayer.setMediaItem(MediaItem.fromUri(downloadLink));
            mPlayer.prepare();
            mPlayer.addListener(new Player.Listener() {
                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    Player.Listener.super.onIsPlayingChanged(isPlaying);
                    mMediaControl.getButton(5).setCompoundDrawablesWithIntrinsicBounds(0, isPlaying ? R.drawable.ic_audio_pause : R.drawable.ic_audio_play, 0, 0);
                    mMediaControl.getButton(5).setText(isPlaying ? R.string.pause : R.string.play);
                }

                @Override
                public void onRenderedFirstFrame() {
                    Player.Listener.super.onRenderedFirstFrame();
                    ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
                    anim.addUpdateListener(valueAnimator -> findViewById(R.id.Video_Player_Layout).setAlpha((Float) valueAnimator.getAnimatedValue()));
                    anim.setDuration(300);
                    anim.start();
                    isLoaded = true;
                    mMediaControl.setProgress(0);
                    setControlsVisibleComplete(true);
                    findViewById(R.id.Video_LoadingText).setVisibility(View.GONE);
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if (playbackState == Player.STATE_READY && !isLoaded) {
                        mPlayer.seekTo(extraLastPosition);
                        findViewById(R.id.Video_Player_Layout).setAlpha(0);
                        findViewById(R.id.Video_Player_Layout).setVisibility(View.VISIBLE);
                    }

                    if (playbackState == Player.STATE_ENDED) {
                        mMediaControl.getButton(5).setText(R.string.play);
                        setControlsVisibleComplete(false);
                        isFinished = true;
                        extraLastPosition = 0;
                    }
                    findViewById(R.id.Video_Buffering).setAlpha(playbackState == Player.STATE_BUFFERING ? 1f : 0.3f);
                }
            });
            updateTimers();
        });
    }

    private void play() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.play();
        }
    }


    private void setVolume(int volume) {
        if (volume >= 0 && volume <= 100) {
            this.volume = volume;
            mPlayer.setVolume((float) this.volume / 100);
            ValueAnimator anim = ValueAnimator.ofFloat(1f, 0.33f);
            anim.addUpdateListener(valueAnimator -> findViewById(R.id.Video_Volume).setAlpha((Float) valueAnimator.getAnimatedValue()));
            anim.setDuration(300);
            anim.start();
            getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).edit().putInt("lastVolume", volume).apply();
        }
    }


    public void updateTimers() {
        long duration = mPlayer.getCurrentPosition();
        long total = mPlayer.getDuration();
        mMediaControl.setProgress((int) (((float) mPlayer.getCurrentPosition() / (float) mPlayer.getDuration()) * 1000));
        String actualTime = durationToTime(duration);
        String totalTime = durationToTime(total);
        ((TextView) findViewById(R.id.Video_TimerActual)).setText(String.format("%s", actualTime));
        ((TextView) findViewById(R.id.Video_TimerTotal)).setText(String.format("/%s", totalTime));
        ((TextView) findViewById(R.id.Video_Volume)).setText(String.format(Locale.getDefault(), "%d%%", volume));
        if (duration > extraLastPosition && !isFinished) extraLastPosition = duration;
        new Handler().postDelayed(this::updateTimers, 200);
    }

    private String durationToTime(long duration) {
        return String.format(
                Locale.getDefault(),
                "%02d:%02d ",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    @Override
    public int getProgress() {
        if (mPlayer.getDuration() != 0) {
            if (isFinished) return 100;
            float percent = ((float) mPlayer.getCurrentPosition() / (float) mPlayer.getDuration()) * 100;
            return Math.round(percent);
        } else {
            return 0;
        }
    }

    @Override
    public long getPosition() {
        return extraLastPosition;
    }

    @Override
    public void setControlsVisibleComplete(boolean visible) {
        mMediaControl.setControlsVisible(visible);
    }

    @Override
    public void onFinishMediaActivity() {
        mPlayer.pause();
        (findViewById(R.id.Video_Player_Layout)).setVisibility(View.INVISIBLE);
        findViewById(R.id.Video_LoadingText).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.Video_LoadingText)).setText(R.string.saving);
        mPlayer.release();
    }

    @Override
    public void onScreenOrientationChange(Configuration newConfig) {
        findViewById(R.id.Video_SpaceLayout).setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
        findViewById(R.id.Video_TitleLayout).setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
    }

}