package com.example.tenny.monitor;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Medion-PC on 2016/2/3.
 */
public class LogToServer {
    private static String site = "http://192.168.1.250/wlog.php?";
    private static String ID = "CM_1_M";

    public static synchronized void getRequest(String words) throws Exception {
        ID = MainActivity.BOARD_ID;
        URL url = new URL(site + "ID=" + ID + "&LogToServer=" + words);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
    }
}