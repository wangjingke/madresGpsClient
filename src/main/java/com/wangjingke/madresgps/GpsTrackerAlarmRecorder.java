package com.wangjingke.madresgps;

import android.app.IntentService;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class GpsTrackerAlarmRecorder extends IntentService {
    public GpsTrackerAlarmRecorder() {
        super("GpsTrackerAlarmRecorder");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Handler handler = new Handler();
        Runnable periodicUpdate = new Runnable() {
            @Override
            public void run() {
                Log.i("recorder", "receiveAlarm"+ SystemClock.elapsedRealtime());
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
                        Outlet.writeToCsv("Tracking", new String[]{Encryption.encode(netLoc.toString()), String.valueOf(netLoc.getTime()), CheckNetwork.checkWifi(GpsTrackerAlarmRecorder.this), CheckNetwork.checkNetwork(GpsTrackerAlarmRecorder.this)});
                    } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        handler.post(periodicUpdate);
        GpsTrackerAlarmTrigger.completeWakefulIntent(intent);
    }
}
