package com.seamas.pttrsslibrary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (PttRssUtils.getBootSetting(context) && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            PttRssSP pttRssSP = PttRssUtils.getPttRssSP(context);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, pttRssSP.getTime());

            Intent in = new Intent(context, AlarmReceiver.class);
            in.setAction("rss");
            in.putExtra("msg", "rss_check");

            PendingIntent pi = PendingIntent.getBroadcast(context, 1, in, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }
}
