package com.seamas.servicefloatingview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.seamas.pttrsslibrary.AlarmReceiver;
import com.seamas.pttrsslibrary.FloatingService;
import com.seamas.pttrsslibrary.PttRssSPParams;
import com.seamas.pttrsslibrary.PttRssUtils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText broad, filter, number;
    private Switch bar;
    private Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PttRssUtils.requestOverlayPermission(this);

        findViews();
        setViews();
        setListener();
    }

    private void setViews() {
        SharedPreferences sp = getSharedPreferences(PttRssSPParams.PTT, MODE_PRIVATE);
        bar.setChecked(sp.getBoolean(PttRssSPParams.ONOFF, false));
        broad.setText(sp.getString(PttRssSPParams.BROAD, ""));
        sp = getSharedPreferences(broad.getText().toString(), Context.MODE_PRIVATE);
        number.setText(String.valueOf(sp.getInt(PttRssSPParams.TIME, 10)));
        filter.setText(sp.getString(PttRssSPParams.FILTER, ""));
    }

    private void setListener() {

        bar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                PttRssUtils.register(this,
                        broad.getText().toString(),
                        filter.getText().toString(),
                        Integer.valueOf(number.getText().toString()));
            else
                PttRssUtils.unregister(this);

            SharedPreferences s = getSharedPreferences(PttRssSPParams.PTT, MODE_PRIVATE);
            s.edit().putBoolean(PttRssSPParams.ONOFF, isChecked).apply();
        });

        test.setOnClickListener(v -> {
            String s = "https://www.ptt.cc/atom/" + broad.getText() + ".xml";

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(s));
            v.getContext().startActivity(intent);
        });
    }

    private void findViews() {
        broad = findViewById(R.id.broad);
        filter = findViewById(R.id.filter);
        number = findViewById(R.id.number);
        bar = findViewById(R.id.bar);
        test = findViewById(R.id.test);
    }
}
