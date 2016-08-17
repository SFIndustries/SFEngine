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

public class GLObject extends GLObjectData
{
    FloatBuffer vertexBuffer, normalBuffer, UVbuffer;
    //FloatBuffer interleavedBuffer;
    //int dataLength;
    public int bufferIndexTemp;

    int renderMode = TEXTURE, renderModePrevious;

    public float[] modelMatrix = new float[16];
    public float[] translationMatrix = new float[16];
    public float[] rotationMatrix = new float[16];
    public float[] scaleMatrix = new float[16];

    public float[]  xAxis = {1.0f, 0, 0, 1}, yAxis = {0, 1.0f, 0, 1}, zAxis = {0, 0, 1.0f, 1},
                    xAxisInit = {1.0f, 0, 0, 1}, yAxisInit = {0, 1.0f, 0, 1}, zAxisInit = {0, 0, 1.0f, 1} ;
    public float[] orientation = new float[4], initOrientation = new float[4];

    public float[] position = {0, 0, 0, 1}, initPosition = {0, 0, 0, 1};
    public float velocity;

    public float alpha = 1.0f;


    public GLObject() {}

    public GLObject( Context context, int resourceId, int textureIDp )
    {
        super( context, resourceId, textureIDp );

        dataLength = vertices.length + normals.length + UVs.length;

        //InitBuffersClientSide();
        InitBuffersVBO();
        InitMatrices();
    }

    public GLObject( GLObjectData object )
    {

        vertices = object.vertices.clone();
        normals = object.normals.clone();
        UVs = object.UVs.clone();

        //dataLength = vertices.length + normals.length + UVs.length;

        bufferIndex = object.bufferIndex;

        textureID = object.textureID;

        //InitBuffersClientSide();
        //InitBuffersVBO();
        InitMatrices();
    }

    public void InitBuffersClientSide()
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

