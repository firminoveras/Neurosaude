package com.firmino.neurossaude.user;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class User {
    public static String email;
    public static String username;
    public static Bitmap image;

    public static void setAudioValues(int week, int audioIndex, @Nullable Integer audioProgress, @Nullable Long audioValue, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (audioProgress != null)
            newProgresses.put("audio" + audioIndex + "Progress", audioProgress);
        if (audioValue != null) newProgresses.put("audio" + audioIndex + "Value", audioValue);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void setVideoValues(int week, @Nullable Integer videoProgress, @Nullable Long videoValue, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (videoProgress != null) newProgresses.put("videoProgress", videoProgress);
        if (videoValue != null) newProgresses.put("videoValue", videoValue);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void setTextValues(int week, @Nullable Integer textProgress, @Nullable Long textValue, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (textProgress != null) newProgresses.put("textProgress", textProgress);
        if (textValue != null) newProgresses.put("textValue", textValue);
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
    }

    public static void getWeekValues(int week, OnGetWeekValuesListener listener) {
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).get().addOnCompleteListener(task -> {

            Long value = task.getResult().getLong("videoProgress");
            int p1 = value != null ? value.intValue() : 0;
            value = task.getResult().getLong("videoValue");
            int p2 = value != null ? value.intValue() : 0;
            value = task.getResult().getLong("textProgress");
            int p3 = value != null ? value.intValue() : 0;
            value = task.getResult().getLong("textValue");
            int p4 = value != null ? value.intValue() : 0;

            List<Integer> p5 = new ArrayList<>();
            value = task.getResult().getLong("audio1Progress");
            p5.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio2Progress");
            p5.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio3Progress");
            p5.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio4Progress");
            p5.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio5Progress");
            p5.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio6Progress");
            p5.add(value != null ? value.intValue() : 0);

            List<Integer> p6 = new ArrayList<>();
            value = task.getResult().getLong("audio1Value");
            p6.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio2Value");
            p6.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio3Value");
            p6.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio4Value");
            p6.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio5Value");
            p6.add(value != null ? value.intValue() : 0);
            value = task.getResult().getLong("audio6Value");
            p6.add(value != null ? value.intValue() : 0);

            listener.onGetWeekValuesListener(p1, p2, p3, p4, p5, p6);
        });
    }

    public static void getWeekCount(OnGetWeekCountListener listener) {
        FirebaseFirestore.getInstance().collection("texts").get().addOnCompleteListener(task -> listener.onGetWeekCountListener(task.getResult().size()));
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

    public static void getTexts(OnGetTextsListener listener) {
        FirebaseFirestore.getInstance().collection("texts").get().addOnCompleteListener(task -> {
            List<String> title = new ArrayList<>(), videoTitle = new ArrayList<>(), videoText = new ArrayList<>(), audioTitle = new ArrayList<>(), audioText = new ArrayList<>(), textTitle = new ArrayList<>(), textText = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                title.add(doc.getString("title"));
                videoTitle.add(doc.getString("videoTitle"));
                videoText.add(doc.getString("videoText"));
                audioTitle.add(doc.getString("audioTitle"));
                audioText.add(doc.getString("audioText"));
                textTitle.add(doc.getString("textTitle"));
                textText.add(doc.getString("textText"));
            }
            listener.onGetTextsListener(title, videoTitle, videoText, audioTitle, audioText, textTitle, textText);
        });
    }


    public interface OnGetWeekInfoListener {
        void onGetWeekInfoListener(String title, String videoTitle, String videoText, String audioTitle, String audioText, String textTitle, String textText);
    }

    public interface OnGetWeekValuesListener {
        void onGetWeekValuesListener(int videoProgress, int videoValue, int textProgress, int textValue, List<Integer> audioProgress, List<Integer> audioValue);
    }

    public interface OnSetProgressListener {
        void onSetProgressListener();
    }

    public interface OnGetWeekCountListener {
        void onGetWeekCountListener(int count);
    }

    public interface OnGetTextsListener {
        void onGetTextsListener(List<String> title, List<String> videoTitle, List<String> videoText, List<String> audioTitle, List<String> audioText, List<String> textTitle, List<String> textText);
    }


}
