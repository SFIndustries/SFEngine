package com.example.luka.openglestest.engine;

/**
 * Created by Jasmin on 31.8.2016..
 */

public final class MatrixOperations
{
    public static float[][] multiply(float[][] m1, float[][] m2)
    {
        int m1ColLength = m1[0].length; // m1 columns length
        int m2RowLength = m2.length;    // m2 rows length

        if(m1ColLength != m2RowLength) return null; // matrix multiplication is not possible

        int mResRowLength = m1.length;    // m result rows length
        int mResColLength = m2[0].length; // m result columns length

        float[][] mResult = new float[mResRowLength][mResColLength];

        for(int i = 0; i < mResRowLength; i++)
        {         // rows from m1
            for(int j = 0; j < mResColLength; j++)
            {     // columns from m2
                for(int k = 0; k < m1ColLength; k++)
                { // columns from m1
                    mResult[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mResult;
    }

    public static float[][] multiplyByScalar(float[][] m1, float a)
    {
        int mResRowLength = m1.length;    // m result rows length
        int mResColLength = m1[0].length; // m result columns length

        float[][] mResult = new float[mResRowLength][mResColLength];

        for(int i = 0; i < mResRowLength; i++)
        {
            for(int j = 0; j < mResColLength; j++)
            {
                mResult[i][j] = m1[i][j] * a;
            }
        }
        return mResult;
    }
}
