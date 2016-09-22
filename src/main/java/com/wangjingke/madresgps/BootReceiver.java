package com.wangjingke.madresgps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String status = preferences.getString("MadresStatus", "");

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && status.equalsIgnoreCase("ON")) {
            Intent pushIntent = new Intent(context, GpsTrackerAlarm.class);
            context.startService(pushIntent);
        }
    }
}
