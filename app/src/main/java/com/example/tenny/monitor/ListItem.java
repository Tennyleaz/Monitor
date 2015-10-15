package com.example.tenny.monitor;

import android.graphics.Bitmap;

/**
 * Created by Tenny on 2015/10/6.
 */
public class ListItem {
    public String itemCount;
    public String productName;
    public String productSerial;
    public Bitmap barcode;

    public  ListItem(String productName, String productSerial, String itemCount, Bitmap barcode) {
        super();
        this.itemCount = itemCount;
        this.productName = productName;
        this.productSerial = productSerial;
        this.barcode = barcode;
    }
}
