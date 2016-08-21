package com.example.luka.openglestest.engine;

import android.content.Context;
import android.opengl.Matrix;
import android.renderscript.Float3;

import com.example.luka.openglestest.MainActivity;
import com.example.luka.openglestest.MojRenderer;
import com.example.luka.openglestest.R;
import com.example.luka.openglestest.util.TextResourceReader;
import static com.example.luka.openglestest.engine.GLCommon.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
    public float velocity = 0;

    public float[] colour = {1, 1, 1, 1};
    public float alpha = 1.0f;

    List<BoundingSphere> boundingSpheres = new ArrayList<>();


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
            GLCommon.SetRenderMode( renderMode );
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
        glUniform4f( colourLocation, colour[0], colour[1], colour[2], colour[3] );

        glBindTexture(GL_TEXTURE_2D, textureID);

        //TODO - bindati teksture na pocetku za GL_TEXTURE0, GL_TEXTURE1 (i=0,1..., GL_TEXTURE0 + i) itd pa
        //TODO samo postaviti aktivnu teksturu;
        glActiveTexture(GL_TEXTURE0);

        //glUniform1i(textureID, 0);
        glUniform1i(textureUniformLocation, 0); // ovo se mozda treba raditi samo kod inita shadera
        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );

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

        if ( renderMode != renderModePrevious )
            GLCommon.SetRenderMode( renderModePrevious );
    }

    public void DrawWireframe( /*int bufferIndexp*/ )
    {
        if ( renderMode != GLCommon.renderMode )
        {
            renderModePrevious = GLCommon.renderMode;
            GLCommon.SetRenderMode( renderMode );
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
        glUniform4f( colourLocation, colour[0], colour[1], colour[2], colour[3] );

        glBindTexture(GL_TEXTURE_2D, textureID);

        glActiveTexture(GL_TEXTURE0);

        glUniform1i(textureUniformLocation, 0); // ovo se mozda treba raditi samo kod inita shadera
        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndex);
        glVertexAttribPointer(aPositionLocation, VERTEX_DATA_SIZE, GL_FLOAT, false, stride, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndex);
        glVertexAttribPointer(aNormalLocation, NORMAL_DATA_SIZE, GL_FLOAT, false, stride, VERTEX_DATA_SIZE * BYTES_PER_FLOAT);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndex);
        glVertexAttribPointer(aTextureLocation, UV_DATA_SIZE, GL_FLOAT, false,
                stride, (VERTEX_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDrawArrays(GL_LINE_STRIP, 0, vertices.length / 2);
        //--------------------------------------------------------------------------------------

        if ( renderMode != renderModePrevious )
            GLCommon.SetRenderMode( renderModePrevious );
    }

    public void Translate( float x, float y, float z )
    {
        translateM(translationMatrix, 0, x, y, z);
        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);

        if (boundingSpheres.size() == 0)
            return;
        for(BoundingSphere b : boundingSpheres)
            multiplyMV(b.center, 0, rotationMatrix, 0, b.initCenter, 0);
        for(int i = 0; i<boundingSpheres.size(); i++)
            multiplyMV(boundingSpheres.get(i).center, 0, translationMatrix, 0, boundingSpheres.get(i).center, 0);

    }

    public void TranslateTo( float x, float y, float z )
    {
        setIdentityM(translationMatrix, 0);
        translateM(translationMatrix, 0, x, y, z);
        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);

        if (boundingSpheres.size() == 0)
            return;
        for(BoundingSphere b : boundingSpheres)
            multiplyMV(b.center, 0, rotationMatrix, 0, b.initCenter, 0);
        for(int i = 0; i<boundingSpheres.size(); i++)
            multiplyMV(boundingSpheres.get(i).center, 0, translationMatrix, 0, boundingSpheres.get(i).center, 0);

    }

    public void Rotate( float angle, float xAxis, float yAxis, float zAxis )
    {
        rotateM(rotationMatrix, 0, angle, xAxis, yAxis, zAxis);
        // TODO prebaciti kontrole u ovu klasu
    }

    public void ScaleTo( float x, float y, float z )
    {
        setIdentityM(scaleMatrix, 0);
        scaleM( scaleMatrix, 0, x, y, z );
    }

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
//        List tempList = TextResourceReader.LoadObjFromResource( context, resourceId );
//        vertices = (float[]) tempList.get(0);
//        float[][] vertices2 = new float[vertices.length/3][3];
//        float[] currentVertex;
//        for (int i = 0; i<vertices.length; i+=3)
//        {
//            currentVertex = new float[3];
//            currentVertex[0] = vertices[i];
//            currentVertex[1] = vertices[i+1];
//            currentVertex[2] = vertices[i+2];
//            vertices2[i] = currentVertex;
//        }
//
//        boundingSpheres.add( BoundingSphere.Calculate( vertices2 ) );

        Vector objects = TextResourceReader.LoadCollisionObject( context, resourceId );
