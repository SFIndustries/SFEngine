package com.example.luka.openglestest.engine;

import android.graphics.Typeface;
import android.widget.LinearLayout;

/**
 * Created by Jasmin on 27.7.2016..
 */
public class MenuButtonStyle
{
    int backgroundResource;
    LinearLayout.LayoutParams llParams;
    int textSize;
    int textColor;
    Typeface textFont;

    public MenuButtonStyle(int backgroundResource, LinearLayout.LayoutParams llParams, int textSize, int textColor, Typeface textFont)
    {
        this.backgroundResource = backgroundResource;
        this.llParams = llParams;
        this.textSize = textSize;
        this.textColor = textColor;
        this.textFont = textFont;
    }

    public MenuButtonStyle(int backgroundResource, LinearLayout.LayoutParams llParams, int textSize, int textColor)
    {
        this.backgroundResource = backgroundResource;
        this.llParams = llParams;
        this.textSize = textSize;
        this.textColor = textColor;
    }
}
