package com.example.luka.openglestest;

/**
 * Created by Luka on 17.11.2015..
 */

import android.content.Context;
import android.graphics.Camera;
import android.opengl.GLSurfaceView;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static com.example.luka.openglestest.engine.GLCommon.*;

import com.example.luka.openglestest.engine.Controls;
import com.example.luka.openglestest.engine.GLCamera;
import com.example.luka.openglestest.engine.GLObject;
import com.example.luka.openglestest.engine.GLObjectData;
import com.example.luka.openglestest.engine.TrackCamera;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.setLookAtM;
import static com.example.luka.openglestest.engine.GLCommon.SetEyePosition;

public class MojRenderer implements GLSurfaceView.Renderer
{
    private final Context context;

    private int program;

    float stariX = 0.0f, stariY = 0.0f;

    public GLObjectData sphereData, planeData, gridData;
    int waterTexture, blueTexture, torusTexture, spaceTexture;
    public static GLObject plane, sphere, grid, spaceSphere;
    public static List<GLObject> spheres = new ArrayList<>();

    int directionX = 1, directionY = -1, directionZ = 1;
    float speedX = 0.008f, speedY = 0.005f, speedZ = 0.003f;

    float[] eyePositionTemp = new float[4], upTm1 = new float[4];

    static float[] tempMatrix = new float[16], pitchMatrix = new float[16];
    static float[] tempVector = new float[4];
    float[] tempOrientation = new float[4], tempZAxis = new float[4];

    public MojRenderer(Context context)
    {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);



        waterTexture = LoadTexture(R.raw.voda, context);
        blueTexture = LoadTexture(R.raw.wall, context);
        torusTexture = LoadTexture(R.raw.torus_texture, context);
        //spaceTexture = LoadTexture(R.raw.space_hd, context);
        spaceTexture = LoadTexture(R.raw.space_sphere_texture, context);

        grid = new GLObject(context, R.raw.grid, LoadTexture(R.raw.checkers, context));

        plane = new GLObject(context, R.raw.f16_1_uv, LoadTexture(R.raw.avion_texture, context));
        plane.SetInitOrientation(new float[]{-plane.yAxis[0], -plane.yAxis[1], -plane.yAxis[2], 1});
        plane.velocity = 0.1f;
        plane.Translate(0, 0, 1);
        Controls.SetControlledObject(plane);
        camera = new TrackCamera();
        ((TrackCamera) camera).SetTrackedObject( plane );

        sphereData = new GLObjectData(context, R.raw.sfera, waterTexture);

//        Random rand = new Random();
//        for (int i = 0; i < 500; i++)
//        {
//            sphere = new GLObject( sphereData );
//            sphere.TranslateTo( rand.nextInt((100 - (-100)) + 1) - 100,
//                                rand.nextInt((100 - (-100)) + 1) - 100,
//                                rand.nextInt((100 - (-100)) + 1) - 100);
//            spheres.add(sphere);
//        }


//        sphere = new GLObject( sphereData );
//        sphere.SetTexture( torusTexture );
//        sphere.Translate(1.0f, -3.0f, 0);
//        spheres.add(sphere);
//        sphere = new GLObject( sphereData );
//        sphere.SetTexture( waterTexture );
//        sphere.Translate(0, -5.0f, 0);
//        spheres.add(sphere);
//        sphere = new GLObject( sphereData );
//        sphere.SetTexture( blueTexture );
//        sphere.Translate(-2.0f, -7.0f, 0);
//        spheres.add(sphere);

        spaceSphere = new GLObject( context, R.raw.sfera_unutra, spaceTexture );
        spaceSphere.Rotate(90, 0, 0, 1);

        programPhongTexture = InitProgram(context, R.raw.vertex_shader_phong, R.raw.fragment_shader_phong);
        programTexture = InitProgram(context, R.raw.vertex_shader_texture, R.raw.fragment_shader_texture);

        SetEyePosition( 0, 1.2f, 0.5f ); // iza aviona, avion gleda prema -y

        //glDisable(GL_DITHER);

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                    center[0], center[1], center[2], up[0], up[1], up[2]);

        UseProgram(programTexture);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

//        final float aspectRatio = width > height ?
//                (float) width / (float) height :
//                (float) height / (float) width;
//        if (width > height)
//        {
//        // Landscape
//            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
//        } else
//        {
//        // Portrait or square
//            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
//        }

        perspectiveM(projectionMatrix, 0, fov, (float) width / (float) height, nearZ, farZ);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Showcase();

        //UseProgram( programTexture );
        //SetUniformsShader();

        plane.UpdatePosition();

        Controls.SetOrientation();
        camera.UpdateCamera();

        plane.Draw();

        // pomakni sferu na mjesto aviona

        //grid.Draw();

//        for(GLObject object: spheres)
//        {
//            object.Draw();
//        }

        spaceSphere.TranslateTo( plane.position[0], plane.position[1], plane.position[2] );
        spaceSphere.Draw();

        //sphere.Draw();

        //plane.Rotate(0.5f, 0.0f, 1.0f, 0);
        //plane.Translate(0.0001f, 0.0001f, 0.0001f);

        // mozda glUniform za npr svjetlo tu staviti unaprijed samo jednom
        // jer se ne mijenja za svaki objekt
        // npr
        // nacrtaj scenu()
        // {
        //        glUniform3f( eyePositionLocation, ocisteX, ocisteY, ocisteZ );
        //        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );
        //
        //        za (svaki objekt)
        //          Draw(objekt);
        //
        // }

    }

    public void handleTouchPress(float normalizedX, float normalizedY)
    {
        stariX = normalizedX;
        stariY = normalizedY;
    }

    public void handleTouchDrag(float normalizedX, float normalizedY)
    {
        if (normalizedX > stariX) eyePosition[0] += 10.0f * Math.abs(normalizedX - stariX);
        else if (normalizedX < stariX) eyePosition[0] -= 10.0f * Math.abs(normalizedX - stariX);

        if (normalizedY > stariY) eyePosition[2] += 10.0f * Math.abs(normalizedY - stariY);
        else if (normalizedY < stariY) eyePosition[2] -= 10.0f * Math.abs(normalizedY - stariY);

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2], center[0], center[1], center[2], 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);

        stariX = normalizedX;
        stariY = normalizedY;
    }

    public void Showcase()
    {
        if ( eyePosition[0] < -1 )
            directionX = 1;
        else if ( eyePosition[0] > 1 )
            directionX = -1;
        if ( eyePosition[1] < -1 )
            directionY = 1;
        else if ( eyePosition[1] > 1 )
            directionY = -1;
        if ( eyePosition[2] < -1 )
            directionZ = 1;
        else if ( eyePosition[2] > 1 )
            directionZ = -1;

        eyePosition[0] += directionX * speedX;
        eyePosition[1] += directionY * speedY;
        eyePosition[2] += directionZ * speedZ;

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2], center[0], center[1], center[2], 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);
    }
}


