package com.example.luka.openglestest;

import android.app.Activity;


import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;

import android.opengl.GLSurfaceView;
import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.*;
import static android.opengl.Matrix.*;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    float stariX, stariY;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //glSurfaceView = new GLSurfaceView(this);

        setContentView(R.layout.activity_main);

        try {
            glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();

        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        final MojRenderer mojRenderer = new MojRenderer(this);
        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign our renderer.



            try {
                glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            glSurfaceView.setRenderer(mojRenderer);


            rendererSet = true;
        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }


        glSurfaceView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event != null) {

                    final float aspectRatio = v.getWidth() > v.getHeight() ?
                            (float) v.getWidth() / (float) v.getHeight() :
                            (float) v.getHeight() / (float) v.getWidth();

                    final float normalizedX;
                    final float normalizedY;

                    if (v.getWidth() > v.getHeight())
                    {
                         normalizedX = (event.getX() / (float) v.getWidth()) * 2 * aspectRatio - aspectRatio;
                         normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);
                    }

                    else
                    {
                        normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
                        normalizedY = -((event.getY() / (float) v.getHeight()) * 2 * aspectRatio - aspectRatio);
                    }



                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                mojRenderer.handleTouchPress(normalizedX, normalizedY);
                            }
                        });
                    }

                    else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                mojRenderer.handleTouchDrag(normalizedX, normalizedY);
                            }


                        });
                    }



                    return true;
                } else return false;
            }






         });


        //setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }
}
