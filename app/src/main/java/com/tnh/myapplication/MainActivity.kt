package com.tnh.myapplication

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import com.tnh.myapplication.mosaic.MosaicMode
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val PICK_IMAGE = 11111


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val decodeSampledBitmapFromResource = decodeSampledBitmapFromResource(res = resources, resId = R.drawable.image3,
                reqWidth = Resources.getSystem().displayMetrics.widthPixels, reqHeight = Resources.getSystem().displayMetrics.heightPixels)
        mMosaicViewGroup.setImageBitmap(decodeSampledBitmapFromResource)
        mMosaicViewGroup.setBitmapShader(BitmapFactory.decodeResource(resources, R.drawable.mosaic_love))


        val arrayShader = listOf(R.drawable.m_dot, R.drawable.m_flower, R.drawable.m_small_flowwer,
                R.drawable.m_kiwi, R.drawable.m_love, R.drawable.m_melon, R.drawable.m_wave, R.drawable.m_star)

        arrayShader.forEachIndexed { index, i ->

            val layoutParams = LinearLayout.LayoutParams(200, 200)
            layoutParams.setMargins(100, 0, 100, 0)

            val img = ImageView(this)
            img.setImageResource(i)
            img.layoutParams = layoutParams


            mContainerMosaic.addView(img)

            img.setOnClickListener {

                mMosaicViewGroup.setBitmapShader(BitmapFactory.decodeResource(resources, i))

            }


        }

        mSeekbar.progress = mMosaicViewGroup.getSize().toInt()

        mSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { /*min=0 ; max =100*/
                if (fromUser) {
                    mMosaicViewGroup.setSizeDraw(progress.toFloat() + 100) // size range : 100 - 200
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        mClear.setOnClickListener {
            mMosaicViewGroup.setModeDraw(MosaicMode.ERASE)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_choose_image) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }


        if (item?.itemId == R.id.action_undo) {
            mMosaicViewGroup.undo()
        }


        if (item?.itemId == R.id.action_redo) {
            mMosaicViewGroup.redo()
        }


        return super.onOptionsItemSelected(item)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val inputStream = getContentResolver().openInputStream(data.data)
            val decodeStream = BitmapFactory.decodeStream(inputStream)
            mMosaicViewGroup.resetView()
            mMosaicViewGroup.setImageBitmap(decodeStream)
        }
    }

}

fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    Log.e("ThoNH", "in:" + inSampleSize)
    return inSampleSize
}

fun decodeSampledBitmapFromResource(res: Resources, resId: Int,
                                    reqWidth: Int, reqHeight: Int): Bitmap {

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(res, resId, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}
