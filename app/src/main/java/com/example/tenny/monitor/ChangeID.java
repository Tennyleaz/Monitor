package com.example.tenny.monitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Tenny on 2015/11/27.
 */
public class ChangeID extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int correctChangeID = intent.getExtras().getInt("correctChangeID", 0);
        if( correctChangeID != 1) return;

        setContentView(R.layout.id_changer);
        ArrayList<String> nameArray, idArray;
        final Spinner nameSelect = (Spinner) findViewById(R.id.nameSelect);
        final Spinner idSelect = (Spinner) findViewById(R.id.numberSelect);
        TextView name = (TextView) findViewById(R.id.name);
        TextView number = (TextView) findViewById(R.id.number);
        Button btn = (Button) findViewById(R.id.button);
        Button btnCancel = (Button) findViewById(R.id.button2);
        final SharedPreferences settings = getApplicationContext().getSharedPreferences("EC510", 0);
        Log.e("mylog", "change id start");

        // Get from the SharedPreferences
        name.setText(settings.getString("board_name", "CM"));
        number.setText(settings.getString("board_ID", "1"));
        nameArray = new ArrayList<String>();
        idArray = new ArrayList<String>();
        nameArray.add("CM");
        nameArray.add("PM");
        nameArray.add("PP");
        idArray.add("1");
        idArray.add("2");
        idArray.add("3");
        idArray.add("4");
        idArray.add("5");
        idArray.add("6");
        idArray.add("7");
        idArray.add("8");
        idArray.add("9");
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(ChangeID.this,  android.R.layout.simple_spinner_dropdown_item, nameArray);
        ArrayAdapter<String> idAdapter = new ArrayAdapter<String>(ChangeID.this,  android.R.layout.simple_spinner_dropdown_item, idArray);
        nameSelect.setAdapter(nameAdapter);
        idSelect.setAdapter(idAdapter);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name, ID;
                name = nameSelect.getSelectedItem().toString();
                ID = idSelect.getSelectedItem().toString();
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("board_name", name);
                editor.putString("board_ID", ID);
                // Apply the edits!
                editor.apply();
                //Intent intent = new Intent(ChangeID.this, MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //startActivity(intent);
                try{
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
