package com.wangjingke.madresgps;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GpsTracker extends Service {

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0f;

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

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    Handler handler = new Handler();
    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            // record the lastest locations from both gps and network if possible
            long current = System.currentTimeMillis();
            if ((current-current%1000)%(1000*10)  == 0) { //record every 10 sec
                Location gpsLoc=null, netLoc=null;

                boolean gps_enabled=mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean network_enabled=mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                int satInView = 0;
                int satInUse = 0;
                if (gps_enabled) {
                    gpsLoc=mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    Iterable<GpsSatellite> satellites = mLocationManager.getGpsStatus(null).getSatellites();
                    if (satellites != null) {
                        for (GpsSatellite sat : satellites) {
                            satInView++;
                            if (sat.usedInFix()) {
                                satInUse++;
                            }
                        }
                    }
                }
                if (network_enabled) netLoc=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (gpsLoc!=null) {
                    try {
                        Outlet.writeToCsv("Tracking", new String[]{Encryption.encode(gpsLoc.toString()), String.valueOf(gpsLoc.getTime()), String.valueOf(satInUse), String.valueOf(satInView)});
                    } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }
                if (netLoc!=null) {
                    try {
                        Outlet.writeToCsv("Tracking", new String[]{Encryption.encode(netLoc.toString()), String.valueOf(netLoc.getTime()), CheckNetwork.checkWifi(GpsTracker.this)});
                    } catch (IOException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }
            }

            // alarm fires off every sec to keep the phone awake
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MILLISECOND, 1000);
            Intent notificationIntent = new Intent(GpsTracker.this, GpsTracker.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(GpsTracker.this, 0, notificationIntent, 0);
            AlarmManager keepAwake = (AlarmManager) getSystemService(ALARM_SERVICE);
            keepAwake.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            handler.postDelayed(periodicUpdate, 1000);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try {
            Outlet.writeToCsv("StartCommand", new String[]{"Start"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStartCommand(intent, flags, startId);
        /*
        // initiate wake lock to keep device awake
        PowerManager powerManager = (PowerManager) getSystemService(GpsTracker.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakelockTag");
        wakeLock.acquire();
        */
        /*
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle("madresGPS running ...");
        builder.setSmallIcon(R.drawable.ic_android_black_24dp);
        startForeground(337, builder.build());
        */
        // show notification on screen and run the service on foreground to avoid standby
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GpsTracker.this);
        String madresID = preferences.getString("MadresID", "");
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(madresID + ", thank you!")
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
        handler.post(periodicUpdate);

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
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try {
            Outlet.writeToCsv("Destroy", new String[]{"ServiceDestroyed"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mLocationManager != null) {
            handler.removeCallbacks(periodicUpdate);
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
            Outlet.writeToCsv("Initialize", new String[]{"initializeLocationManager"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class GpsListener implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
        }
    }

}
