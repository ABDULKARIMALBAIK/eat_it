package com.example.asus.androideatitserver.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.example.asus.androideatitserver.Common.Common;
import com.example.asus.androideatitserver.Helper.NotificationHelper;
import com.example.asus.androideatitserver.MainActivity;
import com.example.asus.androideatitserver.OrderStatus;
import com.example.asus.androideatitserver.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationAPI26(remoteMessage);
            else
                sendNotification(remoteMessage);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String , String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        //Here we will fix to click to notification => go to Order list
        PendingIntent pendingIntent;
        NotificationHelper helper;
        Notification.Builder builder;

        if (Common.currentUser != null){

            //Here will fix to click to notification -> go to Order list
            Intent intent = new Intent(this , OrderStatus.class);
            intent.putExtra(Common.PHONE_TEXT , Common.currentUser.getPhone());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this , 0 , intent ,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            helper = new NotificationHelper(this);
            builder = helper.getEatItChannelNotification(title,
                    message,
                    pendingIntent,
                    defaultSoundUri);

            //Get random Id for notification to show all notifications
            helper.getManager().notify(new Random().nextInt() , builder.build());
        }
        else {  //Fix crush when notification send from new system (Common.currentUser == null)

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            helper = new NotificationHelper(this);
            builder = helper.getEatItChannelNotification(title, message, defaultSoundUri);

            helper.getManager().notify(new Random().nextInt() , builder.build());
        }


    }

    private void sendNotification(RemoteMessage remoteMessage) {

        Map<String , String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");


        Intent intent = new Intent(this , OrderStatus.class);
        intent.putExtra(Common.PHONE_TEXT , Common.currentUser.getPhone());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0 , intent ,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_start)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0  , builder.build());
    }
}
