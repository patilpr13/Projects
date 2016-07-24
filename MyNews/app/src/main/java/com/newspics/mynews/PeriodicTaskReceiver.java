package com.newspics.mynews;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

public class PeriodicTaskReceiver extends BroadcastReceiver {

    private static final String TAG = "PeriodicTaskReceiver";
    private static final String INTENT_ACTION = "com.newspics.mynews.PERIODIC_TASK_HEART_BEAT";
    private static final String PREFS = "TaskPref";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && !intent.getAction().isEmpty()) {
            MyNews myApplication = (MyNews) context.getApplicationContext();
            SharedPreferences sharedPreferences = myApplication.getSharedPreferences(PREFS,0);

            if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
                sharedPreferences.edit().putBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", false).apply();
                stopPeriodicTaskHeartBeat(context);
            } else if (intent.getAction().equals("android.intent.action.BATTERY_OKAY")) {
                sharedPreferences.edit().putBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", true).apply();
                restartPeriodicTaskHeartBeat(context, myApplication);
            } else if (intent.getAction().equals(INTENT_ACTION)) {
                doPeriodicTask(context, myApplication);
            }
        }
    }

    private void doPeriodicTask(Context context, MyNews myApplication) {
        // Periodic task(s) go here ...
        showNotification(context);
    }

    private void showNotification(Context context) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, SplashActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.news_article)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.news_article))
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(R.string.notification_content))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentIntent);

        Notification notification = mBuilder.build();
        // default phone settings for notifications
       // notification.defaults |= Notification.DEFAULT_VIBRATE;
        //notification.defaults |= Notification.DEFAULT_SOUND;

        // cancel notification after click
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // show scrolling text on status bar when notification arrives
        notification.tickerText = "Breaking news!!!";

        // notifiy the notification using NotificationManager
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

    }

    public void restartPeriodicTaskHeartBeat(Context context, MyNews myApplication) {
        SharedPreferences sharedPreferences = myApplication.getSharedPreferences(PREFS,0);
        boolean isBatteryOk = sharedPreferences.getBoolean("BACKGROUND_SERVICE_BATTERY_CONTROL", true);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        boolean isAlarmUp = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;

        if (isBatteryOk && !isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmIntent.setAction(INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            Calendar firingCal= Calendar.getInstance();
            Calendar currentCal = Calendar.getInstance();

            firingCal.set(Calendar.HOUR_OF_DAY, 7); // At the hour you wanna fire
            firingCal.set(Calendar.MINUTE, 0); // Particular minute
            firingCal.set(Calendar.SECOND, 0); // particular second

            long intendedTime = firingCal.getTimeInMillis();
            long currentTime = currentCal.getTimeInMillis();

            if(intendedTime >= currentTime){
                // you can add buffer time too here to ignore some small differences in milliseconds
                // set from today
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent);
            } else{
                // set from next day
                // you might consider using calendar.add() for adding one day to the current day
                firingCal.add(Calendar.DAY_OF_MONTH, 1);
                intendedTime = firingCal.getTimeInMillis();
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent);
            }
       }
    }

    public void stopPeriodicTaskHeartBeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }
}