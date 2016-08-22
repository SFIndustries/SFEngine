package com.example.luka.openglestest.engine;

import com.example.luka.openglestest.MojRenderer;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;

/**
 * Created by Luka on 2.8.2016..
 */
public class Controls
{
    public static float dY, dX;
    static float rollSensitivity = 40;
    static float pitchSensitivity = 40;
    static float alpha = /*0.03f*/ 0.1f, rollAlpha, pitchAlpha;

    static public float[] acceleration = new float[3], accelerationTm1 = new float[3], acceleratonInit = new float[3];
    static public boolean accelerationInitBool = true;
    static float[] tempMatrix = new float[16], pitchMatrix = new float[16];
    static float[] tempVector = new float[4];

    public static final Object mutex = new Object();

    static GLObject controlledObject;

    public static boolean accelerationControlledObject = false;
    public static boolean decelerationControlledObject = false;


    public static void SetControlledObject( GLObject object )
    {
        controlledObject = object;
    }

    public static void GetOrientationFromAcceleration(float[] acceleration )
    {
        if ( accelerationInitBool )
        {
            acceleratonInit = acceleration.clone();
            dX = 0;
            accelerationInitBool = false;
        }

        if ( MojRenderer.plane == null )
            return;

        acceleration[0] -= acceleratonInit[0];
        acceleration[1] -= acceleratonInit[1];

        acceleration = ExponentialSmoothing( acceleration, accelerationTm1, alpha );

        dY = -acceleration[1] % 360/** rollSensitivity*/;
        dX = -acceleration[0] % 360/** pitchSensitivity*/;

        accelerationTm1 = acceleration;
    }

    public static void SetOrientation(  ) // 58 fps na istoj dretvi
    {
        // orijentacija uredaja daje promjenu orijentacije aviona

        android.opengl.Matrix.setRotateM(
                tempMatrix, 0, Controls.dY, controlledObject.zAxis[0], controlledObject.zAxis[1], controlledObject.zAxis[2]);
        multiplyMM(controlledObject.rotationMatrix, 0, tempMatrix, 0, controlledObject.rotationMatrix, 0);
        multiplyMV(tempVector, 0, tempMatrix, 0, controlledObject.xAxis, 0);

        android.opengl.Matrix.setRotateM(tempMatrix, 0, Controls.dX, tempVector[0], tempVector[1], tempVector[2]);
        multiplyMM(controlledObject.rotationMatrix, 0, tempMatrix, 0, controlledObject.rotationMatrix, 0);

        tempVector = controlledObject.initOrientation.clone();
        multiplyMV(controlledObject.orientation, 0, controlledObject.rotationMatrix, 0, tempVector, 0);

        controlledObject.velocity[0] = controlledObject.orientation[0] * controlledObject.velocityScalar;
        controlledObject.velocity[1] = controlledObject.orientation[1] * controlledObject.velocityScalar;
        controlledObject.velocity[2] = controlledObject.orientation[2] * controlledObject.velocityScalar;

        multiplyMV(controlledObject.xAxis, 0, controlledObject.rotationMatrix, 0, controlledObject.xAxisInit, 0);
        multiplyMV(controlledObject.yAxis, 0, controlledObject.rotationMatrix, 0, controlledObject.yAxisInit, 0);
        multiplyMV(controlledObject.zAxis, 0, controlledObject.rotationMatrix, 0, controlledObject.zAxisInit, 0);

    }

    public static float[] ExponentialSmoothing( float[] xt, float[] stm1, float alpha )
    {
        if ( stm1 == null )
            return xt;
        for ( int i=0; i<xt.length; i++ ) {
            //stm1[i] = stm1[i] + alpha * (xt[i] - stm1[i]);
            stm1[i] = alpha * xt[i] + (1 - alpha) * stm1[i];
        }
        return stm1;
    }

}