    public void InitBuffersVBO()
    {
        interleavedBuffer = ByteBuffer.allocateDirect(dataLength * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        int vertexOffset = 0;
        int normalOffset = 0;
        int UVoffset = 0;

        for (int i = 0; i < vertices.length / 3; i++)
        {
            interleavedBuffer.put(vertices, vertexOffset, VERTEX_DATA_SIZE);
            vertexOffset += VERTEX_DATA_SIZE;
            interleavedBuffer.put(normals, normalOffset, NORMAL_DATA_SIZE);
            normalOffset += NORMAL_DATA_SIZE;
            interleavedBuffer.put(UVs, UVoffset, UV_DATA_SIZE);
            UVoffset += UV_DATA_SIZE;
        }

        interleavedBuffer.position(0);

        final int buffers[] = new int[1];
        glGenBuffers(1, buffers, 0);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ARRAY_BUFFER, interleavedBuffer.capacity() * BYTES_PER_FLOAT, interleavedBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        bufferIndex = buffers[0];

        interleavedBuffer.limit(0);
        interleavedBuffer = null;
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

    public void SetRenderMode( int renderModep )
    {
        renderMode = renderModep;
    }

    public void Draw( /*int bufferIndexp*/ )

    // TODO - koristiti isti VBO za crtanje objekata istog tipa (npr. sfera)
    // TODO objekti ce se razlikovati po matricama i aktivnoj teksturi

    {
        if ( renderMode != GLCommon.renderMode )
        {
            renderModePrevious = GLCommon.renderMode;
            UseProgram( renderMode );
        }

        setIdentityM(modelMatrix, 0);
        multiplyMM(modelMatrix, 0, scaleMatrix, 0, modelMatrix, 0); // 1. ili 3.?
        multiplyMM(modelMatrix, 0, rotationMatrix, 0, modelMatrix, 0); // 2.
        multiplyMM(modelMatrix, 0, translationMatrix, 0, modelMatrix, 0); // 1. ili 3.?

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

        glUniformMatrix4fv(modelViewProjectionMatrixLocation, 1, false, modelViewProjectionMatrix, 0);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);
        glUniformMatrix4fv(modelMatrixLocation, 1, false, modelMatrix, 0);

        glUniform1f( alphaLocation, alpha );

//        glUniform3f( eyePositionLocation, eyePosition[0], eyePosition[1], eyePosition[2] );
//        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );

        glBindTexture(GL_TEXTURE_2D, textureID);

        //TODO - bindati teksture na pocetku za GL_TEXTURE0, GL_TEXTURE1 (i=0,1..., GL_TEXTURE0 + i) itd pa
        //TODO samo postaviti aktivnu teksturu;
        glActiveTexture(GL_TEXTURE0);

        //glUniform1i(textureID, 0);
        glUniform1i(textureUniformLocation, 0); // ovo se mozda treba raditi samo kod inita shadera

        // client side
        //--------------------------------------------------------------------------------------
//        vertexBuffer.position(0);
//        glVertexAttribPointer(GLCommon.aPositionLocation, 3, GL_FLOAT, false, 0, vertexBuffer);
//        normalBuffer.position(0);
//        glVertexAttribPointer(GLCommon.aNormalLocation, 3, GL_FLOAT, false, 0, normalBuffer);
//        UVbuffer.position(0);
//        glVertexAttribPointer(GLCommon.aTextureLocation, 2, GL_FLOAT, false, 0, UVbuffer);
//
//        glDrawArrays(GL_TRIANGLES, 0, vertices.length / 3);
        //--------------------------------------------------------------------------------------

        // VBO
        //--------------------------------------------------------------------------------------

//        if (bufferIndexp == -1)
//        {
//            bufferIndexTemp = bufferIndex;
//        }
//        else bufferIndexTemp = bufferIndexp;

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndex);
        glVertexAttribPointer(aPositionLocation, VERTEX_DATA_SIZE, GL_FLOAT, false, stride, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndex);
        glVertexAttribPointer(aNormalLocation, NORMAL_DATA_SIZE, GL_FLOAT, false, stride, VERTEX_DATA_SIZE * BYTES_PER_FLOAT);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndex);
        glVertexAttribPointer(aTextureLocation, UV_DATA_SIZE, GL_FLOAT, false,
                stride, (VERTEX_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDrawArrays(GL_TRIANGLES, 0, vertices.length / 3);
        //--------------------------------------------------------------------------------------

        //UseProgram( renderModePrevious );
    }


    public void Translate( float x, float y, float z )
    {
        translateM(translationMatrix, 0, x, y, z);
        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);
    }

    public void TranslateTo( float x, float y, float z )
    {
        setIdentityM(translationMatrix, 0);
        translateM(translationMatrix, 0, x, y, z);
        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);
    }

    public void Rotate( float angle, float xAxis, float yAxis, float zAxis )
    {
        rotateM(rotationMatrix, 0, angle, xAxis, yAxis, zAxis);
    }

//    public void Scale( float x, float y, float z )
//    {
//        scaleM( scaleMatrix, 0, x, y, z );
//    }

    public void SetInitOrientation( float[] vector )
    {
        initOrientation = vector.clone();
        orientation = initOrientation.clone();
    }

    public void UpdatePosition()
    {
        Translate( velocity * orientation[0], velocity * orientation[1], velocity * orientation[2] );
    }

    public void InitCollisionObject( Context context, int resourceId )
    {
        List tempList = TextResourceReader.LoadObjFromResource( context, resourceId );
        vertices = (float[]) tempList.get(0);



    }

}



class BoundingSphere
{
    float[] center;
    float radius;

    public BoundingSphere(){}

    public BoundingSphere( float[] pCenter, float pRadius )
    {
        center = pCenter.clone();
        radius = pRadius;
    }

    public static BoundingSphere Calculate( float pVertices )
    {
        return new BoundingSphere();
    }
}
