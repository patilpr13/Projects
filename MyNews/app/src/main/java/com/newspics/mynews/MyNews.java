package com.newspics.mynews;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by Abhi on 3/22/2016.
 */

@ReportsCrashes(formUri = "http://newspics.in/",
        mailTo = "info@newspics.in",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class MyNews extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.setProperty("http.keepAlive", "false");
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent startServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        startService(startServiceIntent);
    }
}
