package com.seamas.servicefloatingview;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class PttRssJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("Seamas", "onStartJob");
        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
