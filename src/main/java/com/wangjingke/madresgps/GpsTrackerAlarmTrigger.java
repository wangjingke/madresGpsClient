package com.wangjingke.madresgps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GpsTrackerAlarmTrigger extends WakefulBroadcastReceiver {
    private static final int refresh_interval = 1000*10;
    @Override
    public void onReceive (final Context context, Intent intent) {
        // Intent service = new Intent(context, GpsTrackerAlarmRecorder.class);
        // startWakefulService(context, service);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GpsTrackerWakelock");
        wl.acquire();

        scheduleExactAlarm(context, (AlarmManager)context.getSystemService(Context.ALARM_SERVICE));

        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                //Log.i("recorder", "receiveAlarm"+ SystemClock.elapsedRealtime());
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
        wl.release();
    }

    public static void scheduleExactAlarm(Context context, AlarmManager alarms) {
        Intent i=new Intent(context, GpsTrackerAlarmTrigger.class);
        PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
        //Log.i("trigger", "setAlarm @ "+SystemClock.elapsedRealtime());
        alarms.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+refresh_interval-SystemClock.elapsedRealtime()%1000, pi);
    }

    public static void cancelAlarm(Context context, AlarmManager alarms) {
        Intent i=new Intent(context, GpsTrackerAlarmTrigger.class);
        PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
        alarms.cancel(pi);
    }
}
