package com.example.kuwako.todokuwako.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.kuwako.todokuwako.R;
import com.example.kuwako.todokuwako.activity.MainActivity;

import java.util.Iterator;

/**
 * Created by kuwako on 2016/04/11.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("@@@", "AlarmBroadcastReceiver:onReceive");
        int bid = intent.getIntExtra("intentId", 0);
        Bundle extras = intent.getExtras();
        StringBuilder sb = new StringBuilder();
        if (extras != null) {
            Iterator<?> it = extras.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                Log.d("@@@IntentReceive", "key: " + key);
                Log.d("@@@alarm",  String.valueOf(intent.getIntExtra(key, 0)));
                Log.d("@@@alarm",  String.valueOf(intent.getDoubleExtra(key, 0)));
                Log.d("@@@alarm",  String.valueOf(intent.getStringExtra(key)));

            }
        } else {
            Log.d("@@@IntentReceive", "key: noKeys");
        }
        // TODO　ここ機能してない
        Intent intent2 = new Intent(context, MainActivity.class);
        intent2.putExtra("todoId", intent.getDoubleExtra("id", 0));
        intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, bid, intent2, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_app)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setTicker("タスクの時間だよ")
                .setWhen(System.currentTimeMillis())
                .setContentTitle(intent.getStringExtra("deadline"))
                .setContentText(intent.getStringExtra("task"))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 1000, 300, 2000})
                .setAutoCancel(true)
                .build();

        notificationManager.notify(intent.getIntExtra("id", 0), notification);
        Log.e("@@@notify_id: ", String.valueOf(intent.getIntExtra("id", 0)));
        Log.e("@@@notify_id2: ", String.valueOf(intent2.getIntExtra("todoId", 0)));
    }
}
