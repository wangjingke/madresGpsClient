package com.wangjingke.madresgps;

import android.location.Location;
import android.os.Bundle;

import java.io.IOException;

public class LocationListener implements android.location.LocationListener {
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
