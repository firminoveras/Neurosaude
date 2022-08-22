package com.firmino.neurossaude.user;

import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class User {
    public static String email;
    public static String username;
    public static Uri image;

    public static void setProgress(int week, @Nullable Integer videoProgress, @Nullable Integer textProgress, @Nullable Integer audioProgress, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (videoProgress != null) newProgresses.put("videoProgress", videoProgress);
        if (textProgress != null) newProgresses.put("textProgress", textProgress);
        if (audioProgress != null) newProgresses.put("audioProgress", audioProgress);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void setValues(int week, @Nullable Integer videoProgress, @Nullable Integer textProgress, @Nullable Integer audioProgress, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (videoProgress != null) newProgresses.put("videoValue", videoProgress);
        if (textProgress!= null) newProgresses.put("textValue", textProgress);
        if (audioProgress != null) newProgresses.put("audioValue", audioProgress);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void setAudioProgressAndValue(int week, @Nullable Integer audioProgress, @Nullable Integer audioValue, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (audioProgress != null) newProgresses.put("audioProgress", audioProgress);
        if (audioValue != null) newProgresses.put("audioValue", audioValue);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses).addOnCompleteListener(task -> listener.onSetProgressListener());
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
