package com.seamas.servicefloatingview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        textView.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 10);

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.setAction("rss");
            intent.putExtra("msg", "rss_check");

            PendingIntent pi = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            Log.d("Seamas","setAlarm");

//            JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//            ComponentName componentName = new ComponentName(this, PttRssJobService.class);
//            JobInfo job = new JobInfo.Builder(0, componentName)
//                    .setMinimumLatency(5000)//最小延时 5秒
//                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)//任意网络
//                    .build();
//            //调用schedule
//            mJobScheduler.schedule(job);
        });
    }
}
