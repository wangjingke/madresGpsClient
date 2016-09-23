package com.wangjingke.madresgps;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class ControlPanel extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_panel);
        // Check permission for android 6.0+
        if (Build.VERSION.SDK_INT >= 23) {
            CheckPermission.verifyStoragePermissions(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Create app folder
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.d("MyApp", "No SDCARD");
        } else {
            File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "MadresGPS");
            directory.mkdirs();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start study on click
        Button confirm = (Button) findViewById(R.id.confirmButton);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EditText subjectID = (EditText) findViewById(R.id.subjectID);
                TextView notice = (TextView) findViewById(R.id.startNotice);
                RadioGroup mode = (RadioGroup) findViewById(R.id.radioMode);

                String input = subjectID.getText().toString();
                subjectID.setText(""); // clean the input text

                if (CheckID.stop(input)) {
                    stopService(new Intent(ControlPanel.this, GpsTrackerAlarm.class));
                    stopService(new Intent(ControlPanel.this, GpsTrackerWakelock.class));
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ControlPanel.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("MadresStatus", "OFF");
                    editor.apply();
                    try {
                        Outlet.writeToCsv("StopStudy", new String[]{"StudyStopped"});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Handler delay = new Handler();
                    delay.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ControlPanel.this);
                            String madresID = preferences.getString("MadresID", "");
                            Outlet.renameCsv(madresID); // rename the data file
                        }
                    }, 2000); // delay 2 sec to allow final writing of stopstudy to csv
                    notice.setText("The study is over, and the GPS tracking is terminated.");
                } else if (CheckID.start(input)) {
                    if (CheckID.extractID(input).equals("invalid ID")) {
                        notice.setText("Invalid ID, try again");
                    } else {
                        // write subjectID and status to shared preference
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ControlPanel.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("MadresID", CheckID.extractID(input));
                        editor.putString("MadresStatus", "ON");
                        editor.apply();
                        // write selected mode to shared preference
                        int modeId = mode.getCheckedRadioButtonId();
                        RadioButton chosenMode = (RadioButton) findViewById(modeId);
                        editor.putString("MadresMode", chosenMode.getText().toString());
                        editor.apply();
                        // start service
                        if (chosenMode.getText().toString().equals("Wake Timer")) {
                            stopService(new Intent(ControlPanel.this, GpsTrackerWakelock.class));
                            startService(new Intent(ControlPanel.this, GpsTrackerAlarm.class));
                            try {
                                Outlet.writeToCsv("SubjectID", new String[]{CheckID.extractID(input)});
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            notice.setText("The participant ID is set as " + CheckID.extractID(input) + ", and the study is running.");
                        } else if (chosenMode.getText().toString().equals("Wake Lock")){
                            stopService(new Intent(ControlPanel.this, GpsTrackerAlarm.class));
                            startService(new Intent(ControlPanel.this, GpsTrackerWakelock.class));
                            try {
                                Outlet.writeToCsv("SubjectID", new String[]{CheckID.extractID(input)});
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            notice.setText("The participant ID is set as " + CheckID.extractID(input) + ", and the study is running.");
                        }
                    }
                } else if (CheckID.clean(input)) {
                    Outlet.delete();
                    notice.setText("All the stored data are wiped clean");
                } else {
                    notice.setText("Invalid input");
                }
            }
        });
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, AboutThisApp.class);
        startActivity(intent);
    }
}
