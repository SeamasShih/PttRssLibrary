package com.seamas.pttrsslibrary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.Calendar;

public class PttRssUtils {
    public static void requestOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }

    public static void register(Context context, String broad, String filter, int time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PttRssSPParams.PTT, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(PttRssSPParams.BROAD, broad)
                .apply();

        sharedPreferences = context.getSharedPreferences(broad, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(PttRssSPParams.FILTER, filter)
                .putInt(PttRssSPParams.TIME, time)
                .apply();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 10);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.seamas.START_PTT");
        intent.putExtra("msg", "rss_check");
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }

    public static void setBootReciever(Context context, boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PttRssSPParams.PTT, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean(PttRssSPParams.BOOT, b)
                .apply();
    }

    public static boolean getBootSetting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PttRssSPParams.PTT, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PttRssSPParams.BOOT, false);
    }

    public static void unregister(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.seamas.START_PTT");
        intent.putExtra("msg", "rss_check");
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);

        alarmManager.cancel(pi);
    }

    public static PttRssSP getPttRssSP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PttRssSPParams.PTT, Context.MODE_PRIVATE);
        String broad = sharedPreferences.getString(PttRssSPParams.BROAD, "Gossiping");
        sharedPreferences = context.getSharedPreferences(broad, Context.MODE_PRIVATE);

        PttRssSP pttRssSP = new PttRssSP();
        pttRssSP.setBroad(broad);
        pttRssSP.setFilter(sharedPreferences.getString(PttRssSPParams.FILTER, ""));
        pttRssSP.setTitle(sharedPreferences.getString(PttRssSPParams.TITLE, ""));
        pttRssSP.setTime(sharedPreferences.getInt(PttRssSPParams.TIME, 10));

        return pttRssSP;
    }

    public static void refresh(Context context, String broad, String title) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(broad, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(PttRssSPParams.TITLE, title)
                .apply();
    }
}
