package com.example.luka.openglestest.engine;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

/**
 * Created by Jasmin on 28.7.2016..
 */

public class MainMenu extends LinearLayout
{
    LinearLayout llMenuL, llMenu, llMenuR;

    public void AddMenuButton (MenuButton menuBtn)
    {
        this.llMenu.addView(menuBtn);
    }

    public MainMenu (Context context, LinearLayout llRoot, int color)
    {
        super(context);

        this.setOrientation(LinearLayout.HORIZONTAL);

        llRoot.setWeightSum(1.0f);
        llRoot.setBackgroundColor(color);

        LinearLayout.LayoutParams llParams;

        llMenu = new LinearLayout(context);
        llParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.3f);
        llMenu.setLayoutParams(llParams);
        llMenu.setOrientation(LinearLayout.VERTICAL);
        llMenu.setGravity(Gravity.CENTER);
        llMenu.setWeightSum(1.0f);


        llMenuL = new LinearLayout(context);
        llParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f);
        llMenuL.setLayoutParams(llParams);
        llMenuL.setOrientation(LinearLayout.VERTICAL);


        llMenuR = new LinearLayout(context);
        llParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.35f);
        llMenuR.setLayoutParams(llParams);
        llMenuR.setOrientation(LinearLayout.VERTICAL);

        this.addView(llMenuL);
        this.addView(llMenu);
        this.addView(llMenuR);

        this.setTag("mainmenu");
    }
}
