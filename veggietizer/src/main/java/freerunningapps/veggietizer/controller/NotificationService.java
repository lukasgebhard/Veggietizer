package freerunningapps.veggietizer.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import freerunningapps.veggietizer.R;
import freerunningapps.veggietizer.model.util.PreferencesAccess;
import freerunningapps.veggietizer.view.ViewConstants;
import freerunningapps.veggietizer.view.activity.InputActivity;
import freerunningapps.veggietizer.view.activity.OverviewActivity;

import java.util.Calendar;

/**
 * A service triggering a notification reminding the user to make an entry.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class NotificationService extends Service {
    private static final int NOTIFICATION_ID_REMINDER = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        /* Unneeded due to setAutoCancel(true):

        Intent onNotificationDeletedIntent = new Intent(getApplicationContext(), OnNotificationDeletedReceiver.class);
        PendingIntent onNotificationDeletedPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                    onNotificationDeletedIntent, 0);*/
        NotificationCompat.Builder reminderBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_statusbar)
                .setContentTitle(getString(R.string.reminder_title))
                .setContentText(getString(R.string.reminder_description))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                //.setDeleteIntent(onNotificationDeletedPendingIntent)
                .setAutoCancel(true);
        Intent inputIntent = new Intent(this , InputActivity.class);
        Intent overviewIntent = new Intent(this , OverviewActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(overviewIntent)
            .addNextIntentWithParentStack(inputIntent);
        PendingIntent inputPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        inputIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        inputIntent.putExtra(ViewConstants.CALLER_ACTIVITY, ViewConstants.OVERVIEW_ACTIVITY);
        reminderBuilder.setContentIntent(inputPendingIntent);
        notificationManager.notify(NOTIFICATION_ID_REMINDER, reminderBuilder.build());

        PreferencesAccess.storeDate(getApplicationContext(), PreferencesAccess.NOTIFICATION_PREFS,
                PreferencesAccess.KEY_DATE_REMINDER_LAST_SHOWN, Calendar.getInstance().getTime());

        stopSelf();
    }

/* Unneeded due to setAutoCancel(true):

  private class OnNotificationDeletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent notificationIntent = new Intent(getApplicationContext(), NotificationService.class);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent notificationPendingIntent
                    = PendingIntent.getService(getApplicationContext(), 0, notificationIntent, 0);

            // Do something here

            notificationPendingIntent.cancel();
        }
    }*/
}
