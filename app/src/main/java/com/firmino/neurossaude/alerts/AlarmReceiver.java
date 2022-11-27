package com.firmino.neurossaude.alerts;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.firmino.neurossaude.LoginActivity;
import com.firmino.neurossaude.R;
import com.firmino.neurossaude.views.WeekViewCoinButton;

import java.util.Calendar;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Chegou aqui");
        String text = "";
        int notifyType = context.getSharedPreferences("com.firmino.neurossaude", Context.MODE_PRIVATE).getInt("lastCoinUnlockType", -1);
        switch (notifyType) {
            case WeekViewCoinButton.COIN_TYPE_VIDEO:
                text = "O vídeo da semana está disponível.";
                break;
            case WeekViewCoinButton.COIN_TYPE_AUDIO:
                text = "Prática semanal disponível.";
                break;
        }

        Intent startAppIntent = new Intent(context.getApplicationContext(), LoginActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent onClickIntent = PendingIntent.getActivity(context, 0, startAppIntent, flags);

        int hour = context.getSharedPreferences("com.firmino.neurossaude", Context.MODE_PRIVATE).getInt("hour", 0);
        int minutes = context.getSharedPreferences("com.firmino.neurossaude", Context.MODE_PRIVATE).getInt("minutes", 0);
        String currentHour =  String.format(Locale.getDefault(), "%02dh:%02dmin", hour, minutes);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "neurosaude")
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle("Lembrete" + currentHour)
                .setContentInfo(currentHour)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(onClickIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification.build());
    }

    public static void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(context, AlarmReceiver.class);
        notificationIntent.setAction("android.media.action.DISPLAY_NOTIFICATION");

        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent, flags);
        alarmManager.cancel(broadcast);

        if (context.getSharedPreferences("com.firmino.neurossaude", Context.MODE_PRIVATE).getBoolean("reminderEnabled", false)) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags | PendingIntent.FLAG_IMMUTABLE;
            broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent, flags);
            int hour = context.getSharedPreferences("com.firmino.neurossaude", Context.MODE_PRIVATE).getInt("hour", 0);
            int minutes = context.getSharedPreferences("com.firmino.neurossaude", Context.MODE_PRIVATE).getInt("minutes", 0);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.SECOND, 0);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, broadcast);
        }
    }
}
