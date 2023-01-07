package com.firmino.neurossaude.admin.panel;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firmino.neurossaude.R;
import com.firmino.neurossaude.admin.users.Progress;

import java.util.Locale;

public class UserDetailsWeekView extends LinearLayout {
    private final Context mContext;

    public UserDetailsWeekView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.user_detail_weekview_layout, this);
        findViewById(R.id.WeekViewDetails_Title).setOnClickListener(v -> {
            LinearLayout lay = findViewById(R.id.WeekViewDetails_Layout);
            lay.setVisibility(lay.getVisibility() == VISIBLE ? GONE : VISIBLE);
        });
    }

    public void setup(Progress progress, int week) {
        int progressAudioAll = (int) ((progress.audio1Progress + progress.audio2Progress + progress.audio3Progress + progress.audio4Progress + progress.audio5Progress + progress.audio6Progress) / 6);
        int millisAudioAll = (int) ((progress.millisOnAudio1Media + progress.millisOnAudio2Media + progress.millisOnAudio3Media + progress.millisOnAudio4Media + progress.millisOnAudio5Media + progress.millisOnAudio6Media) / 6);
        ((TextView) findViewById(R.id.WeekViewDetails_Title)).setText(String.format(Locale.getDefault(), "Semana %d", week));

        ((TextView) findViewById(R.id.WeekViewDetails_VideoProgressText)).setText(String.format(Locale.getDefault(), "Progresso: %d %%", progress.videoProgress));
        ((TextView) findViewById(R.id.WeekViewDetails_TextProgressText)).setText(String.format(Locale.getDefault(), "Progresso: %d %%", progress.textProgress));
        ((TextView) findViewById(R.id.WeekViewDetails_AudioText)).setText(String.format(Locale.getDefault(), "Progresso: %d %%", progressAudioAll));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio1Text)).setText(String.format(Locale.getDefault(), "%d %%", progress.audio1Progress));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio2Text)).setText(String.format(Locale.getDefault(), "%d %%", progress.audio2Progress));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio3Text)).setText(String.format(Locale.getDefault(), "%d %%", progress.audio3Progress));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio4Text)).setText(String.format(Locale.getDefault(), "%d %%", progress.audio4Progress));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio5Text)).setText(String.format(Locale.getDefault(), "%d %%", progress.audio5Progress));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio6Text)).setText(String.format(Locale.getDefault(), "%d %%", progress.audio6Progress));

        ((ProgressBar) findViewById(R.id.WeekViewDetails_VideoProgress)).setProgress((int) progress.videoProgress);
        ((ProgressBar) findViewById(R.id.WeekViewDetails_TextProgress)).setProgress((int) progress.textProgress);
        ((ProgressBar) findViewById(R.id.WeekViewDetails_AudioProgress)).setProgress(progressAudioAll);

        ((TextView) findViewById(R.id.WeekViewDetails_VideoTime)).setText(toHour((int) progress.millisOnVideoMedia));
        ((TextView) findViewById(R.id.WeekViewDetails_TextTime)).setText(toHour((int) progress.millisOnTextMedia));
        ((TextView) findViewById(R.id.WeekViewDetails_AudioTime)).setText(toHour(millisAudioAll));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio1Time)).setText(toHour((int) progress.millisOnAudio1Media));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio2Time)).setText(toHour((int) progress.millisOnAudio2Media));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio3Time)).setText(toHour((int) progress.millisOnAudio3Media));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio4Time)).setText(toHour((int) progress.millisOnAudio4Media));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio5Time)).setText(toHour((int) progress.millisOnAudio5Media));
        ((TextView) findViewById(R.id.WeekViewDetails_Audio6Time)).setText(toHour((int) progress.millisOnAudio6Media));
    }

    private String toHour(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60));
        return String.format(Locale.getDefault(), "%02dm:%02ds", minutes, seconds);
    }
}
