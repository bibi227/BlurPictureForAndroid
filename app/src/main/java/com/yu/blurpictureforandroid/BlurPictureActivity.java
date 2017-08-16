package com.yu.blurpictureforandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BlurPictureActivity extends AppCompatActivity {

    final static String BLUR_PICTURE_NAME = "blur_picture.png";
    File blurredImage = null;
    int screenWidth = 720;
    ImageView blur_image_view = null;
    ObservableScrollView blur_scrollview = null;
    TextView body_tv = null;

    final static String TAG = "blurpicture";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur_picture);
        blur_image_view = (ImageView) findViewById(R.id.blur_imageview);
        blur_scrollview = (ObservableScrollView) findViewById(R.id.scrollview);
        body_tv = (TextView) findViewById(R.id.body_tv);
        screenWidth = getScreenWidth(this);
        blurredImage = new File(getFilesDir() + BLUR_PICTURE_NAME);
        if (!blurredImage.exists()) {
            createBlurPicture();
        } else {
            setBlurImageView();
        }

        blur_scrollview.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {

                float alpha = (float)y/(body_tv.getHeight()-scrollView.getHeight());
                if (alpha > 1) {
                    alpha = 1;
                }
                blur_image_view.setAlpha(alpha);
            }
        });
    }

    public void createBlurPicture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap origin_image = BitmapFactory.decodeResource(getResources(), R.drawable.a, options);
                Bitmap blur_img = BlurMethod.gauss_blur(BlurPictureActivity.this, origin_image, 15);
                storeImage(blur_img, blurredImage);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBlurImageView();
                    }
                });
            }
        }).start();


    }

    public void storeImage(Bitmap image, File file) {
        if (file == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "storeImage", e);
        } catch (IOException e) {
            Log.e(TAG, "storeImage", e);
        }
    }

    public void setBlurImageView() {
        Bitmap blur_bitmap = BitmapFactory.decodeFile(getFilesDir() + BLUR_PICTURE_NAME);
        blur_bitmap = Bitmap.createScaledBitmap(blur_bitmap, screenWidth, (int) (blur_bitmap.getHeight()
                * ((float) screenWidth) / (float) blur_bitmap.getWidth()), false);
        blur_image_view.setImageBitmap(blur_bitmap);
    }

    public static int getScreenWidth(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }
        return display.getWidth();
    }

}
