package com.wangjingke.madresgps;

import android.os.SystemClock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.opencsv.CSVWriter;

public class Outlet {
    public static String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"MadresGPS";

    public static String fileNameGPS = "MadresGpsTracking.csv";
    public static String filePathGPS = baseDir + File.separator + fileNameGPS;
    public static File recordGPS = new File(filePathGPS);

    public static void writeToCsv(String prefix, String[] text) throws IOException {
        Date timestamp = new Date();

        CSVWriter writer;
        // File exist
        if(recordGPS.exists() && !recordGPS.isDirectory()){
            FileWriter subjectIdList = new FileWriter(filePathGPS, true);
            writer = new CSVWriter(subjectIdList);
        }
        else {
            writer = new CSVWriter(new FileWriter(filePathGPS));
        }

        List<String> data = new ArrayList<String>();
        data.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp));
        data.add(prefix);
        for (String element : text) {
            data.add(element);
        }
        writer.writeNext(data.toArray(new String[0]));
        writer.close();
    }

    public static void renameCsv(String subjectID) {
        String dataGPSpath = baseDir + File.separator + "MadresGpsTracking_" + subjectID + "_" + System.currentTimeMillis() + ".csv";
        File dataGPS = new File(dataGPSpath);
        recordGPS.renameTo(dataGPS);
    }


    public static void delete() {
        File base = new File(baseDir);
        if(base.exists() && base.isDirectory()){
            String[] children = base.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(base, children[i]).delete();
            }
        }
    }
}
