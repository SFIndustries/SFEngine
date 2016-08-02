package com.example.luka.openglestest;

/**
 * Created by Luka on 17.11.2015..
 */

import android.content.Context;
import android.opengl.GLSurfaceView;

import static com.example.luka.openglestest.engine.GLCommon.*;

import com.example.luka.openglestest.engine.Controls;
import com.example.luka.openglestest.engine.GLObject;

import java.util.ArrayList;
import java.util.List;

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

    public static GLObject plane, sphere, grid;
    public static List<GLObject> spheres = new ArrayList<>();

    int directionX = 1, directionY = -1, directionZ = 1;
    float speedX = 0.008f, speedY = 0.005f, speedZ = 0.003f;

    public MojRenderer(Context context)
    {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        grid = new GLObject(context, R.raw.grid, LoadTexture(R.raw.checkers, context));

        plane = new GLObject(context, R.raw.f16_1_uv, LoadTexture(R.raw.torus_texture, context));
        plane.SetInitOrientation( new float[]{ -plane.yAxis[0], -plane.yAxis[1], -plane.yAxis[2], 1} );
        plane.velocity = 0.02f;
        plane.Translate(0,0,1);
        Controls.SetControlledObject( plane );

        sphere = new GLObject(context, R.raw.sfera, plane.textureID);
        sphere.Translate(1.0f, -3.0f, 0);
//        spheres.add(sphere);
//        sphere = new GLObject(context, R.raw.sfera, plane.textureID);
//        sphere.Translate(0, -5.0f, 0);
//        spheres.add(sphere);
//        sphere = new GLObject(context, R.raw.sfera, plane.textureID);
//        sphere.Translate(-1.0f, -7.0f, 0);
//        spheres.add(sphere);
//        sphere = new GLObject(context, R.raw.sfera, plane.textureID);
//        sphere.Translate(0, -9.0f, 1.0f);
//        spheres.add(sphere);
//        sphere = new GLObject(context, R.raw.sfera, plane.textureID);
//        sphere.Translate(0, -11.0f, 0);
//        spheres.add(sphere);

        InitShaders(context, R.raw.vertex_shader_phong, R.raw.fragment_shader_phong);

        SetEyePosition( 0, 1.2f, 0.5f ); // iza aviona, avion gleda prema -y

        //glDisable(GL_DITHER);

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                    center[0], center[1], center[2], up[0], up[1], up[2]);
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

        SetUniformsShader();

        plane.UpdatePosition();
        SetEyePosition( plane.position[0], plane.position[1] + 1.2f, plane.position[2] + 1.5f );
        //SetCenter( eyePosition[0] + plane.orientation[0], eyePosition[1] + plane.orientation[1], eyePosition[2] + plane.orientation[2] );
        SetCenter( plane.position[0], plane.position[1], plane.position[2] );

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                center[0], center[1], center[2], up[0], up[1], up[2]);

        grid.Draw();
        plane.Draw();
        for(GLObject object: spheres)
        {
            object.Draw();
        }

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


//        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
//        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
//
//        glUniformMatrix4fv(uMatrixLocation, 1, false, modelViewProjectionMatrix, 0);
//        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);
//        glUniformMatrix4fv(modelMatrixLocation, 1, false, modelMatrix, 0);
//
//        glUniform3f( eyePositionLocation, ocisteX, ocisteY, ocisteZ );
//        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );
//
//        glActiveTexture(GL_TEXTURE0);
//        // Set our "myTextureSampler" sampler to user Texture Unit 0
//        //glBindTexture(GL_TEXTURE_2D, textureID);
//        glUniform1i(textureUniformLocation, 0); // texture unit 0
//
//        glDrawArrays(GL_TRIANGLES, 0, vrhoviObjekta.length / POSITION_COMPONENT_COUNT);
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