//        boundingSpheres = new ArrayList<>();

        for(int i = 0; i < objects.size(); i++)
        {
            boundingSpheres.add( BoundingSphere.Calculate1( (float[][]) objects.elementAt(i) ) );
        }

    }

    public void DrawBoundingSpheres()
    {
        GLCommon.boundingSphere.rotationMatrix = rotationMatrix.clone();

        for(int i = 0; i < boundingSpheres.size(); i++)
        {
            GLCommon.boundingSphere.TranslateTo(    boundingSpheres.get(i).center[0],
                                                    boundingSpheres.get(i).center[1],
                                                    boundingSpheres.get(i).center[2]);
            GLCommon.boundingSphere.ScaleTo(
                    boundingSpheres.get(i).radius,
                    boundingSpheres.get(i).radius,
                    boundingSpheres.get(i).radius);

            GLCommon.boundingSphere.DrawWireframe();
        }
    }

    // TODO - napraviti hijerarhiju omedujucih objekata

    public boolean Collision ( GLObject object )
    {
        for(BoundingSphere boundingSphere : boundingSpheres)
        {
            for(BoundingSphere objectBoundingSphere : object.boundingSpheres)
            {
                if ( boundingSphere.Collision( objectBoundingSphere ) )
                    return true;
            }
        }
        return false;
    }

}


class BoundingSphere
{
    float[] center;
    float radius;

    float[] initCenter = {0,0,0,1};
    float[] initRotationMatrix = new float[16];

    static float radiusCorrection = 0.8f;

    public BoundingSphere(){}

    public BoundingSphere( float[] pCenter, float pRadius )
    {
        center = pCenter.clone();
        initCenter = pCenter.clone();
        radius = pRadius;
        setIdentityM(initRotationMatrix, 0);
    }

    // ne radi
    public static BoundingSphere Calculate( float[][] pVertices )
    {
        float[] xmin = {Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY},
                xmax = {Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY},
                ymin = {Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY},
                ymax = {Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY},
                zmin = {Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY},
                zmax = {Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};

        for(float[] p : pVertices)
        {
            if(p[0] < xmin[0]) xmin = p;
            if(p[0] > xmax[0]) xmax = p;
            if(p[1] < ymin[1]) ymin = p;
            if(p[1] > ymax[1]) ymax = p;
            if(p[2] < zmin[2]) zmin = p;
            if(p[2] > zmax[2]) zmax = p;
        }

        float xSpan =  (float) Math.pow ( ( Matrix.length(xmax[0] - xmin[0],xmax[1] - xmin[1],xmax[2] - xmin[2])), 2f );
        float ySpan = (float) Math.pow ( ( Matrix.length(ymax[0] - ymin[0],ymax[1] - ymin[1],ymax[2] - ymin[2])), 2f );
        float zSpan = (float) Math.pow ( ( Matrix.length(zmax[0] - zmin[0],zmax[1] - zmin[1],zmax[2] - zmin[2])), 2f );
        float[] dia1 = xmin;
        float[] dia2 = xmax;
        float maxSpan = xSpan;

        if (ySpan > maxSpan)
        {
            maxSpan = ySpan;
            dia1 = ymin; dia2 = ymax;
        }
        if (zSpan > maxSpan)
        {
            dia1 = zmin; dia2 = zmax;
        }
        float[] center = {(dia1[0]+dia1[0])*0.5f,(dia1[1]+dia1[2])*0.5f,(dia1[2]+dia1[2])*0.5f};
        float sqRad = (float) Math.pow ( ( Matrix.length(dia2[0] - center[0],dia2[1] - center[1],dia2[2] - center[2])), 2f );
        float radius = (float) Math.sqrt(sqRad);

        for(float[] p : pVertices)
        {
            float d = (float) Math.pow ( ( Matrix.length(p[0] - center[0],p[1] - center[1],p[2] - center[2])), 2f );

            if(d > sqRad)
            {
                float r = (float) Math.sqrt(d);
                radius = (radius + r) * 0.5f;
                sqRad = radius * radius;
                float offset = r - radius;

                center[0] = (radius * center[0] + offset * p[0]) / r;
                center[1] = (radius * center[1] + offset * p[1]) / r;
                center[2] = (radius * center[2] + offset * p[2]) / r;
            }
        }

        float[] centerReturn = {center[0],center[1],center[2],1};

        return new BoundingSphere(centerReturn, (radius));
    }

    public static BoundingSphere Calculate1( float[][] pVertices )
    {
        float xSum = 0, ySum = 0, zSum = 0;
        float[] center = {0, 0, 0, 1};
        float radius = 0, temp;

        for ( float[] vertex : pVertices )
        {
            xSum += vertex[0];
            ySum += vertex[1];
            zSum += vertex[2];
        }

        center[0] = xSum / pVertices.length;
        center[1] = ySum / pVertices.length;
        center[2] = zSum / pVertices.length;

        for ( float[] vertex : pVertices )
        {
            temp = Matrix.length( center[0] - vertex[0], center[1] - vertex[1], center[2] - vertex[2] );
            if ( temp > radius )
                radius = temp;
        }

        return new BoundingSphere(center, radius * radiusCorrection);
    }

    public boolean Collision ( BoundingSphere boundingSphere )
    {
        return Matrix.length(center[0] - boundingSphere.center[0],
                center[1] - boundingSphere.center[1],
                center[2] - boundingSphere.center[2]) <= radius + boundingSphere.radius;
    }

}
