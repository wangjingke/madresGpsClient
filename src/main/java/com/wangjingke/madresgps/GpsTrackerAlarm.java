package com.wangjingke.madresgps;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

public class GpsTrackerAlarm extends Service {

    public static LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000*3;
    private static final float LOCATION_DISTANCE = 0f;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try {
            Outlet.writeToCsv("StartCommand", new String[]{"Start under Wake Timer mode"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStartCommand(intent, flags, startId);

        // show notification on screen and run the service on foreground to avoid standby
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GpsTrackerAlarm.this);
        String madresID = preferences.getString("MadresID", "");
        String mode = preferences.getString("MadresMode", "");
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(madresID + ", thank you!")
                .setContentText(mode + " mode")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .build();
        startForeground(337,  notification);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        try {
            Outlet.writeToCsv("Create", new String[]{"ServiceCreated"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            try {
                Outlet.writeToCsv("Error", new String[]{"fail to request location update"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException ex) {
            try {
                Outlet.writeToCsv("Error", new String[]{"network provider does not exist"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            try {
                Outlet.writeToCsv("Error", new String[]{"fail to request location update"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException ex) {
            try {
                Outlet.writeToCsv("Error", new String[]{"gps provider does not exist"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GpsTrackerAlarmTrigger.scheduleExactAlarm(GpsTrackerAlarm.this, (AlarmManager) getSystemService(ALARM_SERVICE));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        GpsTrackerAlarmTrigger.cancelAlarm(this, (AlarmManager)getSystemService(ALARM_SERVICE));
        try {
            Outlet.writeToCsv("Destroy", new String[]{"ServiceDestroyed"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    //noinspection MissingPermission
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    try {
                        Outlet.writeToCsv("Error", new String[]{"fail to remove location listeners"});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void initializeLocationManager() {
        GpsListener checkSatellite = new GpsListener();
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.addGpsStatusListener(checkSatellite);
        }
        try {
            Outlet.writeToCsv("Initialize", new String[]{"InitializeLocationManager"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GpsListener implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
            try {
                Outlet.writeToCsv("LocationListener", new String[]{provider});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            try {
                Outlet.writeToCsv("ProviderDisabled", new String[]{provider});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            try {
                Outlet.writeToCsv("ProviderEnabled", new String[]{provider});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            //Outlet.writeToCsv("StatusChanged", provider);
        }
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
}
