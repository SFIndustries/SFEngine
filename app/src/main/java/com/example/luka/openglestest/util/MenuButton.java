package com.example.luka.openglestest.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Button;

/**
 * Created by Jasmin on 27.7.2016..
 */
public class MenuButton extends Button
{
    public MenuButton(Context context, MenuButtonStyle mbs, String text)
    {
        super(context);

        this.setBackgroundResource(mbs.backgroundResource);
        this.setLayoutParams(mbs.llParams);
        this.setText(text);
        this.setTextColor(mbs.textColor);

        this.setTextSize((float) mbs.textSize);
    }
}
