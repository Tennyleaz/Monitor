package com.example.tenny.monitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {
    static final String SERVERIP = "192.168.1.250";//"140.113.167.14";//"192.168.1.30";
    static final int SERVERPORT = 9000; //8000= echo server, 9000=real server
    static final int SEEK_DEST = 95;
    static final int MAX_LINE = 9;
    static String BOARD_ID = "CM_1_M";

    private TextView connectState, swapTitle, brandName, swapMsg, workerID;
    private ScrollForeverTextView msg;
    private static ProgressDialog pd;
    private AsyncTask task = null;
    private String str1, bname, returnWorkerID, tempSwapMessage, key;
    private SeekBar mySeekBar;
    private boolean connected, swapWorking, swapEnd, bc_msg_reply, bc_msgWorking, notOnstop=false, swap_msgWorking=false, swap_msg_reply=false;
    private ListView firstlist, valueListView, boxListView;
    private MySimpleArrayAdapter myAdapter;
    private ValueAdapter valueAdapter;
    private ArrayList<ValueItem> valueArray;
    private BoxAdapter boxAdapter;
    private ArrayList<BoxItem> boxArray;
    private ArrayList<String> nextBrandArray;
    private ArrayAdapter<String> nextBrandAdapter;
    private ArrayList<ListItem> List_file;
    private int connectionTimeoutCount;
    private Spinner brandSelector;
    private Button n1, n2, n3, n4, n5, n6, n7,n8, n9, n0, btn_enter, btn_delete;
    private TabHost tabHost;
    AlertDialog dialog;
    static boolean active = false;
    private static int rebootCount;
    private int returnBrandName;
    private HashMap<String, String> recipe_map;
    private static Context staticContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        staticContext = getApplicationContext();
        final SharedPreferences settings = getApplicationContext().getSharedPreferences("EC510", 0);
        BOARD_ID = settings.getString("board_name", "CM") + "_" + settings.getString("board_ID", "1") + "_M";
        Log.d("mylog", "BOARD_ID=" + BOARD_ID);
        TextView board_id = (TextView) findViewById(R.id.board_id);
        if(BOARD_ID.contains("CM"))
            board_id.setText("捲菸機" + settings.getString("board_ID", "1"));
        else if(BOARD_ID.contains("PM"))
            board_id.setText("包裝機" + settings.getString("board_ID", "1"));
        else
            board_id.setText("裝箱機" + settings.getString("board_ID", "1"));

        Thread[] threads = new Thread[Thread.activeCount()];  //close all running threads
        for (Thread t : threads) {
            if(t!=null) {
                Log.e("mylog", "onCreate init:force interrupt a thread");
                t.interrupt();
            }
        }

        connectState = (TextView) findViewById(R.id.connectState);
        msg = (ScrollForeverTextView) findViewById(R.id.msg);
        mySeekBar = (SeekBar) findViewById(R.id.myseek);
        mySeekBar.setEnabled(false);
        mySeekBar.setVisibility(View.GONE);
        swapTitle = (TextView) findViewById(R.id.swapTitle);
        swapTitle.setText("目前無換牌指令");
        swapMsg = (TextView) findViewById(R.id.swap_msg);
        swapMsg.setVisibility(View.INVISIBLE);
        workerID = (TextView) findViewById(R.id.workerID);
        connected = false;
        swapWorking = false;
        swapEnd = false;
        bc_msg_reply = false;
        bc_msgWorking = false;
        brandName = (TextView) findViewById(R.id.brandName);
        firstlist = (ListView) findViewById(R.id.listView2);
        connectionTimeoutCount = 0;
        tempSwapMessage = null;
        //TextClock tc= (TextClock) findViewById(R.id.textClock);
        //tc.setFormat24Hour();
        returnWorkerID = "";
        brandSelector = (Spinner) findViewById(R.id.brandSelecter);
        nextBrandArray = new ArrayList<String>();
        nextBrandArray.add("(無)");
        nextBrandAdapter = new ArrayAdapter<String>(MainActivity.this,  android.R.layout.simple_spinner_dropdown_item, nextBrandArray);
        brandSelector.setAdapter(nextBrandAdapter);
        brandSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                returnBrandName = pos;
            }
            public void onNothingSelected(AdapterView<?> parent) {
                returnBrandName = 0;
            }
        });
        n0 = (Button) findViewById(R.id.int0);
        n1 = (Button) findViewById(R.id.int1);
        n2 = (Button) findViewById(R.id.int2);
        n3 = (Button) findViewById(R.id.int3);
        n4 = (Button) findViewById(R.id.int4);
        n5 = (Button) findViewById(R.id.int5);
        n6 = (Button) findViewById(R.id.int6);
        n7 = (Button) findViewById(R.id.int7);
        n8 = (Button) findViewById(R.id.int8);
        n9 = (Button) findViewById(R.id.int9);
        n0.setOnClickListener(numberListener);
        n1.setOnClickListener(numberListener);
        n2.setOnClickListener(numberListener);
        n3.setOnClickListener(numberListener);
        n4.setOnClickListener(numberListener);
        n5.setOnClickListener(numberListener);
        n6.setOnClickListener(numberListener);
        n7.setOnClickListener(numberListener);
        n8.setOnClickListener(numberListener);
        n9.setOnClickListener(numberListener);
        btn_enter = (Button) findViewById(R.id.btn_enter);
        btn_enter.setOnClickListener(enterListener);
        btn_enter.setEnabled(false);
        btn_delete = (Button) findViewById(R.id.btn_del);
        btn_delete.setOnClickListener(deleteListener);
        //table1 = (TableLayout) findViewById(R.id.tab1table);
        //table2 = (TableLayout) findViewById(R.id.tab2layout);
        //boxRowCount = 0;
        valueListView = (ListView) findViewById(R.id.valueListView);
        boxListView = (ListView) findViewById(R.id.boxListView);
        valueArray = new ArrayList<ValueItem>();
        boxArray = new ArrayList<BoxItem>();
        valueAdapter = new ValueAdapter(MainActivity.this, valueArray);
        boxAdapter = new BoxAdapter(MainActivity.this, boxArray);
        valueListView.setAdapter(valueAdapter);
        boxListView.setAdapter(boxAdapter);
        //create 9 box lines
        for (int i=1; i<=MAX_LINE; i++) {
            BoxItem b = new BoxItem(String.valueOf(i), "0", "0");
            boxArray.add(b);
            ValueItem v = new ValueItem("(無)", "0.000", "0.000", "0.000", "0.000", "0.000", "0.000", "0.000", "0.000", "0.000", "");
            valueArray.add(v);
        }
        boxAdapter.notifyDataSetChanged();
        valueAdapter.notifyDataSetChanged();

        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec=tabHost.newTabSpec("tab1");
        spec.setContent(R.id.tab1layout);
        spec.setIndicator("品 管");
        tabHost.addTab(spec);
        spec=tabHost.newTabSpec("tab2");
        spec.setIndicator("數 量");
        spec.setContent(R.id.tab2layout);
        tabHost.addTab(spec);
        tabHost.setCurrentTab(0);

        TabWidget tabWidget = (TabWidget)tabHost.findViewById(android.R.id.tabs);
        View tabView = tabWidget.getChildTabViewAt(0);
        TextView tab = (TextView)tabView.findViewById(android.R.id.title);
        tab.setTextSize(24);
        tabView = tabWidget.getChildTabViewAt(1);
        tab = (TextView)tabView.findViewById(android.R.id.title);
        tab.setTextSize(24);

        if(!isNetworkConnected()) {  //close when not connected
            dialog = new AlertDialog.Builder(MainActivity.this).create();
            dialog.setTitle("警告");
            dialog.setMessage("無網路連線,\n程式即將關閉");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    });
            dialog.show();
            Log.e("Mylog", "no network");
        }
        else {  /* 開啟一個新線程，在新線程裡執行耗時的方法 */
            rebootCount = 0;
            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("連線中");
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            /*pd.setButton(DialogInterface.BUTTON_NEUTRAL, "更改ID...", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //dialog.dismiss();
                }
            });*/
            pd.show();
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
        String init = "CONNECT\t" + BOARD_ID + "<END>";
        SocketHandler.writeToSocket(init);
        str1 = SocketHandler.getOutput();

        String q = "QUERY\tRECIPE_LIST<END>";
        SocketHandler.writeToSocket(q);
        recipe_map = new HashMap<String, String>();
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
            Log.e("Mylog", "ServerDownHandler: connect failed!");
            if(dialog!=null && dialog.isShowing()) return;
            if(active) {
                active = false;
                dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.setTitle("警告");
                dialog.setMessage("伺服器無回應，\n5秒後自動重新連線，若問題持續請洽系統管理員");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "關閉程式",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                                Log.d("mylog", "to finish task...");
                            }
                        });
                dialog.show();
            }
            notOnstop = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("mylog", "ServerDownHandler wait 5000ms");
                    try { Thread.sleep(5000); }
                    catch (InterruptedException e) {
                        Log.e("mylog", "InterruptedException e=" + e);
                    }
                    if(connected) return;
                    if(dialog!=null && dialog.isShowing())
                        dialog.cancel();
                    active = false;
                    if(task!=null) task.cancel(true);
                    Thread[] threads = new Thread[Thread.activeCount()];  //close all running threads
                    for (Thread t : threads) {
                        if(t!=null) {
                            Log.e("mylog", "force interrupt a thread");
                            t.interrupt();
                        }
                    }
                    Log.d("mylog", "ServerDownHandler to start intent.");
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Log.d("mylog", "ServerDownHandler end.");
                }
            }).start();
            return;
        }
    };

    private void updateUI() {
        if(str1 != null && str1.contains("CONNECT_OK")) {
            connectState.setText("伺服器辨識成功");
            connectState.setTextColor(getResources().getColor(R.color.green));
            connected = true;
        } else {
            if(str1 != null) {
                connectState.setText(str1);
            } else {
                connectState.setText("No Connection");
            }
            connectState.setTextColor(getResources().getColor(R.color.red));
            connected = false;
        }
        msg.setText("no message");

        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {  //結束拖動時觸發
                if (seekBar.getProgress() > SEEK_DEST) {
                    /*if (List_file != null)
                        List_file.clear();*/
                    brandSelector.setSelection(0);
                    myAdapter.notifyDataSetChanged();
                    swapTitle.setText("目前無換牌指令");
                    swapTitle.setTextColor(getResources().getColor(R.color.dark_gray));
                    swapMsg.setText("");
                    swapMsg.setVisibility(View.INVISIBLE);
                    seekBar.setProgress(5);
                    seekBar.setEnabled(false);
                    mySeekBar.setVisibility(View.GONE);
                    mySeekBar.setEnabled(false);
                    swapEnd = true;
                    swapWorking = false;
                    bname = "";
                    brandName.setText(bname);
                    btn_enter.setEnabled(false);
                    workerID.setText("");
                    if (task != null) {
                        Log.d("Mylog", "task is: " + task.getStatus());
                        task.cancel(true);
                    }
                    task = new UpdateTask().execute();
                    Log.d("Mylog", "swap end.");
                    //swapWorking = false;
                } else {
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.slider, null));
                    seekBar.setProgress(5);  //go back to zero
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {  /* 開始拖動時觸發*/ }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  //進度改變時觸發  只要在拖動，就會重複觸發
                if (progress > SEEK_DEST)
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.slider_ok, null));
                else
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.slider, null));
            }
        });
    }

    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(returnWorkerID!=null && returnWorkerID.length()>0) {
                returnWorkerID = returnWorkerID.substring(0, returnWorkerID.length() - 1);
                workerID.setText(returnWorkerID);
            }
        }
    };

    private View.OnClickListener enterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("Mylog", "enter pressed, ID=" + returnWorkerID + ", returnBrandName=" + returnBrandName);
            if(nextBrandArray.size()>1 && returnBrandName!=1) {
                dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.setTitle("警告");
                dialog.setMessage(setDialogText("未確認下個品牌", 3));
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "重試一次",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                            }
                        });
                dialog.show();
                return;
            }
            mySeekBar.setVisibility(View.VISIBLE);
            mySeekBar.setEnabled(true);
            swapTitle.setText("向右滑動切換品牌");
            swapTitle.setTextColor(getResources().getColor(R.color.black));
        }
    };

    private View.OnClickListener numberListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(returnWorkerID.length() < 7) {
                Button b = (Button) v;
                returnWorkerID += b.getText();
                workerID.setText(returnWorkerID);
            }
        }
    };

    private class UpdateTask extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... v) {
            //Log.d("Mylog", "UpdateTask listening0...");
            while(!isCancelled()) {
                //Log.d("Mylog", "swapEnd=" + swapEnd + ", swapWorking=" + swapWorking + " bc_msg_reply=" +bc_msg_reply);
                if(swapEnd) {
                    Log.d("Mylog", "prepare to send SWAP OK, ID=" + returnWorkerID);
                    String s = "SWAP_OK\t" + returnWorkerID +"<END>";
                    SocketHandler.writeToSocket(s);
                    swapWorking = false;
                    swapEnd = false;  //TODO: check if ID is correct
                    returnWorkerID = "";
                    Log.d("Mylog", "swapWorking -> false");
                    continue;
                }
                if(swapWorking) {
                    Log.e("Mylog", "break!");
                    break;
                }
                if(connectionTimeoutCount >= 11)
                    break;
                if(bc_msg_reply) {
                    Log.d("Mylog", "to send BC_MSG_OK<END>");
                    String s = "BC_MSG_OK<END>";
                    SocketHandler.writeToSocket(s);
                    bc_msg_reply = false;
                    bc_msgWorking = false;
                    Log.d("Mylog", "BC_MSG_OK");
                }
                if(swap_msg_reply) {
                    Log.d("Mylog", "to send SWAP_MSG_OK<END>");
                    String s = "SWAP_MSG_OK<END>";
                    SocketHandler.writeToSocket(s);
                    swap_msg_reply = false;
                    swap_msgWorking = false;
                    Log.d("Mylog", "SWAP_MSG_OK");
                }
                if(bc_msgWorking || swap_msgWorking)
                    continue;

                Log.e("Mylog", "UpdateTask listening...");
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
                    Thread.sleep(400);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        protected void onProgressUpdate(String... values) {
            if(connectionTimeoutCount >= 10) {
                connected = false;
                Log.e("Mylog", "connect failed!");
                if(dialog!=null && dialog.isShowing()) return;
                if(active) {
                    active = false;
                    dialog = new AlertDialog.Builder(MainActivity.this).create();
                    dialog.setTitle("警告");
                    dialog.setMessage("伺服器無回應，\n5秒後自動重新連線，若問題持續請洽系統管理員");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "關閉程式",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                    Log.d("mylog", "to finish task...");
                                }
                            });
                    dialog.show();
                }
                notOnstop = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("mylog", "onProgressUpdate:wait 5000ms");
                        try { Thread.sleep(5000); }
                        catch (InterruptedException e) {
                            //e.printStackTrace();
                            Log.e("mylog", "InterruptedException e=" + e.toString());
                        }
                        if(connected) return;
                        if(dialog!=null && dialog.isShowing())
                            dialog.cancel();
                        if(task!=null) task.cancel(true);
                        Log.d("mylog", "onProgressUpdate:after 5000ms");
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Thread[] threads = new Thread[Thread.activeCount()];  //close all running threads
                        for (Thread t : threads) {
                            if(t!=null) {
                                Log.e("mylog", "force interrupt a thread");
                                t.interrupt();
                            }
                        }
                        Log.d("mylog", "onProgressUpdate end.");
                    }
                }).start();
                return;
            }

            String result = values[0];
            if(result==null ||result.length() == 0) return;
            String[] lines = result.split("<END>");
            int length = lines.length;

            Log.d("Mylog", "lines.length=" + length);
            boolean updateList = false;
            for(String s: lines) {
                notOnstop = true;
                Log.d("Mylog", "s in line=" + s);
                if(active && s!=null && s.contains("SWAP_MSG\t")) {
                    s = s.replaceAll("SWAP_MSG\t", "");
                    //swapMsg.setVisibility(View.VISIBLE);
                    //swapMsg.setText(s);
                    if(s.contains("此工號不存在")) {
                        if(task!=null)
                            task.cancel(true);
                        swapWorking = true;
                        Log.d("mylog", "此工號不存在");
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle("警告");
                        dialog.setMessage("此工號不存在");
                        dialog.setMessage(setDialogText(s, 3));
                        dialog.setPositiveButton("重試",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        mySeekBar.setVisibility(View.GONE);
                                        btn_enter.setEnabled(true);
                                        swapMsg.setText("請輸入品牌與員工ID");
                                        Log.d("Mylog", "此工號不存在::ok is pressed, task is: " + task.getStatus());
                                    }
                                });
                        dialog.show();
                    } else {
                        swap_msgWorking = true;
                        Log.d("mylog", "inside SWAP_MSG");
                        s = s.replaceAll("SWAP_MSG\t", "");
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle("廣播");
                        dialog.setMessage(setDialogText(s, 3));
                        dialog.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        //send BC_MSG_OK<END>
                                        swap_msg_reply = true;
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                } else if(active && s!=null && s.contains("BC_MSG")) {  //廣播
                    bc_msgWorking = true;
                    Log.d("mylog", "inside BC_MSG");
                    s = s.replaceAll("BC_MSG\t", "");
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("廣播");
                    dialog.setMessage(setDialogText(s, 3));
                    dialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    //send BC_MSG_OK<END>
                                    bc_msg_reply = true;
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();
                } else if(active && s!=null && s.contains("MSG\t")) {
                    s = s.replaceAll("MSG\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    msg.setText(s);
                } else if(active && s!=null && s.contains("UPDATE_LIST\t")) {
                    s = s.replaceAll("UPDATE_LIST\t", "");
                    String[] items = s.split("\t");
                    if(items.length >= 2 && List_file != null) {
                        for(int j=0; j<List_file.size(); j++) {
                            if(List_file.get(j).productSerial.equals(items[0])) {
                                List_file.get(j).itemCount = items[1];
                                //Log.d("mylog", "becomes: " + List_file.get(j).itemCount);
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else if(active && s != null && s.contains("LIST\t")) {
                    //if(List_file != null) {
                    //List_file.clear();
                    //    List_file = new ArrayList<ListItem>();
                    //} else
                    if (List_file == null)
                        List_file = new ArrayList<ListItem>();
                    else
                        List_file.clear();
                    s = s.replaceAll("LIST\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    String[] items = s.split("\n");
                    for (String i : items) {
                        Log.d("Mylog", "line i=" + i);
                        String[] single_item = i.split("\t");
                        String barcode_text = "";
                        if (single_item.length >= 5) {
                            updateList = true;
                            barcode_text = single_item[2];
                            bname = single_item[1];
                            brandName.setText(bname);
                            //Log.d("Mylog", "barcode_text=" + barcode_text);
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
                } else if(active && s!=null && s.contains("SWAP")) {
                    //s = s.replaceAll("SWAP\t", "");
                    tempSwapMessage = s;
                    swapWorking = true;
                    Log.d("Mylog", "swap!!");
                    String[] items = s.split("\t");
                    nextBrandArray.clear();
                    Log.d("mylog", "items.length=" + items.length);
                    if(items.length > 1) {  //have next brand
                        nextBrandArray.add("(請選擇)");
                        key = items[1];
                        String value = recipe_map.get(key);
                        Log.d( "mylog", "items[1] is " + key + ", next brand is " +value );
                        if(value != null) {
                            nextBrandArray.add(value);
                            key = null;
                        } else {
                            Log.d("mylog", "value is null");
                        }
                    } else {
                        nextBrandArray.add("(無)");
                    }
                    nextBrandAdapter.notifyDataSetChanged();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("警告");
                    dialog.setMessage(setDialogText("已下達換牌指令！", 3));
                    dialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    /*mySeekBar.setVisibility(View.VISIBLE);
                                    mySeekBar.setEnabled(true);
                                    swapTitle.setText("向右滑動切換品牌");
                                    swapTitle.setTextColor(getResources().getColor(R.color.black));*/
                                    swapWorking = true;
                                    btn_enter.setEnabled(true);
                                    Log.d("Mylog", "OK pressed");
                                    //task.cancel(true);
                                }
                            });
                    swapMsg.setText("請輸入品牌與員工ID");
                    Log.d("Mylog", "prepare to show dialog...");
                    dialog.show();
                } else if(active && s!=null && s.contains("LIST_EMPTY<END>")) {
                    Log.d("Mylog", "clear!");
                    if(List_file != null)
                        List_file.clear();
                    myAdapter.notifyDataSetChanged();
                } else if(active && s!=null && s.contains("UPDATE_BOX\t")) { //UPDATE_BOX \t 線號 \t 現在箱數 \t 目標箱數
                    s = s.replaceAll("UPDATE_BOX\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    String[] items = s.split("\n");
                    for(String i: items) {
                        Log.d("Mylog","line i=" + i);
                        String[] single_item = i.split("\t");
                        if(single_item.length >= 3) {
                            int lineNumber = Integer.parseInt(single_item[0]) - 1;
                            BoxItem b = new BoxItem(single_item[0], single_item[1], single_item[2]);
                            boxArray.set(lineNumber, b);
                        }
                    }
                    boxAdapter.notifyDataSetChanged();
                } else if(active && s!=null && s.contains("UPDATE_VALUE\t")) {  //時間\t線號\t品牌名稱\t重量max\t重量value\t重量min\t圓周max\t圓周value\t圓周min\t透氣率max\t透氣率value\t透氣率min
                    s = s.replaceAll("UPDATE_VALUE\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    String[] items = s.split("\n");
                    for(String i: items) {
                        Log.d("Mylog","line i=" + i);
                        String[] single_item = i.split("\t");
                        if(single_item.length >= 12) {
                            int lineNumber = Integer.parseInt(single_item[1]) - 1;
                            String name = "生產線" + single_item[1] + " " + single_item[2];
                            String time = "最後更新: " + single_item[0];
                            ValueItem v = new ValueItem(name, single_item[3], single_item[4], single_item[5], single_item[6], single_item[7], single_item[8], single_item[9], single_item[10], single_item[11], time);
                            valueArray.set(lineNumber, v);
                        }
                    }
                    valueAdapter.notifyDataSetChanged();
                } else if (active && s!=null && s.contains("QUERY_REPLY\t")) {
                    s = s.replaceAll("QUERY_REPLY\t", "");
                    s = s.replaceAll("<N>", "\n");
                    s = s.replaceAll("<END>", "");
                    Log.d("mylog", "new RECIPE_LIST=" + s);
                    String[] items = s.split("\n");
                    for(String i: items) {
                        String[] recipe = i.split("\t");
                        if(recipe.length>1) {
                            recipe_map.put(recipe[0], recipe[1]);
                            //Log.d("mylog", recipe[0] + " " + recipe[1] + "\n");
                        }
                    }
                    if(key != null) {
                        nextBrandArray.add(recipe_map.get(key));
                        nextBrandAdapter.notifyDataSetChanged();
                        key = null;
                    }
                    /*System.out.println("recipe_map=");
                    for (Object key : recipe_map.keySet()) {
                        System.out.println(key + " : " + recipe_map.get(key));
                    }*/
                }
                if (s!=null && s.contains("CONNECT_OK<END>")) {
                    active = true;
                    connected = true;
                }
            }
            if(updateList) {
                myAdapter = new MySimpleArrayAdapter(MainActivity.this, List_file);
                firstlist.setAdapter(myAdapter);
            }
        }
    }

    public SpannableString setDialogText(String text, float size) {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new RelativeSizeSpan(size), 0, ss.length(), 0);
        return ss;
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
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            rebootCount++;
            if(rebootCount > 3) {
                notOnstop = true;
                active = false;
                if (task != null)
                    task.cancel(true);
                //SocketHandler.closeSocket();
                if (pd != null)
                    pd.dismiss();
                Thread[] threads = new Thread[Thread.activeCount()];  //close all running threads
                Thread.enumerate(threads);
                for (Thread t : threads) {
                    t.interrupt();
                }
                Log.d("mylog", "KEYCODE_VOLUME_UP key long press!");
                Intent intent = new Intent(MainActivity.this, ChangeID.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        /*if(!notOnstop) {
            Log.d("mylog", "onStop is called");
            System.exit(0);
            task.cancel(true);
            SocketHandler.closeSocket();
            finish();
        }*/
        Thread[] threads = new Thread[Thread.activeCount()];  //close all running threads
        Thread.enumerate(threads);
        for (Thread t : threads) {
            t.interrupt();
        }
        active = false;
        task.cancel(true);
        super.onStop();
    }

    public static void restart(){
        if(staticContext == null) return;
        Log.d("mylog", "restart is called");
        active = false;
        Thread[] threads = new Thread[Thread.activeCount()];  //close all running threads
        Thread.enumerate(threads);
        for (Thread t : threads) {
            t.interrupt();
        }
        Intent intent = new Intent(staticContext, ChangeID.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        staticContext.startActivity(intent);
    }
}
