package com.firmino.neurossaude.user;

import android.content.Context;
import android.graphics.Bitmap;

import com.firmino.neurossaude.views.WeekView;
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

    public static void setAudioValues(int week, int audioIndex, @Nullable Integer audioProgress, @Nullable Long audioValue, Long millisOnMedia, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (audioProgress != null)
            newProgresses.put("audio" + audioIndex + "Progress", audioProgress);
        if (audioValue != null) newProgresses.put("audio" + audioIndex + "Value", audioValue);

        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).get().addOnCompleteListener(task1 -> {
            Long actualMillis = task1.getResult().getLong("millisOnAudio" + audioIndex + "Media");
            if (actualMillis == null) actualMillis = 0L;
            actualMillis += millisOnMedia;
            newProgresses.put("millisOnAudio" + audioIndex + "Media", actualMillis);

            FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
        });
    }

    public static void setVideoValues(int week, @Nullable Integer videoProgress, @Nullable Long videoValue, Long millisOnMedia, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (videoProgress != null) newProgresses.put("videoProgress", videoProgress);
        if (videoValue != null) newProgresses.put("videoValue", videoValue);

        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).get().addOnCompleteListener(task1 -> {
            Long actualMillis = task1.getResult().getLong("millisOnVideoMedia");
            if (actualMillis == null) actualMillis = 0L;
            actualMillis += millisOnMedia;
            newProgresses.put("millisOnVideoMedia", actualMillis);
            FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
        });
    }

    public static void setTextValues(int week, @Nullable Integer textProgress, @Nullable Long textValue, Long millisOnMedia, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (textProgress != null) newProgresses.put("textProgress", textProgress);
        if (textValue != null) newProgresses.put("textValue", textValue);

        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).get().addOnCompleteListener(task1 -> {
            Long actualMillis = task1.getResult().getLong("millisOnTextMedia");
            if (actualMillis == null) actualMillis = 0L;
            actualMillis += millisOnMedia;
            newProgresses.put("millisOnTextMedia", actualMillis);
            FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").document("week" + week).set(newProgresses, SetOptions.merge()).addOnCompleteListener(task -> listener.onSetProgressListener());
        });
    }

    public static void getTimeOnMediaOnAllWeeks(OnGetTimeOnMediaOnAllWeeksListener listener) {
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").get().addOnCompleteListener(task -> {
            long millis = 0L;
            List<Long> millisList = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult().getDocuments()) {
                millisList.add(document.getLong("millisOnTextMedia"));
                millisList.add(document.getLong("millisOnVideoMedia"));
                millisList.add(document.getLong("millisOnAudio1Media"));
                millisList.add(document.getLong("millisOnAudio2Media"));
                millisList.add(document.getLong("millisOnAudio3Media"));
                millisList.add(document.getLong("millisOnAudio4Media"));
                millisList.add(document.getLong("millisOnAudio5Media"));
                millisList.add(document.getLong("millisOnAudio6Media"));
            }
            for (Long m : millisList) if (m != null) millis += m;

            int minutes = (int) ((millis / (1000 * 60)) % 60);
            int hours = (int) ((millis / (1000 * 60 * 60)));

            listener.onGetTimeOnMediaOnAllWeeksListener(hours, minutes);
        });
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


    public static void getWeekViews(Context context, OnGetTextsListener listener) {
        FirebaseFirestore.getInstance().collection("weeks").get().addOnCompleteListener(task -> {
            List<WeekView> weekViews = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                int week = Integer.parseInt(doc.getId());
                String title = doc.getString("title");
                String videoTitle = doc.getString("videoTitle");
                String videoText = doc.getString("videoText");
                String audioTitle = doc.getString("audioTitle");
                String audioText = doc.getString("audioText");
                String textTitle = doc.getString("textTitle");
                String textText = doc.getString("textText");
                weekViews.add(new WeekView(context, week, title, videoTitle, videoText, audioTitle, audioText, textTitle, textText));
            }
            listener.onGetTextsListener(weekViews);
        });
    }


    public interface OnGetWeekValuesListener {
        void onGetWeekValuesListener(int videoProgress, int videoValue, int textProgress, int textValue, List<Integer> audioProgress, List<Integer> audioValue);
    }

    public interface OnSetProgressListener {
        void onSetProgressListener();
    }


    public interface OnGetTextsListener {
        void onGetTextsListener(List<WeekView> weekViews);
    }

    public interface OnGetTimeOnMediaOnAllWeeksListener {
        void onGetTimeOnMediaOnAllWeeksListener(int hours, int minutes);
    }


}
