package com.firmino.neurossaude.user;

import android.content.Context;
import android.graphics.Bitmap;

import com.firmino.neurossaude.admin.users.Profile;
import com.firmino.neurossaude.admin.users.Progress;
import com.firmino.neurossaude.views.WeekView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

public class User {
    public static String email;
    public static String username;
    public static Bitmap image;
    public static boolean admin = false;
    public static final List<Map<String, List<Object>>> progressWeeks = new ArrayList<>();


    public static void setAudioValues(int week, int audioIndex, @Nullable Integer audioProgress, @Nullable Long audioValue, Long millisOnMedia, OnSetProgressListener listener) {
        Map<String, Object> newProgresses = new HashMap<>();
        if (audioProgress != null) newProgresses.put("audio" + audioIndex + "Progress", audioProgress);
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

    @SuppressWarnings("unchecked")
    public static void loadProgress(@Nullable OnTaskCompleted listener) {
        FirebaseFirestore.getInstance().collection("users/" + email + "/progress/").get().addOnCompleteListener(task -> {
            progressWeeks.clear();
            QuerySnapshot result = task.getResult();
            for (DocumentSnapshot week : result.getDocuments()) {
                Map<String, List<Object>> newWeek = new HashMap<>();
                for (String field : new String[]{"video", "text", "audio1", "audio2", "audio3", "audio4", "audio5", "audio6"}) {
                    List<Object> data = (List<Object>) week.get(field);
                    if (data != null) newWeek.put(field, data);
                }
                progressWeeks.add(newWeek);
            }
            if (listener != null) listener.onTaskCompletedListener();
        });
    }

    public static void saveProgress(@Nullable OnTaskCompleted listener) {
        CollectionReference firebaseInstance = FirebaseFirestore.getInstance().collection("users/" + email + "/progress/");
        AtomicInteger weeksCount = new AtomicInteger(progressWeeks.size());
        int weekNumber = 0;
        for (Map<String, List<Object>> week : progressWeeks) {
            Map<String, Object> weekDataArray = new HashMap<>();
            for (String field : new String[]{"video", "text", "audio1", "audio2", "audio3", "audio4", "audio5", "audio6"}) {
                List<Object> data = week.get(field);
                if (data != null) weekDataArray.put(field, data);
            }
            firebaseInstance.document("week" + (++weekNumber)).set(weekDataArray, SetOptions.merge()).addOnCompleteListener(task -> {
                weeksCount.getAndDecrement();
                if (weeksCount.get() <= 0 && listener != null) listener.onTaskCompletedListener();
            });
        }
    }

    public static void saveLocalProgress(int week, String field, Long percentage, Long millis) {
        Long count = 1L;

        try{
            List<Object> data =  progressWeeks.get(week - 1).get(field);
            count += (Long) data.get(1);
            millis += (Long) data.get(2);
        } catch (IndexOutOfBoundsException | NullPointerException exception) {
            exception.printStackTrace();
        }

        List<Object> data = new ArrayList<>();
        data.add(percentage);
        data.add(count);
        data.add(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss", Locale.getDefault());
        data.add(sdf.format(new Date()));

        try {
            progressWeeks.get(week - 1).put(field, data);
        } catch (IndexOutOfBoundsException exception) {
            while(progressWeeks.size() < week){
                Map<String, List<Object>> newWeek = new HashMap<>();
                progressWeeks.add(newWeek);
            }
            progressWeeks.get(week - 1).put(field, data);
        }
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

    public static void getProfiles(OnGetProfilesComplete listener) {
        List<Profile> profiles = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("users/").get().addOnCompleteListener(task -> {
            int profilesCount = task.getResult().size();
            for (QueryDocumentSnapshot result : task.getResult()) {
                String email = result.getString("email");
                String image = result.getString("image");
                String username = result.getString("username");
                Profile profile = new Profile(username, image, email);


                result.getReference().collection("progress").get().addOnCompleteListener(task1 -> {

                    for (QueryDocumentSnapshot result1 : task1.getResult()) {
                        Long audio1Progress = result1.getLong("audio1Progress");
                        Long audio2Progress = result1.getLong("audio2Progress");
                        Long audio3Progress = result1.getLong("audio3Progress");
                        Long audio4Progress = result1.getLong("audio4Progress");
                        Long audio5Progress = result1.getLong("audio5Progress");
                        Long audio6Progress = result1.getLong("audio6Progress");
                        Long textProgress = result1.getLong("textProgress");
                        Long videoProgress = result1.getLong("videoProgress");
                        Long millisOnAudio1Media = result1.getLong("millisOnAudio1Media");
                        Long millisOnAudio2Media = result1.getLong("millisOnAudio2Media");
                        Long millisOnAudio3Media = result1.getLong("millisOnAudio3Media");
                        Long millisOnAudio4Media = result1.getLong("millisOnAudio4Media");
                        Long millisOnAudio5Media = result1.getLong("millisOnAudio5Media");
                        Long millisOnAudio6Media = result1.getLong("millisOnAudio6Media");
                        Long millisOnTextMedia = result1.getLong("millisOnTextMedia");
                        Long millisOnVideoMedia = result1.getLong("millisOnVideoMedia");

                        Progress progress = new Progress(audio1Progress != null ? audio1Progress : 0, audio2Progress != null ? audio2Progress : 0, audio3Progress != null ? audio3Progress : 0, audio4Progress != null ? audio4Progress : 0, audio5Progress != null ? audio5Progress : 0, audio6Progress != null ? audio6Progress : 0, textProgress != null ? textProgress : 0, videoProgress != null ? videoProgress : 0, millisOnAudio1Media != null ? millisOnAudio1Media : 0, millisOnAudio2Media != null ? millisOnAudio2Media : 0, millisOnAudio3Media != null ? millisOnAudio3Media : 0, millisOnAudio4Media != null ? millisOnAudio4Media : 0, millisOnAudio5Media != null ? millisOnAudio5Media : 0, millisOnAudio6Media != null ? millisOnAudio6Media : 0, millisOnTextMedia != null ? millisOnTextMedia : 0, millisOnVideoMedia != null ? millisOnVideoMedia : 0

                        );
                        profile.addWeekProgress(progress);

                    }
                    profiles.add(profile);
                    if (profiles.size() == profilesCount) listener.onGetProfilesComplete(profiles);
                });
            }
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

    public interface OnGetProfilesComplete {
        void onGetProfilesComplete(List<Profile> profiles);
    }

    public interface OnTaskCompleted {
        void onTaskCompletedListener();
    }


}
