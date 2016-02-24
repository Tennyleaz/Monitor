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
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.SocketFactory;

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
            socket = SocketFactory.getDefault().createSocket();
            SocketAddress remoteaddr = new InetSocketAddress(ip, port);
            socket.connect(remoteaddr, 5000);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            disconnectCount = 0;
        }
        catch (UnknownHostException e)
        {
            isCreated = false;
            Log.e("mylog", "Error0: UnknownHostException, " + e.getMessage());
            LogToServer.getRequest("initSocket:UnknownHostException");
            MainActivity.restart("initSocket::UnknownHostException");
        }
        catch(IOException e)
        {
            isCreated = false;
            Log.e("mylog", "Error1: IOException, " + e.getMessage());
            LogToServer.getRequest("initSocket:IOException");
            MainActivity.restart("initSocket::IOException");
        }

        try {
            socket.setSoTimeout(0);
        } catch (SocketException e) {
            Log.e("mylog", "Error: SocketException, " + e.getMessage());
            LogToServer.getRequest("initSocket:SocketException");
            MainActivity.restart("initSocket::SocketException");
        }
        isCreated = true;
        return socket;
    }

    //public static synchronized void setSocket(Socket socket){
    //    SocketHandler.socket = socket;
    //}

    public static synchronized String getOutput(){
        if(isCreated) {
            String result = "";
            int i;
            List<Byte> buffer = new ArrayList<Byte>();
            buffer.clear();

            byte[] readbyte = new byte[8192];
            Arrays.fill(readbyte, (byte) 0);
            try {
                while((i=in.read(readbyte)) != -1) {
                    for(int j=0; j<i; j++) {
                        buffer.add(readbyte[j]);
                    }
                    //to test if <END> received
                    String s= new String(readbyte, 0, i);
                    Arrays.fill(readbyte, (byte) 0);
                    Log.d("Mylog", "i=" + i + ", s="+s);
                    if(s.contains("<END>"))
                        break;
                }
                if (i == -1) { //read() returns -1, the peer has closed the connection
                    isCreated = false;
                    Log.e("mylog", "the peer has closed the connection");
                    LogToServer.getRequest("ERROR: the peer has closed the connection");
                    MainActivity.restart("getOutput::read() returns -1");
                }
            } catch (SocketTimeoutException e) {
                Log.e("mylog", "Error getOutput SocketTimeoutException: " + e.getMessage());
                LogToServer.getRequest("getOutput:SocketTimeoutException");
                MainActivity.restart("getOutput::SocketTimeoutException");
            } catch (IOException e) {
                Log.e("mylog", "Error getOutput IOException: " + e.getMessage());
                LogToServer.getRequest("getOutput:IOException");
                MainActivity.restart("getOutput::IOException");
            } catch (Exception e) {
                e.printStackTrace();
                LogToServer.getRequest("writeToSocket:Exception?");
                MainActivity.restart("writeToSocket:Exception?");
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
                MainActivity.restart("getOutput::disconnectCount >= 10");
            }
            return result;
        }
        else {
            Log.e("Mylog", "socket not created, cant get output!");
            LogToServer.getRequest("socket not created, cant get output!");
            MainActivity.restart("getOutput::socket not created");
            return null;
        }
    }

    public static synchronized void writeToSocket(String s){
        if(isCreated) {
            try {
                out.write(s.getBytes());
            } catch (IOException e) {
                System.out.println("Error writeToSocket IOException: " + e.getMessage());
                LogToServer.getRequest("writeToSocket:IOException");
                MainActivity.restart("writeToSocket:IOException");
            } catch (Exception e) {
                e.printStackTrace();
                LogToServer.getRequest("writeToSocket:Exception?");
                MainActivity.restart("writeToSocket:Exception?");
            }
        }
        else {
            Log.e("Mylog", "socket not created, cant write!");
            LogToServer.getRequest(" socket not created, cant write!");
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