package com.tnh.myapplication.mosaic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.tnh.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ThoNh on 1/30/2018.
 */

public class MosaicView extends View {
    private static final String TAG = MosaicView.class.getSimpleName();

    /*Threshold start draw*/
    private static final float TOUCH_TOLERANCE = 4;

    private static final float DEFAULT_PAINT_STROKE_WIDTH = 100f;

    private static final int DEFAULT_SIZE_SHADER = 250;

    /*Paint for draw*/
    private Paint mPaint;

    private Shader mCurrentShader;

    /*Using this list for draw, all part will be save in list and using for loop to draw with canvas*/
    private List<MosaicHolder> mDrawList = new ArrayList<>();

    /*Using this list for save undo mPath*/
    private List<MosaicHolder> mUndoList = new ArrayList<>();

    /*Path drawing*/
    private Path mPath;

    /*TempX, tempY*/
    private float mTempX, mTempY;


    private Canvas mBufferCanvas;

    private Bitmap mBufferBitmap;

    private Paint mBufferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    public MosaicView(Context context) {
        super(context);
        init();
    }

    public MosaicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MosaicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // create default shader
        Bitmap defaultBmShad = BitmapFactory.decodeResource(getResources(), R.drawable.default_mosaic);
        defaultBmShad = resizeShaderBitmap(defaultBmShad);
        mCurrentShader = new BitmapShader(defaultBmShad, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(DEFAULT_PAINT_STROKE_WIDTH);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setShader(mCurrentShader);
        mPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));

        mPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e(TAG, "onSizeChanged");

        if (mBufferBitmap == null && w > 0 && h > 0) {
            mBufferBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Log.e(TAG, "createBitmap:" + w + ":" + h);
        }

        if (mBufferCanvas == null && mBufferBitmap != null) {
            mBufferCanvas = new Canvas(mBufferBitmap);
            Log.e(TAG, "createCanvas");
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(x, y);
                invalidate();
                break;
        }

        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        mBufferBitmap.eraseColor(Color.TRANSPARENT); // reset bitmap avoid cannot undo redo after bitmap render

        if (mBufferBitmap != null && mBufferCanvas != null) {
            for (MosaicHolder mh : mDrawList) {
                mBufferCanvas.drawPath(mh.mPath, mh.mPaint);
            }

            mBufferCanvas.drawPath(mPath, mPaint);

            canvas.drawBitmap(mBufferBitmap, 0, 0, mBufferPaint);
        }
    }

    private void onTouchDown(float x, float y) {
        mUndoList.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mTempX = x;
        mTempY = y;
    }

    private void onTouchMove(float x, float y) {
        float dx = Math.abs(x - mTempX);
        float dy = Math.abs(y - mTempY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mTempX, mTempY, (x + mTempX) / 2, (y + mTempY) / 2);
            mTempX = x;
            mTempY = y;
        }
    }

    private void onTouchUp(float x, float y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(mPaint.getShader());
        paint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));
        paint.setStrokeWidth(mPaint.getStrokeWidth());
        paint.setShader(mPaint.getShader());
        paint.setXfermode(mPaint.getXfermode());

        mDrawList.add(new MosaicHolder(mPath, paint));
        mPath = new Path();
    }

    public void onUndoClick() {
        if (mDrawList.size() > 0) {
            mUndoList.add(mDrawList.remove(mDrawList.size() - 1)); // remove last path && add last path to undo list
            invalidate(); // draw all path in mDrawList
        } else {
            Toast.makeText(getContext(), "Cannot undo!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRedoClick() {
        if (mUndoList.size() > 0) {
            mDrawList.add(mUndoList.remove(mUndoList.size() - 1)); // remove last path in UndoList && add that path to mDrawList
            invalidate(); // draw all path mDrawList
        } else {
            Toast.makeText(getContext(), "Cannot redo more!", Toast.LENGTH_SHORT).show();
        }

    }

    public void setBitmapShader(Bitmap bitmapShader) {
        bitmapShader = resizeShaderBitmap(bitmapShader);
        mCurrentShader = new BitmapShader(bitmapShader, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mPaint.setShader(mCurrentShader);
        Log.e(TAG, "setBitmapShader:" + bitmapShader.getWidth() + ":" + bitmapShader.getHeight());
    }

    public void setMode(MosaicMode mode) {
        switch (mode) {
            case DRAW:
                mPaint.setShader(mCurrentShader);
                mPaint.setXfermode(null);
                break;
            case ERASE:
                mPaint.setShader(null);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                break;
        }
        Log.e(TAG, "setMode:" + mode.toString());
    }

    private Bitmap resizeShaderBitmap(Bitmap bm) {
        Log.e(TAG, "resizeShaderBitmap() source --> " + bm.getWidth() + ":" + bm.getHeight());

        while (bm.getWidth() > DEFAULT_SIZE_SHADER) {
            bm = Bitmap.createScaledBitmap(bm, (int) (bm.getWidth() / 1.5f), (int) (bm.getHeight() / 1.5f), true);
        }

        Log.e(TAG, "resizeShaderBitmap() --> destiny " + bm.getWidth() + ":" + bm.getHeight());
        return bm;
    }

    public void setSize(float size) {
        this.mPaint.setStrokeWidth(size);
        Log.e(TAG, "setSize:" + size);
    }

    public float getSize() {
        return mPaint.getStrokeWidth();
    }
}
