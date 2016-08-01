package com.wangjingke.madresgps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GpsTracker extends Service {

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000*5;
    private static final float LOCATION_DISTANCE = 0f;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
            try {
                Outlet.writeToCsv("LocationListener", provider);
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
                Outlet.writeToCsv("ProviderDisabled", provider);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            try {
                Outlet.writeToCsv("ProviderEnabled", provider);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            /*
            try {
                Outlet.writeToCsv("StatusChanged", provider);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
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
            Location gpsLoc=null, netLoc=null, lastLoc=null;
            boolean gps_enabled=false, network_enabled=false;

            gps_enabled=mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled=mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (gps_enabled) gpsLoc=mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (network_enabled) netLoc=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gpsLoc!=null && netLoc!=null){
                if (gpsLoc.getTime()>=netLoc.getTime()) {
                    lastLoc=gpsLoc;
                } else {
                    lastLoc=netLoc;
                }
            }

            if (gpsLoc!=null && netLoc==null) {lastLoc=gpsLoc;}
            if (gpsLoc==null && netLoc!=null) {lastLoc=netLoc;}

            if (lastLoc!=null) {
                try {
                    Outlet.writeToCsv("LocationTracking", Encryption.encode(lastLoc.toString()));
                } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }

            handler.postDelayed(periodicUpdate, 1000*10);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try {
            Outlet.writeToCsv("StartCommand", "Start");
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        try {
            Outlet.writeToCsv("Create", "ServiceCreated");
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.post(periodicUpdate);
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            try {
                Outlet.writeToCsv("Error", "fail to request location update");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException ex) {
            try {
                Outlet.writeToCsv("Error", "network provider does not exist");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            try {
                Outlet.writeToCsv("Error", "fail to request location update");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException ex) {
            try {
                Outlet.writeToCsv("Error", "gps provider does not exist");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            Outlet.writeToCsv("Destroy", "ServiceDestroyed");
        } catch (IOException e) {
            e.printStackTrace();
        }


        super.onDestroy();
        if (mLocationManager != null) {
            handler.removeCallbacks(periodicUpdate);
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    //noinspection MissingPermission
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    try {
                        Outlet.writeToCsv("Error", "fail to remove location listeners");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            Outlet.writeToCsv("Initialize", "initializeLocationManager");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
