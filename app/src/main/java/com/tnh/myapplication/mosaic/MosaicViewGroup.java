package com.tnh.myapplication.mosaic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by ThoNh on 1/30/2018.
 */

public class    MosaicViewGroup extends FrameLayout {
    public static final String TAG = MosaicViewGroup.class.getSimpleName();

    /*Show image*/
    private ImageView mImageView;

    /*For draw on it*/
    private MosaicView mMosaicView;

    public MosaicViewGroup(@NonNull Context context) {
        super(context);
        init();
    }

    public MosaicViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MosaicViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void resetView() {
        removeAllViews();
        init();
    }

    public void init() {

        // Add ImageView
        mImageView = new ImageView(getContext());
        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mImageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mImageView);

        // Add Mosaic
        mMosaicView = new MosaicView(getContext());
        mMosaicView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mMosaicView);
    }


    /**
     * SetImageResource for ImageView
     * After set Image for ImageView --> Calculates the mSize rect of the drawable set in the imageView then create MosaicView with mSize of Rect
     *
     * @param resId
     */
    public void setImageResource(@DrawableRes final int resId) {
        mImageView.setImageResource(resId);
        reDrawMosaicView();
    }

    public void setImageBitmap(Bitmap bm) {
        mImageView.setImageBitmap(bm);
        reDrawMosaicView();
    }

    private void reDrawMosaicView() {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RectF bounds = new RectF();
                Drawable drawable = mImageView.getDrawable();
                if (drawable != null) {
                    mImageView.getImageMatrix().mapRect(bounds, new RectF(drawable.getBounds()));
                }

                LayoutParams lp = new LayoutParams((int) (bounds.right - bounds.left), (int) (bounds.bottom - bounds.top));
                mMosaicView.setLayoutParams(lp);
                mMosaicView.setX((int) bounds.left);
                mMosaicView.setY((int) bounds.top);
            }
        }, 300);
    }


    public void setBitmapShader(Bitmap bmShader) {
        if (mMosaicView != null) {
            mMosaicView.setBitmapShader(bmShader);
            mMosaicView.setMode(MosaicMode.DRAW);
        }
    }


    public void setSizeDraw(@FloatRange(from = 100f, to = 200f) float size) {
        if (mMosaicView != null) {
            mMosaicView.setSize(size);
        }
    }


    public void setModeDraw(MosaicMode mode) {
        if (mMosaicView != null) {
            mMosaicView.setMode(mode);
        }
    }

    public float getSize() {
        return mMosaicView.getSize();
    }


    public void undo() {
        mMosaicView.onUndoClick();
    }

    public void redo() {
        mMosaicView.onRedoClick();
    }

}
