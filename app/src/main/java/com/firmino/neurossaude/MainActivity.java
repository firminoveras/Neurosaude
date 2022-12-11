package com.firmino.neurossaude;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firmino.neurossaude.alerts.AlarmReceiver;
import com.firmino.neurossaude.alerts.MessageAlert;
import com.firmino.neurossaude.user.User;
import com.firmino.neurossaude.views.WeekView;
import com.firmino.neurossaude.views.WeekViewCoinButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> mediaActivityResult, initVideoActivityResult;
    private List<WeekView> mWeekViews;
    private TextView mMainProgress;
    private TextView mMainWeek;
    private TextView mMainTime;
    private AppBarLayout mMainAppBar;
    private LinearLayout mLayoutSettings;
    private FloatingActionButton mFabSettings;
    private boolean isSettingsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeekViews = new ArrayList<>();
        mMainProgress = findViewById(R.id.Main_Bar_Progress);
        mMainWeek = findViewById(R.id.Main_Bar_Week);
        mMainTime = findViewById(R.id.Main_Bar_Time);
        mMainAppBar = findViewById(R.id.Main_AppBar);
        mFabSettings = findViewById(R.id.Main_Button_Settings);
        mLayoutSettings = findViewById(R.id.Main_Layout_SettingsButtons);

        ((ImageView) findViewById(R.id.Main_Bar_UserImage)).setImageBitmap(User.image);
        ((TextView) findViewById(R.id.Main_Bar_UserName)).setText(User.username);
        findViewById(R.id.Main_Bar_Background).startAnimation(AnimationUtils.loadAnimation(this, R.anim.background_rotatoring_zoom));

        ViewGroup.LayoutParams lay = mMainAppBar.getLayoutParams();
        lay.height = getResources().getDisplayMetrics().heightPixels;
        mMainAppBar.setLayoutParams(lay);
        new Handler().postDelayed(() -> runOnUiThread(() -> {
            ValueAnimator anim = ValueAnimator.ofInt(getResources().getDisplayMetrics().heightPixels, (int) getResources().getDimension(R.dimen.app_bar_height));
            anim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = mMainAppBar.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                mMainAppBar.setLayoutParams(params);
            });
            anim.setDuration(1000);
            anim.start();
            mFabSettings.show();
        }), 3000);

        initMediaResultListener();
        initVideoActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> showAlarmDialog());

        findViewById(R.id.Main_Button_Alarm).setOnClickListener(view -> showAlarmDialog());
        findViewById(R.id.Main_Button_Exit).setOnClickListener(view -> showExitDialog());
        findViewById(R.id.Main_Button_Offline).setOnClickListener(view -> showOfflineModeDialog());
        mFabSettings.setOnClickListener(view -> setSettingsVisible(!isSettingsVisible));
        findViewById(R.id.Main_Button_InitialVideo).setOnClickListener(view -> startInitVideo());



        update();
    }

    @Override
    public void onBackPressed() {
        setSettingsVisible(false);
        View alertContent = getLayoutInflater().inflate(R.layout.back_dialog_layout, findViewById(R.id.Back_Main));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setView(alertContent);
        Dialog dialog = alert.show();

        alertContent.findViewById(R.id.Back_Cancel).setOnClickListener(v -> dialog.dismiss());
        alertContent.findViewById(R.id.Back_Close).setOnClickListener(v -> super.onBackPressed());
    }

    private void initMediaResultListener() {
        mediaActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null) {
                setLoadingMode(true);
                int progressResult = result.getData().getIntExtra("progress", -1);
                int week = result.getData().getIntExtra("week", -1);
                int audioIndex = result.getData().getIntExtra("audioIndex", -1);
                long positionResult = result.getData().getLongExtra("position", -1);
                long millisOnMedia = result.getData().getLongExtra("millisOnMedia", -1);
                switch (result.getData().getIntExtra("mediaType", -1)) {
                    case PlayActivity.MEDIA_TYPE_TEXT:
                        User.setTextValues(week, progressResult, positionResult, millisOnMedia, this::update);
                        break;
                    case PlayActivity.MEDIA_TYPE_VIDEO:
                        User.setVideoValues(week, progressResult, positionResult, millisOnMedia, this::update);
                        break;
                    case PlayActivity.MEDIA_TYPE_AUDIO:
                        User.setAudioValues(week, audioIndex, progressResult, millisOnMedia, positionResult, this::update);
                        break;
                }
            }
        });
    }

    private void setSettingsVisible(boolean visible) {
        if (isSettingsVisible != visible) {

            isSettingsVisible = visible;
            mFabSettings.setClickable(false);

            mFabSettings.startAnimation(AnimationUtils.loadAnimation(this, isSettingsVisible ? R.anim.rotate_fab : R.anim.rotate_fab_inverse));

            ValueAnimator anim = ValueAnimator.ofFloat(mLayoutSettings.getAlpha(), mLayoutSettings.getAlpha() == 0 ? 1f : 0f);
            anim.addUpdateListener(valueAnimator -> mLayoutSettings.setAlpha((Float) valueAnimator.getAnimatedValue()));
            anim.setDuration(300);
            anim.start();

            for (int i = 0; i < mLayoutSettings.getChildCount(); i++) {
                int finalI = i;
                float actualTranslation = mLayoutSettings.getChildAt(finalI).getTranslationX();
                ValueAnimator anim2 = ValueAnimator.ofFloat(actualTranslation, actualTranslation == 0 ? getResources().getDimension(R.dimen.MainLayoutButtonsWidth) : 0f);
                anim2.addUpdateListener(valueAnimator -> mLayoutSettings.getChildAt(finalI).setTranslationX((Float) valueAnimator.getAnimatedValue()));
                anim2.setDuration(300 + (100L * (i)));
                anim2.start();
            }

            new Handler().postDelayed(() -> runOnUiThread(() -> mFabSettings.setClickable(true)), 300 + (100L * (mLayoutSettings.getChildCount())));
        }
    }

    private void startInitVideo() {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("maintitle", "Introdução do Curso");
        intent.putExtra("title", "Olá, tudo bem com você?");
        intent.putExtra("text", getString(R.string.initvideo));
        intent.putExtra("url", "gs://neurosaude-firmino.appspot.com/video/video_intro.mp4");
        initVideoActivityResult.launch(intent);
    }

    private void showAlarmDialog() {
        setSettingsVisible(false);
        setFreeDistractionModeOn(true);
        SharedPreferences prefs = getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE);
        View alertContent = getLayoutInflater().inflate(R.layout.alarm_dialog_layout, findViewById(R.id.Alarm_Main));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setView(alertContent);
        Dialog dialog = alert.show();

        TimePicker alarmTimePicker = alertContent.findViewById(R.id.Alarm_TimePìcker);
        TextView alarmCloseButton = alertContent.findViewById(R.id.Alarm_SaveAndClose);
        SwitchMaterial alarmEnableSwitch = alertContent.findViewById(R.id.Alarm_Enable);

        alarmTimePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        alarmTimePicker.setIs24HourView(true);
        alarmTimePicker.setCurrentHour(prefs.getInt("hour", alarmTimePicker.getCurrentHour()));
        alarmTimePicker.setCurrentMinute(prefs.getInt("minutes", alarmTimePicker.getCurrentMinute()));

        alarmEnableSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            alarmTimePicker.setVisibility(b ? View.VISIBLE : View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("Neurosaude", "Lembretes das Práticas", importance);
                channel.setDescription("Canal de notificações para envio de lembretes para as práticas semanais.");
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        });
        alarmEnableSwitch.setChecked(prefs.getBoolean("reminderEnabled", false));

        alarmCloseButton.setOnClickListener(view -> {
            prefs.edit().putInt("hour", alarmTimePicker.getCurrentHour()).apply();
            prefs.edit().putInt("minutes", alarmTimePicker.getCurrentMinute()).apply();
            prefs.edit().putBoolean("reminderEnabled", alarmEnableSwitch.isChecked()).apply();
            AlarmReceiver.setAlarm(this);
            alertContent.findViewById(R.id.Alarm_Layout).setVisibility(View.GONE);
            if (alarmEnableSwitch.isChecked()) {

                MessageAlert.create(this, MessageAlert.TYPE_SUCESS, String.format(Locale.getDefault(), "Lembrete criado para %02dh:%02dmin", alarmTimePicker.getCurrentHour(), alarmTimePicker.getCurrentMinute()));
            } else {
                MessageAlert.create(this, MessageAlert.TYPE_SUCESS, "Lembretes desabilitados.");
            }
            setFreeDistractionModeOn(false);
            dialog.dismiss();
        });

        alarmEnableSwitch.setOnCheckedChangeListener((compoundButton, b) -> alarmTimePicker.setVisibility(b ? View.VISIBLE : View.GONE));
    }

    private void showExitDialog() {
        setSettingsVisible(false);
        setFreeDistractionModeOn(true);
        View alertContent = getLayoutInflater().inflate(R.layout.exit_dialog_layout, findViewById(R.id.Exit_Main));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setView(alertContent);
        Dialog dialog = alert.show();

        alertContent.findViewById(R.id.Exit_Cancel).setOnClickListener(v -> {
            setFreeDistractionModeOn(false);
            dialog.dismiss();
        });
        alertContent.findViewById(R.id.Exit_Close).setOnClickListener(v -> finish());
        alertContent.findViewById(R.id.Exit_LogOut).setOnClickListener(v -> {
            getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).edit().putBoolean("reminderEnabled", false).apply();
            AlarmReceiver.setAlarm(this);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                FirebaseAuth.getInstance().signOut();
                finish();
            });
        });
    }

    private void showOfflineModeDialog() {
        setSettingsVisible(false);
        MessageAlert.create(this,MessageAlert.TYPE_ALERT, "Essa funcionalidade ainda está em desenvolvimento.");
        /*
        setSettingsVisible(false);
        SharedPreferences prefs = getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE);
        View alertContent = getLayoutInflater().inflate(R.layout.offline_dialog_layout, findViewById(R.id.Offline_Main));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setView(alertContent);
        Dialog dialog = alert.show();

        SwitchMaterial offlineModeSwitch = alertContent.findViewById(R.id.Offline_Enable);
        ProgressBar offlineProgressBar = alertContent.findViewById(R.id.Offline_Progress);
        FrameLayout offlineDownloadButton = alertContent.findViewById(R.id.Offline_Download);
        TextView offlineExit = alertContent.findViewById(R.id.Offline_Close);

        offlineDownloadButton.setOnClickListener(v -> {
            offlineProgressBar.setIndeterminate(true);
            prefs.edit().putBoolean("offlineAllDownloaded", false).apply();
            offlineModeSwitch.setEnabled(false);
            offlineExit.setEnabled(false);
            offlineModeSwitch.setChecked(false);

            List<StorageReference> downloadItems = new ArrayList<>();

            StorageReference storage = FirebaseStorage.getInstance().getReference();
            Task<ListResult> TaskText = storage.child("text").listAll()
                    .addOnSuccessListener(listResult -> downloadItems.addAll(listResult.getItems()));
            Task<ListResult> TaskAudio = storage.child("audio").listAll()
                    .addOnSuccessListener(listResult -> downloadItems.addAll(listResult.getItems()));
            Task<ListResult> TaskVideo = storage.child("video").listAll()
                    .addOnSuccessListener(listResult -> downloadItems.addAll(listResult.getItems()));

            Tasks.whenAll(TaskText, TaskAudio, TaskVideo)
                    .addOnSuccessListener(unused -> {
                        int total = downloadItems.size();
                        AtomicInteger actual = new AtomicInteger();
                        offlineProgressBar.setIndeterminate(false);
                        offlineProgressBar.setMax(total);
                        offlineProgressBar.setProgress(0);
                        for (StorageReference item : downloadItems) {
                            try {
                                item.getFile(File.createTempFile(item.getName().split("\\.")[0], item.getName().split("\\.")[1]))
                                        .addOnSuccessListener(taskSnapshot -> {
                                            actual.getAndIncrement();
                                            offlineProgressBar.setProgress(actual.get());
                                            if (actual.get() == total) {
                                                prefs.edit().putBoolean("offlineAllDownloaded", true).apply();
                                                offlineModeSwitch.setEnabled(true);
                                                v.setVisibility(View.GONE);
                                                offlineExit.setEnabled(true);
                                                MessageAlert.create(this, MessageAlert.TYPE_SUCESS, "Curso inteiramente carregado com sucesso.");
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Ocorreu um erro ao baixar um item.");
                                            dialog.dismiss();
                                        });
                            } catch (IOException e) {
                                MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Ocorreu um erro ao baixar os itens");
                                dialog.dismiss();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Ocorreu um erro ao listar ítens");
                        dialog.dismiss();
                    });
        });

        offlineExit.setOnClickListener(v -> {
            prefs.edit().putBoolean("offlineMode",offlineModeSwitch.isChecked()).apply();
            if(!offlineModeSwitch.isChecked()){

                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                wifi.setWifiEnabled(false);

            }
        });

    */
    }

    private void setFreeDistractionModeOn(boolean active) {
        FrameLayout ripple = findViewById(R.id.Main_Ripple);
        if(active) {
            ripple.setVisibility(View.VISIBLE);
            int maxSize = getResources().getDisplayMetrics().heightPixels * 2;
            ValueAnimator anim = ValueAnimator.ofInt(0, maxSize);
            anim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = ripple.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                params.width = (int) animation.getAnimatedValue();
                ripple.setLayoutParams(params);
            });
            anim.setDuration(1000);
            anim.start();
        }else {
            ValueAnimator anim = ValueAnimator.ofInt(ripple.getLayoutParams().height, 0);
            anim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams params = ripple.getLayoutParams();
                params.height = (int) animation.getAnimatedValue();
                params.width = (int) animation.getAnimatedValue();
                ripple.setLayoutParams(params);
            });
            anim.setDuration(500);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ripple.setVisibility(View.GONE);
                    super.onAnimationEnd(animation);
                }
            });
        }
    }

    private void update() {
        setLoadingMode(true);
        User.getWeekViews(this, weekViews -> {
            ((ViewGroup) findViewById(R.id.mWeekViewList)).removeAllViews();
            mWeekViews = weekViews;
            for (WeekView weekView : mWeekViews) {
                weekView.setOnCoinClicked(intent -> mediaActivityResult.launch(intent));
                ((ViewGroup) findViewById(R.id.mWeekViewList)).addView(weekView);
                weekView.update();
            }

            new Thread(() -> {
                boolean allUpdateEnds = false;
                while (!allUpdateEnds) {
                    boolean actual = true;
                    for (WeekView weekView : mWeekViews) actual = weekView.isEnabledMode() && actual;
                    allUpdateEnds = actual;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                }
                int progress = 0;
                for (WeekView weekView : mWeekViews) progress += weekView.getProgress();
                int finalProgress = progress;

                runOnUiThread(() -> {
                    mMainProgress.setText(String.format("%s %%", finalProgress / mWeekViews.size()));
                    mWeekViews.get(0).unlock();
                    for (int i = 1; i < mWeekViews.size(); i++) if (mWeekViews.get(i - 1).getProgress() >= 100) mWeekViews.get(i).unlock();

                    getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).edit().putInt("lastCoinUnlockType", getLastCoinUnlocked().getType()).apply();
                    int totalWeeks = mWeekViews.size();
                    int unlockedWeeksCount = 0;
                    for (WeekView weekView : mWeekViews) if (weekView.isUnlocked()) unlockedWeeksCount++;
                    mMainWeek.setText(String.format(Locale.getDefault(), "%d/%d", unlockedWeeksCount, totalWeeks));

                    int dayOfTheMonth = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MINUTE);
                    getLastCoinUnlocked().setListenerComplete(() -> getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).edit().putInt("lastCompleteDate", dayOfTheMonth).apply());

                    if (getLastWeekViewUnlocked() != null)
                        getLastWeekViewUnlocked().getFirstLockedCoin().setUnlocked(dayOfTheMonth != getSharedPreferences("com.firmino.neurossaude", MODE_PRIVATE).getInt("lastCompleteDate", 0));

                    User.getTimeOnMediaOnAllWeeks((hours, minutes) -> {
                        mMainTime.setText(String.format(Locale.getDefault(), "%02dh%02dmin", hours, minutes));
                        setLoadingMode(false);
                    });
                });
            }).start();
        });
    }

    private void setLoadingMode(boolean active) {
        findViewById(R.id.Main_Layout_WeekViews).setVisibility(active ? View.GONE : View.VISIBLE);
        findViewById(R.id.Main_Progress_Loading).setVisibility(active ? View.VISIBLE : View.GONE);
        findViewById(R.id.Main_Bar_UserImageProgress).setVisibility(active ? View.VISIBLE : View.GONE);
        RelativeLayout imageLay = findViewById(R.id.AppBar_Image);
        if (imageLay.getWidth() != 0) {
            ValueAnimator anim = ValueAnimator.ofInt(imageLay.getWidth(), active ? ((ViewGroup) imageLay.getParent()).getWidth() : (int) getResources().getDimension(R.dimen.appbarimagesize));
            anim.addUpdateListener(animation -> {
                ViewGroup.LayoutParams lay = imageLay.getLayoutParams();
                lay.width = (int) animation.getAnimatedValue();
                imageLay.setLayoutParams(lay);
            });
            anim.setDuration(300);
            anim.start();
        }
    }

    public WeekViewCoinButton getLastCoinUnlocked() {
        for (WeekView weekView : mWeekViews)
            if (weekView.getProgress() < 100)
                return weekView.getLastUnlockedCoin();
        return null;
    }

    private WeekView getLastWeekViewUnlocked() {
        for (WeekView weekView : mWeekViews)
            if (weekView.isUnlocked())
                return weekView;
        return null;
    }

}