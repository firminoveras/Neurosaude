package com.firmino.neurossaude.user;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class User {
    public static String email;
    public static String username;
    public static Bitmap image;

    public static void setAudioProgressAndValue(int week, @Nullable Integer audioProgress, @Nullable Long audioValue, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (audioProgress != null) newProgresses.put("audioProgress", audioProgress);
        if (audioValue != null) newProgresses.put("audioValue", audioValue);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void setVideoProgressAndValue(int week, @Nullable Integer videoProgress, @Nullable Long videoValue, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (videoProgress != null) newProgresses.put("videoProgress", videoProgress);
        if (videoValue != null) newProgresses.put("videoValue", videoValue);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void setTextProgressAndValue(int week, @Nullable Integer textProgress, @Nullable Long textValue, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (textProgress != null) newProgresses.put("textProgress", textProgress);
        if (textValue != null) newProgresses.put("textValue", textValue);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void getValues(int week, OnGetValueListener listener) {
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).get().addOnCompleteListener(task -> {
            Long videoProgress = task.getResult().getLong("videoValue");
            int v = videoProgress != null ? videoProgress.intValue() : 0;

            Long textProgress = task.getResult().getLong("textValue");
            int t = textProgress != null ? textProgress.intValue() : 0;

            Long audioProgress = task.getResult().getLong("audioValue");
            int a = audioProgress != null ? audioProgress.intValue() : 0;

            listener.onGetValueListener(v, t, a);
        });
    }

    public static void getProgress(int week, OnGetProgressListener listener) {
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).get().addOnCompleteListener(task -> {
            Long videoProgress = task.getResult().getLong("videoProgress");
            int v = videoProgress != null ? videoProgress.intValue() : 0;

            Long textProgress = task.getResult().getLong("textProgress");
            int t = textProgress != null ? textProgress.intValue() : 0;

            Long audioProgress = task.getResult().getLong("audioProgress");
            int a = audioProgress != null ? audioProgress.intValue() : 0;

            listener.onGetProgressListener(v, t, a);
        });
    }

    public static void getWeekTexts(int week, OnGetWeekInfoListener listener) {
        FirebaseFirestore.getInstance().collection("texts").document("week" + week).get().addOnCompleteListener(task -> {
            String videoTitle = task.getResult().getString("videoTitle");
            String videoText = task.getResult().getString("videoText");
            String audioTitle = task.getResult().getString("audioTitle");
            String audioText = task.getResult().getString("audioText");
            String textTitle = task.getResult().getString("textTitle");
            String textText = task.getResult().getString("textText");
            String title = task.getResult().getString("title");
            listener.onGetWeekInfoListener(title, videoTitle, videoText, audioTitle, audioText, textTitle, textText);
        });
    }



    public interface OnGetWeekInfoListener {
        void onGetWeekInfoListener(String title, String videoTitle, String videoText, String audioTitle, String audioText, String textTitle, String textText);
    }

    public interface OnGetProgressListener {
        void onGetProgressListener(int videoProgress, int textProgress, int audioProgress);
    }

    public interface OnGetValueListener {
        void onGetValueListener(int videoLastMillis, int textLastPage, int audioLastMillis);
    }

    public interface OnSetProgressListener {
        void onSetProgressListener();
    }


}
