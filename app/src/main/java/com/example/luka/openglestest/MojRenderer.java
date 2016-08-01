package com.example.luka.openglestest;

/**
 * Created by Luka on 17.11.2015..
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.example.luka.openglestest.engine.GLCommon;
import com.example.luka.openglestest.engine.GLObject;
import com.example.luka.openglestest.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.*;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

public class MojRenderer implements GLSurfaceView.Renderer
{
    private final Context context;

    private int program;

    float stariX = 0.0f, stariY = 0.0f;

    GLObject plane;

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

        plane = new GLObject(context, R.raw.f16_1_uv, GLCommon.LoadTexture(R.raw.torus_texture, context));

        GLCommon.InitShaders(context, R.raw.vertex_shader_phong, R.raw.fragment_shader_phong);

        //glDisable(GL_DITHER);

        setLookAtM( GLCommon.viewMatrix, 0, GLCommon.eyePosition[0], GLCommon.eyePosition[1], GLCommon.eyePosition[2],
                    GLCommon.center[0], GLCommon.center[1], GLCommon.center[2], 0.0f, 0.0f, 1.0f);
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

        perspectiveM(GLCommon.projectionMatrix, 0, GLCommon.fov, (float) width / (float) height, GLCommon.nearZ, GLCommon.farZ);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Showcase();

        plane.Draw();

        // mozda glUniform za npr svjetlo tu staviti unaprijed samo jednom j
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
        if (normalizedX > stariX) GLCommon.eyePosition[0] += 10.0f * Math.abs(normalizedX - stariX);
        else if (normalizedX < stariX) GLCommon.eyePosition[0] -= 10.0f * Math.abs(normalizedX - stariX);

        if (normalizedY > stariY) GLCommon.eyePosition[2] += 10.0f * Math.abs(normalizedY - stariY);
        else if (normalizedY < stariY) GLCommon.eyePosition[2] -= 10.0f * Math.abs(normalizedY - stariY);

        setLookAtM( GLCommon.viewMatrix, 0, GLCommon.eyePosition[0], GLCommon.eyePosition[1], GLCommon.eyePosition[2], GLCommon.center[0], GLCommon.center[1], GLCommon.center[2], 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(GLCommon.viewMatrixLocation, 1, false, GLCommon.viewMatrix, 0);

        stariX = normalizedX;
        stariY = normalizedY;
    }

    public void Showcase()
    {
        if ( GLCommon.eyePosition[0] < -1 )
            directionX = 1;
        else if ( GLCommon.eyePosition[0] > 1 )
            directionX = -1;
        if ( GLCommon.eyePosition[1] < -1 )
            directionY = 1;
        else if ( GLCommon.eyePosition[1] > 1 )
            directionY = -1;
        if ( GLCommon.eyePosition[2] < -1 )
            directionZ = 1;
        else if ( GLCommon.eyePosition[2] > 1 )
            directionZ = -1;

        GLCommon.eyePosition[0] += directionX * speedX;
        GLCommon.eyePosition[1] += directionY * speedY;
        GLCommon.eyePosition[2] += directionZ * speedZ;

        setLookAtM( GLCommon.viewMatrix, 0, GLCommon.eyePosition[0], GLCommon.eyePosition[1], GLCommon.eyePosition[2], GLCommon.center[0], GLCommon.center[1], GLCommon.center[2], 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(GLCommon.viewMatrixLocation, 1, false, GLCommon.viewMatrix, 0);
    }
}


