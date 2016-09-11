package com.example.luka.openglestest.engine;

import java.util.ArrayList;
import java.util.List;

import android.opengl.Matrix;
import android.support.annotation.NonNull;

/**
 * Created by Jasmin on 28.8.2016..
 */

public class BSplineCurve extends Curve
{
    public List<float[]> dataPoints, tangentPoints;
    float deltaT;

    public BSplineCurve(List<float[]> _points, float _deltaT)
    {
        dataPoints = new ArrayList<float[]>(_points);

        if (_deltaT > 0) deltaT = _deltaT;
        else deltaT = 0.01f;

        points = findBspline();
        tangentPoints = findBsplineTangent();
    }

    public BSplineCurve(Curve _curve, float _deltaT)
    {
        dataPoints = new ArrayList<float[]>(_curve.points);

        if (_deltaT > 0) deltaT = _deltaT;
        else deltaT = 0.01f;

        points = findBspline();
        tangentPoints = findBsplineTangent();

    }

    List<float[]> findBspline()
    {
        List<float[]> resPoints = new ArrayList<float[]>();
        float a = 1.0f/6.0f;

        // float[] BVector = new float[]{-1 / 6, 3 / 6, -3 / 6, 1 / 6, 3 / 6, -1, 0, -4 / 6, -3 / 6, 3 / 6, 3 / 6, 1 / 6, 1 / 6, 0, 0, 0};
        float[][] BMatrix = new float[][]{{-1, 3, -3, 1},{3, -6, 3, 0},{-3, 0, 3, 0}, {1, 4, 1, 0}};
        BMatrix = MatrixOperations.multiplyByScalar(BMatrix, a);

        for (int i = 0; i < dataPoints.size() - 3; i++)
        {
            // float[] RVector = new float[16];
            float[][] RMatrix = new float[4][4];

            // int last = 0;

            for (int j = 0; j < 4; j++)
            {
                RMatrix[0][j] = dataPoints.get(i)[j];
                RMatrix[1][j] = dataPoints.get(i+1)[j];
                RMatrix[2][j] = dataPoints.get(i+2)[j];
                RMatrix[3][j] = dataPoints.get(i+3)[j];

                /*
                RVector[last] = dataPoints.get(i)[j];
                RVector[last + 1] = dataPoints.get(i + 1)[j];
                RVector[last + 2] = dataPoints.get(i + 2)[j];
                RVector[last + 3] = dataPoints.get(i + 3)[j];
                last += 4;
                */
            }


            List<Float> tList = findTRange01(deltaT);

            for (float t : tList)
            {
                //float[] TVector = new float[]{(float) Math.pow(t, 3), (float) Math.pow(t, 2), t, 1};
                float[][] TVector = new float[][]{{(float) Math.pow(t, 3), (float) Math.pow(t, 2), t, 1}};

                //float[] resTemp = new float[16];
                float[][] res;
                //Matrix.multiplyMM(resTemp, 0, BVector, 0, RVector, 0); // B*R

                res = MatrixOperations.multiply(BMatrix, RMatrix); // B*R
                res = MatrixOperations.multiply(TVector, res);


                resPoints.add(res[0]);
            }
        }

        return resPoints;

    }


    @NonNull
    static List<Float> findTRange01(float deltaT)
    {
        List<Float> tList = new ArrayList<Float>();
        float tempT = 0.0f;

        while (tempT <= 1.0f)
        {
            tList.add(tempT);
            tempT += deltaT;
        }
        return tList;
    }


    List<float[]> findBsplineTangent()
    {
        List<float[]> resPoints = new ArrayList<float[]>();
        float[][] BMatrix = new float[][]{{-1, 3, -3, 1}, {2, -4, 2, 0}, {-1, 0, 1, 0}};
        BMatrix = MatrixOperations.multiplyByScalar(BMatrix, 0.5f);

        for (int i = 0; i < dataPoints.size() - 3; i++)
        {
            float[][] RMatrix = new float[4][4];

            for (int j = 0; j < 4; j++)
            {
                RMatrix[0][j] = dataPoints.get(i)[j];
                RMatrix[1][j] = dataPoints.get(i + 1)[j];
                RMatrix[2][j] = dataPoints.get(i + 2)[j];
                RMatrix[3][j] = dataPoints.get(i + 3)[j];
            }

            List<Float> tList = findTRange01(deltaT);

            for (float t : tList)
            {
                float[][] TVector = new float[][]{{(float) Math.pow(t, 2), t, 1}};

                float[][] res;

                res = MatrixOperations.multiply(BMatrix, RMatrix); // B*R
                res = MatrixOperations.multiply(TVector, res);


                resPoints.add(res[0]);
            }
        }

        return resPoints;
    }


    public static float[] findAxisOfRotation(float[] initialRotation, float[] desiredRotation) // (s, e)
    {
        float x = initialRotation[1]*desiredRotation[2] - desiredRotation[1]*initialRotation[2];
        float y = -1 * (initialRotation[0]*desiredRotation[2] - desiredRotation[0]*initialRotation[2]);
        float z = initialRotation[0]*desiredRotation[1] - initialRotation[1]*desiredRotation[0];

        return new float[]{x, y, z};
    }

    public static float findAngleOfRotation(float[] initialRotation, float[] desiredRotation)
    {
        float dotProduct = initialRotation[0]*desiredRotation[0];
        dotProduct += initialRotation[1]*desiredRotation[1];
        dotProduct += initialRotation[2]*desiredRotation[2];

        float normProduct = Matrix.length(initialRotation[0], initialRotation[1], initialRotation[2]);
        normProduct *= Matrix.length(desiredRotation[0], desiredRotation[1], desiredRotation[2]);

        float res = (float) Math.toDegrees(Math.acos((double)dotProduct/normProduct));
        return res;
    }

}
