package com.tnh.myapplication.mosaic;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

/**
 * Created by ThoNh on 2/1/2018.
 */

public class MosaicHolder {

    /*Drawing path, start when user touch_down, end when user touch_up */
    public Path mPath;

    public Paint mPaint ;


    public MosaicHolder(Path path, Paint paint) {
        mPath = path;
        mPaint = paint;
    }
}
