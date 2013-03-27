package com.example.androidwwo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtil {
    public static Bitmap loadBitmap(String url) {
    	Bitmap bitmap = null;
    	try {
    		bitmap = BitmapFactory.decodeStream((new URL(url)).openConnection().getInputStream());
    	} catch (Exception e) {
    		
    	}

        return bitmap;
    }
}
