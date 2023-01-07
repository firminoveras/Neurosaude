package com.firmino.neurossaude.admin.users;

public class Progress {
    public final long audio1Progress;
    public final long audio2Progress;
    public final long audio3Progress;
    public final long audio4Progress;
    public final long audio5Progress;
    public final long audio6Progress;
    public final long textProgress;
    public final long videoProgress;
    public final long millisOnAudio1Media;
    public final long millisOnAudio2Media;
    public final long millisOnAudio3Media;
    public final long millisOnAudio4Media;
    public final long millisOnAudio5Media;
    public final long millisOnAudio6Media;
    public final long millisOnTextMedia;
    public final long millisOnVideoMedia;

    public Progress(long audio1Progress,
                    long audio2Progress,
                    long audio3Progress,
                    long audio4Progress,
                    long audio5Progress,
                    long audio6Progress,
                    long textProgress,
                    long videoProgress,
                    long millisOnAudio1Media,
                    long millisOnAudio2Media,
                    long millisOnAudio3Media,
                    long millisOnAudio4Media,
                    long millisOnAudio5Media,
                    long millisOnAudio6Media,
                    long millisOnTextMedia,
                    long millisOnVideoMedia) {
        this.audio1Progress = audio1Progress;
        this.audio2Progress = audio2Progress;
        this.audio3Progress = audio3Progress;
        this.audio4Progress = audio4Progress;
        this.audio5Progress = audio5Progress;
        this.audio6Progress = audio6Progress;
        this.textProgress = textProgress;
        this.videoProgress = videoProgress;
        this.millisOnAudio1Media = millisOnAudio1Media;
        this.millisOnAudio2Media = millisOnAudio2Media;
        this.millisOnAudio3Media = millisOnAudio3Media;
        this.millisOnAudio4Media = millisOnAudio4Media;
        this.millisOnAudio5Media = millisOnAudio5Media;
        this.millisOnAudio6Media = millisOnAudio6Media;
        this.millisOnTextMedia = millisOnTextMedia;
        this.millisOnVideoMedia = millisOnVideoMedia;
    }

    public float getProgressPercent() {
        float progress = (audio1Progress + audio2Progress + audio3Progress + audio4Progress + audio5Progress + audio6Progress + videoProgress);
        return progress / 7;
    }
}
