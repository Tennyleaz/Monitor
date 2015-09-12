package com.example.tenny.monitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.WriterException;

public class MainActivity extends Activity {
    static final String SERVERIP = "140.113.167.14";
    static final int SERVERPORT = 9000; //8000= echo server, 9000=real server
    private TextView Pname, Pcode, Iname, Icode, connectState, message, serialText;
    private ScrollForeverTextView msg;
    private static ProgressDialog pd;
    private AsyncTask task = null;
    private String str1, productSerial, itemCode;
    private ImageView barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Pname = (TextView) findViewById(R.id.tv2);
        Pcode = (TextView) findViewById(R.id.tv4);
        Iname = (TextView) findViewById(R.id.tv6);
        Icode = (TextView) findViewById(R.id.tv10);
        message = (TextView) findViewById(R.id.textView);
        connectState = (TextView) findViewById(R.id.connectState);
        msg = (ScrollForeverTextView) findViewById(R.id.msg);
        barcode = (ImageView) findViewById(R.id.imageView);

        if(!isNetworkConnected()) {  //close when not connected
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("警告");
            dialog.setMessage("無網路連線,\n程式即將關閉");
            dialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    });
            dialog.show();
            Log.e("Mylog", "no network");
        }
        else {
            pd = ProgressDialog.show(MainActivity.this, "連線中", "Please wait...");    /* 開啟一個新線程，在新線程裡執行耗時的方法 */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InitServer();
                    handler.sendEmptyMessage(0);// 執行耗時的方法之後發送消給handler
                }

            }).start();
        }
        task = new UpdateTask().execute();
    }

    private void InitServer() {
        SocketHandler.closeSocket();
        SocketHandler.initSocket(SERVERIP, SERVERPORT);
        String init = "CONNECT\tFF<END>";
        SocketHandler.writeToSocket(init);
        str1 = SocketHandler.getOutput();
        Log.d("Mylog", str1);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {// handler接收到消息後就會執行此方法
            updateUI();
            pd.dismiss();// 關閉ProgressDialog
        }
    };

    private void updateUI() {
        if(str1.contains("CONNECT_OK")) {
            connectState.setText("伺服器辨識成功");
            connectState.setTextColor(getResources().getColor(R.color.green));
        }
        else {
            connectState.setText(str1);
            connectState.setTextColor(getResources().getColor(R.color.red));
        }
        msg.setText("1234567890wwwwwwwwwwwwwwwwwwwwww1234567890...1234567890wwwwwwwwwwwwwwwwwwwwww1234567890..." +
                "1234567890wwwwwwwwwwwwwwwwwwwwww1234567890...1234567890wwwwwwwwwwwwwwwwwwwwww1234567890...");

        String qrData = "Data I want to encode in QR code";

        try {
            Bitmap bitmap = OneDBarcode.encodeAsBitmap(qrData);
            barcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private class UpdateTask extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... v) {
            //Log.d("Mylog", "UpdateTask listening0...");
            while(!isCancelled()){
                Log.d("Mylog", "UpdateTask listening...");
                String result;
                result = SocketHandler.getOutput();
                publishProgress(result);
                Log.d("Mylog", "result=" + result);
            }
            return null;
        }
        protected void onProgressUpdate(String... values) {
            String result = values[0];
            String[] lines = result.split("<END>");
            for(String s: lines) {
                if(s != null && s.contains("MSG")) {
                    s = s.replaceAll("MSG\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    msg.setText(s);
                }
                else if(s != null && s.contains("LIST")) {
                    s = s.replaceAll("LIST\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    String[] items = s.split("\t");
                    if(items.length >= 4) {
                        Pcode.setText(items[0]);
                        Pname.setText(items[1]);
                        Icode.setText(items[2]);
                        Iname.setText(items[3]);
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("Mylog", "back is pressed");
            finish();
            System.exit(0);
            SocketHandler.closeSocket();
            Log.d("Mylog", "back is pressed2");
            task.cancel(true);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStop(){
        System.exit(0);
        task.cancel(true);
        SocketHandler.closeSocket();
        finish();
        super.onStop();
    }
}
