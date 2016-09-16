package com.wangjingke.madresgps;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class CheckNetwork {
    public static String checkWifi(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // WiFi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if(wifiInfo!=null && wifiInfo.getNetworkId() == -1 ){
                return "WiFi+-"; // Not connected to an access-Point
            }
            return "WiFi++";      // Connected to an Access Point
        } else {
            return "WiFi--"; // WiFi adapter is OFF
        }
    }

    public static String checkNetwork(Context context) {
        boolean isConnected = false, isWiFi = false;

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }

        if (isConnected) {
            if (isWiFi) {
                return ("Network++");
            } else {
                return ("Network+-");
            }
        } else {
            return("Network--");
        }
    }
}
