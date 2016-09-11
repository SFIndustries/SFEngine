package com.example.luka.openglestest.engine;

import static android.opengl.Matrix.setLookAtM;
import static com.example.luka.openglestest.engine.GLCommon.SetCenter;
import static com.example.luka.openglestest.engine.GLCommon.SetEyePosition;
import static com.example.luka.openglestest.engine.GLCommon.center;
import static com.example.luka.openglestest.engine.GLCommon.centerTm1;
import static com.example.luka.openglestest.engine.GLCommon.eyePosition;
import static com.example.luka.openglestest.engine.GLCommon.eyePositionSmooth;
import static com.example.luka.openglestest.engine.GLCommon.eyePositionSmoothTm1;
import static com.example.luka.openglestest.engine.GLCommon.eyePositionTm1;
import static com.example.luka.openglestest.engine.GLCommon.up;
import static com.example.luka.openglestest.engine.GLCommon.upTm1;
import static com.example.luka.openglestest.engine.GLCommon.viewMatrix;

public class TrackCamera extends GLCamera
{
    float trackOrientaionEyeX = 0, trackOrientaionEyeY = /*-1.0f*/ -4f, trackOrientaionEyeZ = /*0.5f*/2f;
    float trackOrientaionCenterX = 0, trackOrientaionCenterY = 1.0f, trackOrientaionCenterZ = 0;

    public float cameraAlpha = 0.05f;

    public void SetTrackedObject( GLObject object )
    {
        trackedObject = object;
    }

    public void UpdateCamera()
    {
        for( int i = 0; i < eyePosition.length; i++ )
        {
            eyePosition[i] =    trackedObject.position[i]; //+

//                    trackedObject.xAxis[i]*trackOrientaionEyeX +
//                    trackedObject.orientation[i]*trackOrientaionEyeY +
//                    trackedObject.zAxis[i]*trackOrientaionEyeZ;
        }

        for( int i = 0; i < eyePosition.length; i++ )
        {
            eyePositionSmooth[i] = trackedObject.xAxis[i]*trackOrientaionEyeX +
                    trackedObject.orientation[i]*trackOrientaionEyeY +
                    trackedObject.zAxis[i]*trackOrientaionEyeZ;
        }

        up = trackedObject.zAxis.clone();
        up = Controls.ExponentialSmoothing(up, upTm1, cameraAlpha);
        upTm1 = up.clone();

        //eyePosition = Controls.ExponentialSmoothing(eyePosition, eyePositionTm1, cameraAlpha);
        eyePositionSmooth = Controls.ExponentialSmoothing(eyePositionSmooth, eyePositionSmoothTm1, cameraAlpha);
        SetEyePosition(eyePosition[0]+eyePositionSmooth[0], eyePosition[1]+eyePositionSmooth[1], eyePosition[2]+eyePositionSmooth[2]);
        eyePositionTm1 = eyePosition.clone();
        eyePositionSmoothTm1 = eyePositionSmooth.clone();

        for( int i = 0; i < center.length; i++ )
        {
            center[i] = trackedObject.position[i] +
                    trackedObject.xAxis[i]*trackOrientaionCenterX +
                    trackedObject.orientation[i]*trackOrientaionCenterY +
                    trackedObject.zAxis[i]*trackOrientaionCenterZ;
        }

        //center = Controls.ExponentialSmoothing(center, centerTm1, cameraAlpha);
        SetCenter( center[0], center[1], center[2] );
        centerTm1 = center.clone();

        setLookAtM(viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                center[0], center[1], center[2], up[0], up[1], up[2] );
    }
}
