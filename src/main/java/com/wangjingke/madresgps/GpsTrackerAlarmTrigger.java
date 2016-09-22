package com.wangjingke.madresgps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GpsTrackerAlarmTrigger extends WakefulBroadcastReceiver {
    @Override
    public void onReceive (Context context, Intent intent) {
        Intent service = new Intent(context, GpsTrackerAlarmRecorder.class);
        startWakefulService(context, service);
        scheduleExactAlarm(context, (AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
    }

    public static void scheduleExactAlarm(Context context, AlarmManager alarms) {
        Intent i=new Intent(context, GpsTrackerAlarmTrigger.class);
        PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
        alarms.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+1000*10-SystemClock.elapsedRealtime()%1000, pi);
    }

    public static void cancelAlarm(Context context, AlarmManager alarms) {
        Intent i=new Intent(context, GpsTrackerAlarmTrigger.class);
        PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
        alarms.cancel(pi);
    }
}
