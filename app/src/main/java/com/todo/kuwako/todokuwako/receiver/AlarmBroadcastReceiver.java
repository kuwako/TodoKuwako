package com.todo.kuwako.todokuwako.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.todo.kuwako.todokuwako.R;
import com.todo.kuwako.todokuwako.activity.MainActivity;
import com.todo.kuwako.todokuwako.contract.PreferenceContract;

/**
 * Created by kuwako on 2016/04/11.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int bid = intent.getIntExtra("intentId", 0);

        Intent intent2 = new Intent(context, MainActivity.class);
        intent2.putExtra("todoId", intent.getIntExtra("id", 0));
        intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, bid, intent2, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Integer vibrateType = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceContract.Vibrate.KEY, "2"));

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_app)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setTicker("タスクの時間だよ")
                .setWhen(System.currentTimeMillis())
                .setContentTitle(intent.getStringExtra("deadline"))
                .setContentText(intent.getStringExtra("task"))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setVibrate(PreferenceContract.Vibrate.TYPES[vibrateType])
                .build();

        notificationManager.notify(intent.getIntExtra("id", 0), notification);
    }

}
