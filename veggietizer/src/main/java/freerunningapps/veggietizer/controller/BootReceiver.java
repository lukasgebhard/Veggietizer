package freerunningapps.veggietizer.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import freerunningapps.veggietizer.model.util.PreferencesAccess;

import java.util.Calendar;
import java.util.Date;

/**
 * Delivers notifications to the Android system approx. each three days at around 1:00 p.m.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class BootReceiver extends BroadcastReceiver {
    public static final int ALARM_REQUEST_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notifyIntent = new Intent(context.getApplicationContext() , NotificationService.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmPendingIntent = PendingIntent.getService(context.getApplicationContext(),
                ALARM_REQUEST_ID, notifyIntent, 0);

        Calendar longAgo = Calendar.getInstance();
        longAgo.set(Calendar.YEAR, 1990);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.roll(Calendar.DAY_OF_MONTH, -3);

        Date threeDaysAgoDate = calendar.getTime();
        Date lastAppLaunchDate = PreferencesAccess.readDate(context, PreferencesAccess.NOTIFICATION_PREFS,
                PreferencesAccess.KEY_DATE_LAST_APP_LAUNCH);
        Date reminderLastShownDate = PreferencesAccess.readDate(context, PreferencesAccess.NOTIFICATION_PREFS,
                PreferencesAccess.KEY_DATE_REMINDER_LAST_SHOWN);

        if (lastAppLaunchDate == null) {
            lastAppLaunchDate = longAgo.getTime();
        }
        if (reminderLastShownDate == null) {
            reminderLastShownDate = longAgo.getTime();
        }

        boolean isTodayAlarmDay = threeDaysAgoDate.compareTo(lastAppLaunchDate) >= 0
                && threeDaysAgoDate.compareTo(reminderLastShownDate) >= 0;

        if (isTodayAlarmDay) {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(System.currentTimeMillis());
            alarmTime.set(Calendar.HOUR_OF_DAY, 13); // Alarm to be triggered at approx. 1:00 p.m.
            alarmManager.set(AlarmManager.RTC, alarmTime.getTimeInMillis(), alarmPendingIntent);
        }
    }
}
