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
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {
    static final String SERVERIP = "140.113.167.14";
    static final int SERVERPORT = 9000; //8000= echo server, 9000=real server
    static final int SEEK_DEST = 95;
    private TextView connectState, swapTitle, brandName;
    private ScrollForeverTextView msg;
    private static ProgressDialog pd;
    private AsyncTask task = null;
    private String str1, bname;
    private SeekBar mySeekBar;
    private boolean connected, swapWorking, swapEnd;
    private ListView mylist, firstlist;
    private ArrayAdapter<String> listAdapter;
    private MySimpleArrayAdapter myAdapter;
    //private ArrayAdapter<String> listAdapter01;
    private ArrayList<ListItem> List_file;
    private int connectionTimeoutCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectState = (TextView) findViewById(R.id.connectState);
        msg = (ScrollForeverTextView) findViewById(R.id.msg);
        mylist = (ListView) findViewById(R.id.listView);
        mySeekBar = (SeekBar) findViewById(R.id.myseek);
        mySeekBar.setEnabled(false);
        swapTitle = (TextView) findViewById(R.id.swapTitle);
        swapTitle.setText("目前無換牌指令");
        connected = false;
        swapWorking = false;
        swapEnd = false;
        //countTV = (TextView) findViewById(R.id.itemCount);.
        brandName = (TextView) findViewById(R.id.brandName);
        firstlist = (ListView) findViewById(R.id.listView2);
        connectionTimeoutCount = 0;

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
            new Thread(new Runnable() {
                @Override
                public void run() {  // 需要背景作的事
                    try {
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(1000);
                        }
                        if(!connected) {
                            Log.e("Mylog", "1000ms timeout");
                            ServerDownHandler.sendEmptyMessage(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        task = new UpdateTask().execute();
    }

    private void InitServer() {
        SocketHandler.closeSocket();
        SocketHandler.initSocket(SERVERIP, SERVERPORT);
        String init = "CONNECT\tCM_1_M<END>";
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

    private Handler ServerDownHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {// handler接收到消息後就會執行此方法
            pd.dismiss();// 關閉ProgressDialog
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("警告");
            dialog.setMessage("伺服器無回應，程式即將關閉\n請嘗試重新連線或洽系統管理員");
            dialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    });
            dialog.show();
        }
    };

    private void updateUI() {
        if(str1.contains("CONNECT_OK")) {
            connectState.setText("伺服器辨識成功");
            connectState.setTextColor(getResources().getColor(R.color.green));
            connected = true;
        }
        else {
            connectState.setText(str1);
            connectState.setTextColor(getResources().getColor(R.color.red));
            connected = false;
        }
        msg.setText("1234567890wwwwwwwwwwwwwwwwwwwwww1234567890...1234567890wwwwwwwwwwwwwwwwwwwwww1234567890..." +
                "1234567890wwwwwwwwwwwwwwwwwwwwww1234567890...1234567890wwwwwwwwwwwwwwwwwwwwww1234567890...");

        /*String qrData = "Data I want to encode in QR code";
        try {
            Bitmap bitmap = OneDBarcode.encodeAsBitmap(qrData, 400, 150);
            barcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }*/

        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {  //結束拖動時觸發
                if(seekBar.getProgress() > SEEK_DEST) {
                    //TODO: apply change brand
                    if(List_file != null)
                        List_file.clear();
                    myAdapter.notifyDataSetChanged();
                    swapTitle.setText("目前無換牌指令");
                    swapTitle.setTextColor(getResources().getColor(R.color.dark_gray));
                    seekBar.setProgress(5);
                    seekBar.setEnabled(false);
                    swapEnd = true;
                    bname = "";
                    brandName.setText(bname);
                    if(task != null)
                        task.cancel(true);
                    task = new UpdateTask().execute();
                    Log.d("Mylog", "swap end.");
                    //swapWorking = false;
                } else {
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.slider, null));
                    seekBar.setProgress(5);  //go back to zero
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {  /* 開始拖動時觸發*/  }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  //進度改變時觸發  只要在拖動，就會重複觸發
                if(progress > SEEK_DEST)
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.slider_ok, null));
                else
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.slider, null));
            }
        });

        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        listAdapter.add("111");
        listAdapter.add("222");
        listAdapter.add("333");
        mylist.setAdapter(listAdapter);
    }

    private class UpdateTask extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... v) {
            //Log.d("Mylog", "UpdateTask listening0...");
            while(!isCancelled()) {
                if(swapEnd) {
                    SocketHandler.writeToSocket("SWAP_OK<END>");
                    swapWorking = false;
                    swapEnd = false;
                    Log.d("Mylog", "swapWorking -> false");
                    continue;
                }
                if(swapWorking) {
                    Log.d("Mylog", "break!");
                    break;
                }
                if(connectionTimeoutCount >= 11)
                    break;

                Log.d("Mylog", "UpdateTask listening...");
                String result;
                result = SocketHandler.getOutput();
                publishProgress(result);
                Log.d("Mylog", "result=" + result);
                if (result == null || result.isEmpty() || result.equals(""))
                    connectionTimeoutCount++;
                else
                    connectionTimeoutCount = 0;
                //
                try {
                    Thread.sleep(90);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        protected void onProgressUpdate(String... values) {
            if(connectionTimeoutCount >= 10) {
                Log.e("Mylog", "connect failed!");
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("警告");
                dialog.setMessage("伺服器無回應，程式即將關閉\n請嘗試重新連線或洽系統管理員");
                dialog.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        });
                dialog.show();
                return;
            }

            String result = values[0];
            if(result.length() == 0) return;
            String[] lines = result.split("<END>");
            int length = lines.length;

            //Log.d("Mylog", "lines.length=" + length);
            boolean updateList = false;
            for(String s: lines) {
                if(s != null && s.contains("MSG\t")) {
                    s = s.replaceAll("MSG\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    msg.setText(s);
                } else if(s!=null && s.contains("UPDATE_LIST\t")) {
                    s = s.replaceAll("UPDATE_LIST\t", "");
                    String[] items = s.split("\t");
                    if(items.length >= 2 && List_file != null) {
                        //TODO: update list
                        for(int j=0; j<List_file.size(); j++) {
                            if(List_file.get(j).productSerial.equals(items[0])) {
                                List_file.get(j).itemCount = items[1];
                                //Log.d("mylog", "becomes: " + List_file.get(j).itemCount);
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else if(s != null && s.contains("LIST\t")) {
                    if(List_file != null) {
                        List_file.clear();
                        List_file = new ArrayList<ListItem>();
                    } else
                        List_file = new ArrayList<ListItem>();
                    s = s.replaceAll("LIST\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    String[] items = s.split("\n");
                    for(String i: items){
                        Log.d("Mylog",", line i=" + i);
                        String[] single_item = i.split("\t");
                        String barcode_text = "";
                        if(single_item.length >= 4) {
                            //TODO: add pics
                            updateList = true;
                            barcode_text = single_item[2];
                            bname = single_item[1];
                            brandName.setText(bname);
                            Log.d("Mylog", "barcode_text=" + barcode_text);
                            Bitmap bitmap = null;
                            try {
                                bitmap = OneDBarcode.encodeAsBitmap(barcode_text, 450, 100);
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                            ListItem singleItem = new ListItem(single_item[3], single_item[2], "0.0", bitmap);
                            List_file.add(singleItem);
                        }
                    }

                } else if(s!=null && s.contains("SWAP")) {
                    //s = s.replaceAll("SWAP\t", "");
                    swapWorking = true;
                    Log.d("Mylog", "swap!!");
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("警告");
                    dialog.setMessage("已下達換牌指令！");
                    dialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    mySeekBar.setEnabled(true);
                                    swapTitle.setText("向右滑動切換品牌");
                                    swapTitle.setTextColor(getResources().getColor(R.color.black));
                                    swapWorking = true;
                                    task.cancel(true);
                                }
                            });
                    Log.d("Mylog", "prepare to show dialog...");
                    dialog.show();
                }
            }
            if(updateList) {
                myAdapter = new MySimpleArrayAdapter(MainActivity.this, List_file);
                firstlist.setAdapter(myAdapter);
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
