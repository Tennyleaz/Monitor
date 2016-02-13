package com.example.tenny.monitor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Medion-PC on 2016/2/3.
 */
public class LogToServer {

    private static final String site = "http://140.113.167.14/wlog.php?";
    private static URL url = null;
    private static String mySite = null;

    public static synchronized void getRequest(String words) {
        final String ID = MainActivity.BOARD_ID;

        try {
            mySite = site + "ID="+ ID + "&Log=" + URLEncoder.encode(words + "\n", "UTF-8");
            url = new URL(mySite);
        } catch (Exception e) {
            Log.e("logtoserver", "Unable write log ! ");
            e.printStackTrace();
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Log.d("logtoserver", "mysite=" + mySite);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    rd.close();
                } catch (Exception e) {
                    Log.e("logtoserver", "Unable send log ! ");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}
