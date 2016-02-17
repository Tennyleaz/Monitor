package com.example.tenny.monitor;
/**
 * Created by Tenny on 2015/8/29.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocketHandler {
    private static Socket socket = null;
    private static boolean isCreated = false;
    private static InputStream in = null;
    private static OutputStream out = null;
    private static String ip;
    private static int port;
    private static int disconnectCount = 0;

    public static synchronized Socket getSocket(){
        if(isCreated)
            return socket;
        else
            return null;
    }

    public static synchronized Socket initSocket(String SERVERIP, int SERVERPORT){
        try {
            ip = SERVERIP;
            port = SERVERPORT;
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port));
            isCreated = true;
            in = socket.getInputStream();
            out = socket.getOutputStream();
            disconnectCount = 0;
        }
        catch (UnknownHostException e)
        {
            System.out.println("Error0: UnknownHostException, "+e.getMessage());
            try { LogToServer.getRequest("initSocket:UnknownHostException");
            } catch (Exception exp) { Log.e("mylog", "SocketHandler:LogToServer error"); }
            MainActivity.restart();
        }
        catch(IOException e)
        {
            System.out.println("Error1: IOException, " + e.getMessage());
            try { LogToServer.getRequest("initSocket:IOException");
            } catch (Exception exp) { Log.e("mylog", "SocketHandler:LogToServer error"); }
            MainActivity.restart();
        }
        return socket;
    }

    //public static synchronized void setSocket(Socket socket){
    //    SocketHandler.socket = socket;
    //}

    public static synchronized String getOutput(){
        if(isCreated) {
            String result = "";
            int i;
            List<Byte> buffer = new ArrayList<Byte>();;
            //byte[] buffer = new byte[32768];
            byte[] readbyte = new byte[8192];
            try {
                while((i=in.read(readbyte)) != -1) {
                    for(int j=0; j<i; j++) {
                        buffer.add(readbyte[j]);
                    }
                    //to test if <END> received
                    String s= new String(readbyte, 0, i);
                    readbyte = new byte[8192];
                    Log.d("Mylog", "i=" + i + ", s="+s);
                    if(s.contains("<END>"))
                        break;
                }
            } catch (IOException e) {
                System.out.println("Error getOutput IOException: " + e.getMessage());
                try { LogToServer.getRequest("getOutput:IOException");
                } catch (Exception exp) { Log.e("mylog", "SocketHandler:LogToServer error"); }
                MainActivity.restart();
            }
            result = byteListToString(buffer);
            if (result == null || result.length() == 0) {
                disconnectCount++;
            } else {
                disconnectCount = 0;
            }
            if (disconnectCount >= 10) {
                LogToServer.getRequest("無回應>=10次, need to reboot!");
                Log.e("Mylog", "socket disconnectCount >= 10 !");
                MainActivity.restart();
            }
            return result;
        }
        else {
            Log.e("Mylog", "socket not created, cant get output!");
            try { LogToServer.getRequest("socket not created, cant get output!");
            } catch (Exception exp) { Log.e("mylog", "SocketHandler:LogToServer error"); }
            return null;
        }
    }

    public static synchronized void writeToSocket(String s){
        if(isCreated) {
            try {
                out.write(s.getBytes());
            } catch (IOException e) {
                System.out.println("Error writeToSocket IOException: " + e.getMessage());
                try { LogToServer.getRequest(" writeToSocket:IOException");
                } catch (Exception exp) { Log.e("mylog", "SocketHandler:LogToServer error"); }
                MainActivity.restart();
            }
        }
        else {
            Log.e("Mylog", "socket not created, cant write!");
            try { LogToServer.getRequest(" socket not created, cant write!");
            } catch (Exception exp) { Log.e("mylog", "SocketHandler:LogToServer error"); }
        }
    }

    private static String byteListToString(List<Byte> l) {
        if (l == null) {
            return "";
        }
        byte[] array = new byte[l.size()];
        int i = 0;
        for (Byte current : l) {
            array[i] = current;
            i++;
        }
        String s = null;
        try {
            s = new String(array, "UTF-8");
        }
        catch (UnsupportedEncodingException e){
            Log.e("Mylog", "UnsupportedEncodingException:"+e);
        }
        return s;
    }

    public static synchronized void closeSocket() {
        Log.d("Mylog", "Socket closed");
        if(isCreated) {
            try {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                isCreated = false;
            }
            catch (UnknownHostException e)
            {
                System.out.println("Error3: "+e.getMessage());
            }
            catch(IOException e)
            {
                System.out.println("Error4: " + e.getMessage());
            }
        }
    }
}