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
        String mode = preferences.getString("MadresMode", "");
        Intent pushIntent = null;
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && status.equalsIgnoreCase("ON")) {
            if (mode.equalsIgnoreCase("Wake Lock")) {
                pushIntent = new Intent(context, GpsTrackerWakelock.class);
            } else if (mode.equalsIgnoreCase("Wake Timer")) {
                pushIntent = new Intent(context, GpsTrackerAlarm.class);
            }
            context.startService(pushIntent);
        }
    }
}
