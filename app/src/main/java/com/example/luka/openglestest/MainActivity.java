package com.example.luka.openglestest;

import android.app.Activity;


import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import android.opengl.GLSurfaceView;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.opengl.Matrix.*;

import com.example.luka.openglestest.engine.MainMenu;
import com.example.luka.openglestest.engine.MenuButton;
import com.example.luka.openglestest.engine.MenuButtonStyle;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.opengl.Matrix.multiplyMM;

public class MainActivity extends Activity implements SensorEventListener {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    float stariX, stariY;

    Resources r;
    DisplayMetrics dm;
    float dmScale;
    MainMenu mainMenu;
    MenuButtonStyle mbs;
    LinearLayout llRoot;
    LinearLayout.LayoutParams llParams;

    SensorManager mSensorManager;
    Sensor accelerationSensor;

    float dRoll, dPitch;
    float dIgnoreThreshold = 5;
    float alpha = 0.15f;

    public float[] acceleration = new float[3], accelerationTm1 = new float[3];

    public static final Object mutex = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        //glSurfaceView = new GLSurfaceView(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        r = getResources();
        dm = r.getDisplayMetrics();
        dmScale = dm.density;

        setContentView(R.layout.activity_main);

        //createMainMenu();

        glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);

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
        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign our renderer.

            try
            {
                glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            glSurfaceView.setRenderer(mojRenderer);


            rendererSet = true;
        } else
        {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                    } else
                    {
                        normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
                        normalizedY = -((event.getY() / (float) v.getHeight()) * 2 * aspectRatio - aspectRatio);
                    }


                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                mojRenderer.handleTouchPress(normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE)
                    {
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
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        if ( MojRenderer.plane == null )
            return;

        acceleration = event.values.clone();

//        if ( Math.sqrt( Math.pow( acceleration[0] - accelerationTm1[0], 2 ) +
//                        Math.pow( acceleration[1] - accelerationTm1[1], 2 ) +
//                        Math.pow( acceleration[2] - accelerationTm1[2], 2 ) ) > 8.5f )
//        {
//            return;
//        }

//        if (        Math.abs(acceleration[0] - accelerationTm1[0]) > dIgnoreThreshold
//                ||  Math.abs(acceleration[1] - accelerationTm1[1]) > dIgnoreThreshold )
//        {
//            return;
//        }
//
        acceleration = exponentialSmoothing( acceleration, accelerationTm1, alpha );

        dRoll = -acceleration[1] * 10;
        dPitch = -acceleration[0] * 10;

        float[] tempMatrix = new float[16];

        android.opengl.Matrix.setIdentityM( tempMatrix, 0 );

        synchronized(mutex) {
            android.opengl.Matrix.setIdentityM(MojRenderer.plane.rotationMatrix, 0);
            android.opengl.Matrix.setRotateM(tempMatrix, 0, dRoll, 0, 1.0f, 0);
            multiplyMM(MojRenderer.plane.rotationMatrix, 0, tempMatrix, 0, MojRenderer.plane.rotationMatrix, 0);
            android.opengl.Matrix.setRotateM(tempMatrix, 0, dPitch, 1.0f, 0, 0);
            multiplyMM(MojRenderer.plane.rotationMatrix, 0, tempMatrix, 0, MojRenderer.plane.rotationMatrix, 0);
        }

        accelerationTm1 = acceleration;

    }

    private float[] exponentialSmoothing( float[] xt, float[] stm1, float alpha ) {
        if ( stm1 == null )
            return xt;
        for ( int i=0; i<xt.length; i++ ) {
            //stm1[i] = stm1[i] + alpha * (xt[i] - stm1[i]);
            stm1[i] = alpha * xt[i] + (1 - alpha) * stm1[i];
        }
        return stm1;
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mSensorManager.unregisterListener(this);

        if (rendererSet)
        {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mSensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        if (rendererSet)
        {
            glSurfaceView.onResume();
        }
    }

    void createMainMenu()
    {

        llRoot = (LinearLayout) findViewById(R.id.ll_root);
        //mainMenu = new MainMenu(this, llRoot, 0x80073763); // b
        //mainMenu = new MainMenu(this, llRoot, 0x80660000); // r
        mainMenu = new MainMenu(this, llRoot, 0x80274e13); // g
        llRoot.addView(mainMenu);

        llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.18f);
        llParams.setMargins(0, DpToPixels(6, dmScale), 0, DpToPixels(6, dmScale));

        //mbs = new MenuButtonStyle(R.drawable.btn_blue, llParams, DpToPixels(20, dmScale), 0xffffffff); // b
        // (int backgroundResource, LinearLayout.LayoutParams llp, int textSize, int textColor)
        //mbs = new MenuButtonStyle(R.drawable.btn_red, llParams, DpToPixels(20, dmScale), 0xffffffff); // r
        mbs = new MenuButtonStyle(R.drawable.btn_green, llParams, DpToPixels(20, dmScale), 0xffffffff); // g

        MenuButton btnNewGame = new MenuButton(this, mbs, getString(R.string.main_newgame));
        // (Context context, MenuButtonStyle mbs, String text)
        MenuButton btnScoreboard = new MenuButton(this, mbs, getString(R.string.main_scoreboard));
        MenuButton btnSettings = new MenuButton(this, mbs, getString(R.string.main_settings));
        MenuButton btnCredits = new MenuButton(this, mbs, getString(R.string.main_credits));
        MenuButton btnQuitGame = new MenuButton(this, mbs, getString(R.string.main_quitgame));

        mainMenu.AddMenuButton(btnNewGame);
        mainMenu.AddMenuButton(btnScoreboard);
        mainMenu.AddMenuButton(btnSettings);
        mainMenu.AddMenuButton(btnCredits);
        mainMenu.AddMenuButton(btnQuitGame);

    }

    void deleteMenu(LinearLayout ll)
    {
        ll.removeAllViewsInLayout();
    }

    static int DpToPixels(int dp, float scale)
    {
        return (int) (dp * scale + 0.5f);
    }

    static int PixelsToDp(int px, float scale)
    {
        return (int) ((px/scale) + 0.5f);
    }

    static int colorHexWithAlpha (int colorHex, float alpha)
    {
        int alphaHex = Math.round(alpha * 255);
        int r = Color.red(colorHex);
        int g = Color.green(colorHex);
        int b = Color.blue(colorHex);
        return Color.argb(alphaHex, r, g, b);
    }
}
