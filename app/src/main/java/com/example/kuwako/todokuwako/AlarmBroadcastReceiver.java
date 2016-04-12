package com.example.kuwako.todokuwako;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by kuwako on 2016/04/11.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "Received", Toast.LENGTH_LONG).show();
        int bid = intent.getIntExtra("intentId", 0);
        Intent intent2 = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, bid, intent2, 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.calendar)
                .setTicker("Time has come.")
                .setWhen(System.currentTimeMillis())
                .setContentTitle("時間ですよー")
                .setContentText("なう")
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.cancelAll();
        notificationManager.notify(R.string.app_name, notification);
    }
}
