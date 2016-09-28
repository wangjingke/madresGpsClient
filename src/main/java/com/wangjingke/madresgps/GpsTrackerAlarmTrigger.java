package com.wangjingke.madresgps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GpsTrackerAlarmTrigger extends BroadcastReceiver {

    @Override
    public void onReceive (final Context context, Intent intent) {
        scheduleExactAlarm(context, (AlarmManager)context.getSystemService(Context.ALARM_SERVICE), intent.getIntExtra("interval", 10));

        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                // record the latest locations from both gps and network if possible
                Location gpsLoc = null, netLoc = null;

                boolean gps_enabled = GpsTrackerAlarm.mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean network_enabled = GpsTrackerAlarm.mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                int satInView = 0;
                int satInUse = 0;
                if (gps_enabled) {
                    gpsLoc = GpsTrackerAlarm.mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    Iterable<GpsSatellite> satellites = GpsTrackerAlarm.mLocationManager.getGpsStatus(null).getSatellites();
                    if (satellites != null) {
                        for (GpsSatellite sat : satellites) {
                            satInView++;
                            if (sat.usedInFix()) {
                                satInUse++;
                            }
                        }
                    }
                }
                if (network_enabled) netLoc = GpsTrackerAlarm.mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (gpsLoc != null) {
                    try {
                        Outlet.writeToCsv("Tracking", new String[]{Encryption.encode(gpsLoc.toString()), String.valueOf(gpsLoc.getTime()), String.valueOf(satInUse), String.valueOf(satInView)});
                    } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }
                if (netLoc != null) {
                    try {
                        Outlet.writeToCsv("Tracking", new String[]{Encryption.encode(netLoc.toString()), String.valueOf(netLoc.getTime()), CheckNetwork.checkWifi(context), CheckNetwork.checkNetwork(context)});
                    } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        handler.post(periodicUpdate);
    }

    public static void scheduleExactAlarm(Context context, AlarmManager alarms, int interval) {
        int refresh_interval = interval;
        Intent i = new Intent(context, GpsTrackerAlarmTrigger.class).putExtra("interval", refresh_interval);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        alarms.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+refresh_interval*1000-SystemClock.elapsedRealtime()%1000, pi);
    }

    public static void cancelAlarm(Context context, AlarmManager alarms) {
        Intent i = new Intent(context, GpsTrackerAlarmTrigger.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        alarms.cancel(pi);
    }
}
