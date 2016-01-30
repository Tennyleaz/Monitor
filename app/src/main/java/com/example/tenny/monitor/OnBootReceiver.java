package com.example.tenny.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Tenny on 2015/11/26.
 */
public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /*if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent myActivity = new Intent(context, MainActivity.class);
            myActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myActivity);
        }*/
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        if (isConnected) {
            Intent myActivity = new Intent(context, MainActivity.class);
            myActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(myActivity);
        }
    }
}
