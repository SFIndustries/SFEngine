package com.example.luka.openglestest.engine;

import android.content.Context;

import com.example.luka.openglestest.MainActivity;
import com.example.luka.openglestest.util.TextResourceReader;
import static com.example.luka.openglestest.engine.GLCommon.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GLObject extends GLObjectData
{
    FloatBuffer vertexBuffer, normalBuffer, UVbuffer;

    static final int BYTES_PER_FLOAT = 4;

    private float[] modelMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    public float[] rotationMatrix = new float[16];
    private float[] scaleMatrix = new float[16];

    public float[] xAxis = {1.0f, 0, 0, 1}, yAxis = {0, 1.0f, 0, 1}, zAxis = {0, 0, 1.0f, 1};
    public float[] orientation = new float[4], initOrientation = new float[4];

    public float[] position = {0, 0, 0, 1}, initPosition = {0, 0, 0, 1};
    public float velocity;

    public GLObject( Context context, int resourceId, int textureIDp )
    {
        super( context, resourceId, textureIDp );

        InitBuffers();
        InitMatrices();
    }

    public GLObject( GLObjectData object )
    {
        vertices = object.vertices.clone();
        normals = object.normals.clone();
        UVs = object.UVs.clone();

        textureID = object.textureID;

        InitBuffers();
        InitMatrices();
    }

    public void InitBuffers()
    {
        vertexBuffer = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices);

        normalBuffer = ByteBuffer
                .allocateDirect(normals.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        normalBuffer.put(normals);

        UVbuffer = ByteBuffer
                .allocateDirect(UVs.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        UVbuffer.put(UVs);
    }

    public void InitMatrices()
    {
        setIdentityM(translationMatrix, 0);
        setIdentityM(rotationMatrix, 0);
        setIdentityM(scaleMatrix, 0);
        setIdentityM(modelMatrix, 0);
    }

    public void SetTexture( int textureIDp )
    {
        textureID = textureIDp;
    }

    public void Draw()
    {
        setIdentityM(modelMatrix, 0);
        //multiplyMM(modelMatrix, 0, modelMatrix, 0, translationMatrix, 0);
        //multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0);
        //multiplyMM(modelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);

        multiplyMM(modelMatrix, 0, scaleMatrix, 0, modelMatrix, 0); // 1. ili 3.?
        synchronized(Controls.mutex)
        {
            multiplyMM(modelMatrix, 0, rotationMatrix, 0, modelMatrix, 0); // 2.
        }
        multiplyMM(modelMatrix, 0, translationMatrix, 0, modelMatrix, 0); // 1. ili 3.?

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

        glUniformMatrix4fv(modelViewProjectionMatrixLocation, 1, false, modelViewProjectionMatrix, 0);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);
        glUniformMatrix4fv(modelMatrixLocation, 1, false, modelMatrix, 0);

//        glUniform3f( eyePositionLocation, eyePosition[0], eyePosition[1], eyePosition[2] );
//        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );

        glBindTexture(GL_TEXTURE_2D, textureID);
        glActiveTexture(GL_TEXTURE0);
        glUniform1i(textureID, 0);

        vertexBuffer.position(0);
        glVertexAttribPointer(GLCommon.aPositionLocation, 3, GL_FLOAT, false, 0, vertexBuffer);
        normalBuffer.position(0);
        glVertexAttribPointer(GLCommon.aNormalLocation, 3, GL_FLOAT, false, 0, normalBuffer);
        UVbuffer.position(0);
        glVertexAttribPointer(GLCommon.aTextureLocation, 2, GL_FLOAT, false, 0, UVbuffer);

        glDrawArrays(GL_TRIANGLES, 0, vertices.length / 3);
    }

    public void Translate( float x, float y, float z )
    {
        translateM(translationMatrix, 0, x, y, z );
        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);
    }

    public void TranslateTo( float x, float y, float z )
    {
        setIdentityM(translationMatrix, 0);
        translateM(translationMatrix, 0, x, y, z );
        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);
    }

    public void Rotate( float angle, float xAxis, float yAxis, float zAxis )
    {
        rotateM( rotationMatrix, 0, angle, xAxis, yAxis, zAxis );
    }

//    public void Scale( float x, float y, float z )
//    {
//        scaleM( scaleMatrix, 0, x, y, z );
//    }

    public void SetInitOrientation(float[] vector )
    {
        initOrientation = vector.clone();
    }

    public void UpdatePosition()
    {
        Translate( velocity * orientation[0], velocity * orientation[1], velocity * orientation[2] );

    }

}
