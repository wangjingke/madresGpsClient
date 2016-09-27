package com.wangjingke.madresgps;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class ForegroundNotification {

    static android.app.Notification run (Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String madresID = preferences.getString("MadresID", "");
        String mode = preferences.getString("MadresMode", "");
        int interval = preferences.getInt("MadresInterval", 10);
        android.app.Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(madresID + ", thank you!")
                .setContentText(mode + " mode, at " + interval + " sec interval")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .build();

        return notification;
    }

}
