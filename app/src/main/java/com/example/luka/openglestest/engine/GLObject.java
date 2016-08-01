package com.example.luka.openglestest.engine;

import android.content.Context;

import com.example.luka.openglestest.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

/**
 * Created by Luka on 1.8.2016..
 */
public class GLObject
{
    float[] vertices, normals, UVs;
    FloatBuffer vertexBuffer, normalBuffer, UVbuffer;

    private static final int BYTES_PER_FLOAT = 4;


    int textureID;

    private float[] modelMatrix = new float[16];
    private float[] translationMatrix = new float[16];
    private float[] rotationMatrix = new float[16];
    private float[] scaleMatrix = new float[16];

    public GLObject( Context context, int resourceId, int textureIDp )
    {
        List tempList = TextResourceReader.LoadObjFromResource( context, resourceId );
        vertices =  (float[]) tempList.get(0);
        normals = (float[]) tempList.get(1);
        UVs = (float[]) tempList.get(2);

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

        textureID = textureIDp;

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
        multiplyMM(modelMatrix, 0, modelMatrix, 0, translationMatrix, 0);
        multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0);
        multiplyMM(modelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);

        multiplyMM(GLCommon.viewProjectionMatrix, 0, GLCommon.projectionMatrix, 0, GLCommon.viewMatrix, 0);
        multiplyMM(GLCommon.modelViewProjectionMatrix, 0, GLCommon.viewProjectionMatrix, 0, modelMatrix, 0);

        glUniformMatrix4fv(GLCommon.modelViewProjectionMatrixLocation, 1, false, GLCommon.modelViewProjectionMatrix, 0);
        glUniformMatrix4fv(GLCommon.viewMatrixLocation, 1, false, GLCommon.viewMatrix, 0);
        glUniformMatrix4fv(GLCommon.modelMatrixLocation, 1, false, modelMatrix, 0);

        glUniform3f( GLCommon.eyePositionLocation, GLCommon.eyePosition[0], GLCommon.eyePosition[1], GLCommon.eyePosition[2] );
        glUniform3f( GLCommon.lightPositionLocation, GLCommon.lightPosition[0], GLCommon.lightPosition[1], GLCommon.lightPosition[2] );

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
    }

    public void TranslateTo( float x, float y, float z )
    {
        setIdentityM(translationMatrix, 0);
        translateM(translationMatrix, 0, x, y, z );
    }

    public void Rotate( float angle, float xAxis, float yAxis, float zAxis )
    {
        rotateM( rotationMatrix, 0, angle, xAxis, yAxis, zAxis );
    }

    public void Scale( float x, float y, float z )
    {
        scaleM( scaleMatrix, 0, x, y, z );
    }

}
