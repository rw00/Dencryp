package com.rw.dencryp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int LOADING_STEP_TIME = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final ProgressBar progressbarLoading = (ProgressBar) findViewById(R.id.progressbar_loading);
        progressbarLoading.setProgress(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressbarLoading.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
        } else {
            progressbarLoading.getProgressDrawable().setColorFilter(
                    Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        final int progress[] = new int[1];
        Thread threadLoading = new Thread(new Runnable() {
            public void run() {
                long time = System.currentTimeMillis();
                while (progress[0] <= 100) {
                    if (System.currentTimeMillis() - time >= LOADING_STEP_TIME) {
                        SplashActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                progress[0]++;
                                progressbarLoading.setProgress(progress[0]);
                            }
                        });
                        time = System.currentTimeMillis();
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage(), ex);
                } finally {
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });
        threadLoading.start();
    }
}
