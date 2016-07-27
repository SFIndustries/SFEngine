package com.example.luka.openglestest;

import android.app.Activity;


import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import android.opengl.GLSurfaceView;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.luka.openglestest.util.MenuButton;
import com.example.luka.openglestest.util.MenuButtonStyle;

public class MainActivity extends Activity {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    float stariX, stariY;

    Resources r;
    DisplayMetrics dm;
    float dmScale;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //glSurfaceView = new GLSurfaceView(this);

        r = getResources();
        dm = r.getDisplayMetrics();
        dmScale = dm.density;

        setContentView(R.layout.activity_main);

        mainMenu();

        try {
            glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        } catch (Exception e) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            glSurfaceView.setRenderer(mojRenderer);


            rendererSet = true;
        } else {
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

                    if (v.getWidth() > v.getHeight()) {
                        normalizedX = (event.getX() / (float) v.getWidth()) * 2 * aspectRatio - aspectRatio;
                        normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);
                    } else {
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
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
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

    void mainMenu() {
        LinearLayout llRoot = (LinearLayout) findViewById(R.id.ll_root);
        llRoot.setBackgroundColor(0x80073763); // blue = 0x80073763, red = 0x80660000, green = 0x80274e13
        //llRoot.setAlpha(0.7f);
        llRoot.setWeightSum(1.0f);

        LinearLayout.LayoutParams llParams;

        LinearLayout llMenu = new LinearLayout(this);
        llParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.3f);
        llMenu.setLayoutParams(llParams);
        llMenu.setOrientation(LinearLayout.VERTICAL);
        llMenu.setGravity(Gravity.CENTER);
        llMenu.setWeightSum(1.0f);

        LinearLayout llMenuL = new LinearLayout(this);
        llParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f);
        llMenuL.setLayoutParams(llParams);
        llMenuL.setOrientation(LinearLayout.VERTICAL);

        LinearLayout llMenuR = new LinearLayout(this);
        llParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f);
        llMenuR.setLayoutParams(llParams);
        llMenuR.setOrientation(LinearLayout.VERTICAL);

        llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.12f);
        llParams.setMargins(0, DpToPixels(6, dmScale), 0, DpToPixels(6, dmScale));
        MenuButtonStyle mbs = new MenuButtonStyle(R.drawable.btn_blue, llParams, DpToPixels(20, dmScale), 0xffffffff); // (int backgroundResource, LinearLayout.LayoutParams llp, int textSize, int textColor)

        MenuButton btnNewGame = new MenuButton(this, mbs, getString(R.string.main_newgame)); // (Context context, MenuButtonStyle mbs, String text)
        MenuButton btnScoreboard = new MenuButton(this, mbs, getString(R.string.main_scoreboard));
        MenuButton btnSettings = new MenuButton(this, mbs, getString(R.string.main_settings));
        MenuButton btnCredits = new MenuButton(this, mbs, getString(R.string.main_credits));
        MenuButton btnQuitGame = new MenuButton(this, mbs, getString(R.string.main_quitgame));

        llMenu.addView(btnNewGame);
        llMenu.addView(btnScoreboard);
        llMenu.addView(btnSettings);
        llMenu.addView(btnCredits);
        llMenu.addView(btnQuitGame);

        llRoot.addView(llMenuL);
        llRoot.addView(llMenu);
        llRoot.addView(llMenuR);

    }

    static int DpToPixels(int dp, float scale)
    {
        return (int) (dp * scale + 0.5f);
    }

    static int PixelsToDp(int px, float scale)
    {
        return (int) ((px/scale) + 0.5f);
    }
}
