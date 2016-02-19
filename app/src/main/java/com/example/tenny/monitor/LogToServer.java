package com.example.tenny.monitor;

import android.util.Log;
//
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

//
///**
// * Created by Medion-PC on 2016/2/3.
// */
//public class LogToServer {
//
//    private static final String site = "http://140.113.167.14/wlog.php?";//"http://192.168.1.250/wlog.php?";//
//    private static URL url = null;
//    private static int logcount = 0;
//
//
//    public static synchronized void getRequest(final String words) {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    final String ID = MainActivity.BOARD_ID;
//                    String mySite = null;
//                    if(words == null)
//                        return;
//                    if(words.contains("分行:UPDATE"))
//                        return;
//                    String words2 = words.replaceAll("<", "&lt;");
//                    words2 = words2.replaceAll(">", "&gt;");
//                    words2 = Integer.toString(logcount) + words2;
//                    logcount++;
//                    if(logcount>1000) logcount=0;
//                    Log.d("logtoserver", words2);
//                    try {
//                        mySite = site + "ID="+ ID + "&Log=" + URLEncoder.encode(words2 + "\n", "UTF-8");
//                        url = new URL(mySite);
//                    } catch (Exception e) {
//                        Log.e("logtoserver", "Unable write log ! ");
//                        e.printStackTrace();
//                    }
//                    Log.d("logtoserver", "mysite=" + url.toString());
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setRequestMethod("GET");
//                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                    rd.close();
//                    Log.d("logtoserver", "thread finished");
//                } catch (Exception e) {
//                    Log.e("logtoserver", "Unable send log ! ");
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
//    }
//}
public class LogToServer {
    //private static int logcount = 0;

    public static synchronized void getRequest(String words) {
        final String site = "http://192.168.1.250/wlog.php?";//"http://140.113.167.14/wlog.php?";
        final String ID = MainActivity.BOARD_ID;
        //if (words.contains("分行:UPDATE")) return;
        //words = words.replaceAll("<END>", "");
        //words = words.replaceAll("<N>", "&nbsp;");
        words = words.replaceAll("<", "&lt;");
        words = words.replaceAll(">", "&gt;");
        final String msg = words;
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (LogToServer.class) {
                    try {
                        String mySite = site + "ID=" + ID + "&Log=" + URLEncoder.encode(msg + "\n", "UTF-8");
                        //Log.d("logtoserver", "words=" + logcount + msg);
                        //Log.d("logtoserver", "mySite=" + mySite);
                        //logcount++;
                        URL url = new URL(mySite);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        rd.close();
                    } catch (Exception e) {
                        android.util.Log.e("MyLog", "Unable write log ! ");
                    }
                }
            }
        }).start();
    }
}