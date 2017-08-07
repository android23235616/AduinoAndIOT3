package com.elabs.aduinoandiot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Tanmay on 07-08-2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    SharedPreferences sharedPreferences;
    String name = "";
    @Override
    public void onMessageReceived(RemoteMessage message){
        sharedPreferences = getSharedPreferences(Constants.sharedPreferenceConstant, Context.MODE_PRIVATE);
        name = sharedPreferences.getString("name","");
        sendMessage(message.getNotification().getBody());
    }

    private void sendMessage(String body) {
        Intent i = new Intent(this, BufferActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Hello, "+name).setContentText(body).setAutoCancel(true).setContentIntent(pi);

        NotificationManager notificationManagere = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notifi = notificationBuilder.build();
        notifi.sound = Uri.parse("android.resource://"
                + this.getPackageName() + "/" + R.raw.not);
        long[] vibrate = { 0, 100, 200, 300 };
        notifi.vibrate = vibrate;
        notificationManagere.notify(0, notifi);
    }
}
