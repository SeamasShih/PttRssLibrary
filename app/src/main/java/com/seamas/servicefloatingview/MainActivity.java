package com.seamas.servicefloatingview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.seamas.pttrsslibrary.AlarmReceiver;
import com.seamas.pttrsslibrary.FloatingService;
import com.seamas.pttrsslibrary.PttRssUtils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Switch textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PttRssUtils.requestOverlayPermission(this);

        textView = findViewById(R.id.text);
        textView.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked)
//                PttRssUtils.register(this, "Gossiping", "", 10);
//            else
//                PttRssUtils.unregister(this);

            Intent intent = new Intent();
            intent.setClass(this, FloatingService.class);
            startService(intent);
        });
    }
}
