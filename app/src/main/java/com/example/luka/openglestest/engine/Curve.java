package com.example.luka.openglestest.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jasmin on 28.8.2016..
 */

public class Curve
{
    public List<float[]> points;

    Curve ()
    {
        points = new ArrayList<float[]>();
    }

    Curve (List<float[]> _points)
    {
        points = new ArrayList<float[]>(_points);
    }
}
