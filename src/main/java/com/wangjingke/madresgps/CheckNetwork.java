package com.wangjingke.madresgps;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class CheckNetwork {
    public static String checkWifi(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // WiFi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if(wifiInfo!=null && wifiInfo.getNetworkId() == -1 ){
                return "WifiOnNotConnected"; // Not connected to an access-Point
            }
            return "WifiConnected";      // Connected to an Access Point
        } else {
            return "WifiOff"; // WiFi adapter is OFF
        }
    }
}
