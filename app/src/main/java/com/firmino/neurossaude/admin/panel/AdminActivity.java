package com.firmino.neurossaude.admin.panel;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firmino.neurossaude.R;
import com.firmino.neurossaude.admin.users.Profile;
import com.firmino.neurossaude.admin.users.Progress;
import com.firmino.neurossaude.alerts.MessageAlert;
import com.firmino.neurossaude.user.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class AdminActivity extends AppCompatActivity {

    private LinearLayout mLayoutDetails;
    private UserDetailsAdapter mAdapter;
    private boolean isDetailsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        mLayoutDetails = findViewById(R.id.Admin_Details);
        findViewById(R.id.Admin_Details_Head).setOnClickListener(v -> setDetailsVisible(false));
        RecyclerView mList = findViewById(R.id.Admin_List);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(layoutManager);
        mAdapter = new UserDetailsAdapter(this);
        mAdapter.setOnUserDetailsItemClickListener(this::userViewClicled);
        mList.setAdapter(mAdapter);
        findViewById(R.id.Admin_MenuBar).setTranslationY(-getResources().getDimension(R.dimen.admin_menubar_size));
        update();

        findViewById(R.id.Admin_Menu_Clear).setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_alpha));
            mAdapter.selectClear();
        });

        findViewById(R.id.Admin_Menu_All).setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_alpha));
            mAdapter.selectAll();
        });
        findViewById(R.id.Admin_Menu_Refresh).setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_alpha));
            update();
        });
        findViewById(R.id.Admin_Menu_Download).setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click_alpha));
            downloadLog(mAdapter.getSelectedItems());
        });
    }

    private void userViewClicled(Profile profile) {
        setDetailsVisible(false);
        ((LinearLayout) findViewById(R.id.Admin_Details_Weeks)).removeAllViews();
        User.getWeekViews(this, weekViews -> runOnUiThread(() -> {
            int weekCount = weekViews.size();
            ((TextView) findViewById(R.id.Admin_Details_Username)).setText(profile.username);
            ((TextView) findViewById(R.id.Admin_Details_Email)).setText(profile.email);
            ((TextView) findViewById(R.id.Admin_Details_ProgressText)).setText(String.format(Locale.getDefault(), "%d %%", profile.getProgress(weekCount)));
            ((ProgressBar) findViewById(R.id.Admin_Details_ProgressBar)).setProgress(profile.getProgress(weekCount));
            int millis = profile.getTimeInMillis();
            int minutes = (millis / (1000 * 60)) % 60;
            int hours = millis / (1000 * 60 * 60);
            ((TextView) findViewById(R.id.Admin_Details_Time)).setText(String.format(Locale.getDefault(), "%02dh%02dmin", hours, minutes));
            if (profile.image != null) {
                new Thread(() -> {
                    Bitmap image;
                    try {
                        URL newurl = new URL(Uri.parse(profile.image).toString());
                        image = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                    } catch (IOException e) {
                        image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_google_logged);
                        ((ImageView) findViewById(R.id.Admin_Details_Image)).setImageBitmap(image);
                    }

                    Bitmap finalImage = image;
                    runOnUiThread(() -> {
                        ((ImageView) findViewById(R.id.Admin_Details_Image)).setImageBitmap(finalImage);
                        int week = 0;
                        for (Progress p : profile.getProgressWeeks()) {
                            week++;
                            UserDetailsWeekView weekView = new UserDetailsWeekView(this);
                            weekView.setup(p, week);
                            ((LinearLayout) findViewById(R.id.Admin_Details_Weeks)).addView(weekView);
                        }
                        setDetailsVisible(true);
                    });
                }).start();
            } else {
                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_google_logged);
                ((ImageView) findViewById(R.id.Admin_Details_Image)).setImageBitmap(image);
            }
        }));
    }

    private void update() {
        setLoadingMode(true);
        mAdapter.clearAllViews();
        User.getProfiles(profiles -> {
            for (Profile p : profiles) mAdapter.addUserDetailView(p);
            new Handler().postDelayed(() -> setLoadingMode(false), 1000);
        });
    }

    private void setLoadingMode(boolean active) {
        findViewById(R.id.Admin_Loading).setVisibility(active ? View.VISIBLE : View.GONE);
        findViewById(R.id.Admin_List).setVisibility(active ? View.GONE : View.VISIBLE);
        LinearLayout menuLay = findViewById(R.id.Admin_MenuBar);
        ValueAnimator anim = ValueAnimator.ofFloat(!active ? -getResources().getDimension(R.dimen.admin_menubar_size) : 0, !active ? 0 : -getResources().getDimension(R.dimen.admin_menubar_size));
        anim.addUpdateListener(animation -> menuLay.setTranslationY((Float) animation.getAnimatedValue()));
        anim.setDuration(300);
        anim.start();
    }

    private void setDetailsVisible(boolean visible) {
        if (isDetailsVisible != visible) {
            isDetailsVisible = visible;
            ValueAnimator anim = ValueAnimator.ofInt(visible ? (int) getResources().getDimension(R.dimen.admin_details_height) : 0, visible ? 0 : (int) getResources().getDimension(R.dimen.admin_details_height));
            anim.addUpdateListener(animation -> mLayoutDetails.setTranslationY((int) animation.getAnimatedValue()));
            anim.setDuration(200);
            anim.start();
        }
    }

    private void downloadLog(List<Profile> selectedProfiles) {
        if (selectedProfiles.size() > 0) {
            setLoadingMode(true);
            User.getWeekViews(this, weekViews -> runOnUiThread(() -> {
                int weekCount = weekViews.size();
                try {
                    File directory = new File(getCacheDir().getAbsolutePath());
                    if (!directory.isDirectory()) //noinspection ResultOfMethodCallIgnored
                        directory.mkdirs();
                    String fileName = "neurossaude_" + Calendar.getInstance().getTime().toString().replace(" ", "_") + ".xls";
                    File file = new File(directory, fileName);
                    WorkbookSettings wbSettings = new WorkbookSettings();
                    wbSettings.setLocale(new Locale(Locale.ENGLISH.getLanguage(), Locale.ENGLISH.getCountry()));
                    WritableWorkbook workbook;
                    workbook = Workbook.createWorkbook(file, wbSettings);
                    WritableSheet sheet = workbook.createSheet("Perfis do Neurossaude", 0);

                    WritableCellFormat formatTitle = new WritableCellFormat();
                    formatTitle.setBackground(Colour.BLUE);
                    formatTitle.setWrap(true);
                    formatTitle.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
                    formatTitle.setAlignment(Alignment.FILL);
                    formatTitle.setFont(new WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE));

                    WritableCellFormat formatData = new WritableCellFormat();
                    formatData.setBackground(Colour.LIGHT_BLUE);
                    formatData.setWrap(true);
                    formatData.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.GRAY_50);
                    formatTitle.setAlignment(Alignment.FILL);
                    formatData.setFont(new WritableFont(WritableFont.ARIAL, 8, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));


                    int c = 0;
                    sheet.addCell(new Label(c++, 0, "Nome", formatTitle));
                    sheet.addCell(new Label(c++, 0, "Email", formatTitle));
                    sheet.addCell(new Label(c++, 0, "Progresso", formatTitle));
                    for (int i = 1; i < weekCount + 1; i++) {
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Video Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Video Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Texto Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Texto Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 1 Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 1 Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 2 Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 2 Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 3 Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 3 Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 4 Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 4 Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 5 Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 5 Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 6 Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio 6 Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio Geral Progresso", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Audio Geral Tempo", formatTitle));
                        sheet.addCell(new Label(c++, 0, "S" + i + ": Progresso", formatTitle));
                    }
                    int row = 1;
                    for (Profile profile : selectedProfiles) {
                        c = 0;
                        sheet.addCell(new Label(c++, row, profile.username, formatData));
                        sheet.addCell(new Label(c++, row, profile.email, formatData));
                        float progressAll = 0;
                        for (Progress p : profile.getProgressWeeks()) progressAll += p.getProgressPercent();
                        sheet.addCell(new Label(c++, row, progressAll / weekCount + " %", formatData));
                        for (Progress progress : profile.getProgressWeeks()) {
                            sheet.addCell(new Label(c++, row, progress.videoProgress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnVideoMedia), formatData));
                            sheet.addCell(new Label(c++, row, progress.textProgress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnTextMedia), formatData));
                            sheet.addCell(new Label(c++, row, progress.audio1Progress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnAudio1Media), formatData));
                            sheet.addCell(new Label(c++, row, progress.audio2Progress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnAudio2Media), formatData));
                            sheet.addCell(new Label(c++, row, progress.audio3Progress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnAudio3Media), formatData));
                            sheet.addCell(new Label(c++, row, progress.audio4Progress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnAudio4Media), formatData));
                            sheet.addCell(new Label(c++, row, progress.audio5Progress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnAudio5Media), formatData));
                            sheet.addCell(new Label(c++, row, progress.audio6Progress + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) progress.millisOnAudio6Media), formatData));
                            int progressAudioAll = (int) ((progress.audio1Progress + progress.audio2Progress + progress.audio3Progress + progress.audio4Progress + progress.audio5Progress + progress.audio6Progress) / 6);
                            int millisAudioAll = (int) ((progress.millisOnAudio1Media + progress.millisOnAudio2Media + progress.millisOnAudio3Media + progress.millisOnAudio4Media + progress.millisOnAudio5Media + progress.millisOnAudio6Media) / 6);
                            sheet.addCell(new Label(c++, row, progressAudioAll + " %", formatData));
                            sheet.addCell(new Label(c++, row, toHour((int) millisAudioAll), formatData));
                        }
                        row++;
                    }

                    workbook.write();
                    workbook.close();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    Uri uriFile = Uri.fromFile(file);
                    StorageReference riversRef = storageRef.child(uriFile.getLastPathSegment());
                    UploadTask uploadTask = riversRef.putFile(uriFile);
                    uploadTask.addOnFailureListener(exception -> {
                        MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Erro ao exportar planilha");
                        setLoadingMode(false);
                    }).addOnSuccessListener(taskSnapshot -> {
                        MessageAlert.create(this, MessageAlert.TYPE_SUCESS, "Sucesso ao exportar planilha, ela estará disponível no Console do Firebase para Download. (Apenas Administradores)");
                        setLoadingMode(false);
                    });
                } catch (IOException | WriteException e) {
                    e.printStackTrace();
                    MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Erro ao exportar planilha");
                    setLoadingMode(false);
                }
            }));
        } else {
            MessageAlert.create(this, MessageAlert.TYPE_ALERT, "Nenhum perfil selecionado");
        }

    }

    private String toHour(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60));
        return String.format(Locale.getDefault(), "%02dm:%02ds", minutes, seconds);
    }
}