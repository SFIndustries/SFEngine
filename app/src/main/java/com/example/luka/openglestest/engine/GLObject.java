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
    public float[] velocity = {0, 0, 0, 1}, velocityTp1 = {0, 0, 0, 1}, tangentialVelocityTp1 = {0, 0, 0, 1};

    public float velocityScalar = 0;
    public float accelerationScalar = 0.005f;
    public float[]  tangentialVelocity = {0, 0, 0, 1}, tangentialVelocityTemp = {0, 0, 0, 1},
                    translationalVelocity = {0, 0, 0, 1};
    public float tangentialVelocityScalar;
    public float collisionRadiusScalar;
    public float[] collisionRadius;

    public float mass = 1;
    public float momentOfIntertia = .5f;

    public float[] centerOfMass = new float[4], centerOfMassInit = new float[4];

    public float[] colour = {1, 1, 1, 1};
    public float alpha = 1.0f;

    public List rotationAxes = new ArrayList();
    float[] rotationAxis = {0,0,0,1};

    public List angularVelocities = new ArrayList();
    public float angularVelocity = 0;

    List<BoundingSphere> boundingSpheres = new ArrayList<>();

    float[] centerToCenter = {0, 0, 0, 1}, collisionPoint = new float[4];
    float collisionDepth, centerToCenterDistance;

    public float[] gunLeftWing = {.4f, .3f, -.1f, 1}, gunLeftWingInit = {.4f, .3f, -.1f, 1};
    public float[] gunRightWing = {-.4f, .3f, -.1f, 1}, gunRightWingInit = {-.4f, .3f, -.1f, 1};

    public float projectileVelocityScalar = .3f; // 2f
    public int fireRate = 10;
    public long fireRateCounter = 0, lastFired = 0;

    int i, j;

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

        boundingSpheres = new ArrayList<>(object.boundingSpheres);

        centerOfMassInit = object.centerOfMassInit;
        centerOfMass = centerOfMassInit;

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
    {
        if ( renderMode != GLCommon.renderMode )
        {
            renderModePrevious = GLCommon.renderMode;
            GLCommon.SetRenderMode( renderMode );
        }

        //---------------------------------------------------------------------
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

    public void DrawBloom ( int layers, float scalingStep, float alphaStep )
    {
        float alphaTemp = 1;
        float scalingTemp = 1;
        float alphaOld = alpha;
        float[] scaleMatrixOld = scaleMatrix.clone();

        for ( int i = 0; i < layers + 1; i++ )
        {
            ScaleTo( scalingTemp, scalingTemp, scalingTemp );
            alpha = alphaTemp;
            Draw();
            scalingTemp += scalingStep;
            alphaTemp -= alphaStep;
        }

        alpha = alphaOld;
        scaleMatrix = scaleMatrixOld.clone();

    }

    public void Translate( float x, float y, float z )
    {
        translateM(translationMatrix, 0, x, y, z);
        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);
        multiplyMV(centerOfMass, 0, translationMatrix, 0, centerOfMassInit, 0);

        if (boundingSpheres.size() == 0)
            return;
        for(BoundingSphere b : boundingSpheres)
            multiplyMV(b.center, 0, rotationMatrix, 0, b.initCenter, 0);
        for(int i = 0; i<boundingSpheres.size(); i++)
            multiplyMV(boundingSpheres.get(i).center, 0, translationMatrix, 0, boundingSpheres.get(i).center, 0);

        multiplyMV(gunLeftWing, 0, rotationMatrix, 0, gunLeftWingInit, 0);
        multiplyMV(gunLeftWing, 0, translationMatrix, 0, gunLeftWing, 0);
        multiplyMV(gunRightWing, 0, rotationMatrix, 0, gunRightWingInit, 0);
        multiplyMV(gunRightWing, 0, translationMatrix, 0, gunRightWing, 0);

    }

    public void TranslateTo( float x, float y, float z )
    {
        setIdentityM(translationMatrix, 0);

        Translate(x, y, z);

//        translateM(translationMatrix, 0, x, y, z);
//        multiplyMV(position, 0, translationMatrix, 0, initPosition, 0);
//
//        if (boundingSpheres.size() == 0)
//            return;
//        for(BoundingSphere b : boundingSpheres)
//            multiplyMV(b.center, 0, rotationMatrix, 0, b.initCenter, 0);
//        for(int i = 0; i<boundingSpheres.size(); i++)
//            multiplyMV(boundingSpheres.get(i).center, 0, translationMatrix, 0, boundingSpheres.get(i).center, 0);

    }

    public void Rotate( float angle, float pxAxis, float pyAxis, float pzAxis )
    {
        rotateM(rotationMatrix, 0, angle, pxAxis, pyAxis, pzAxis);
        // TODO prebaciti kontrole u ovu klasu

        multiplyMV(xAxis, 0, rotationMatrix, 0, xAxisInit, 0);
        multiplyMV(yAxis, 0, rotationMatrix, 0, yAxisInit, 0);
        multiplyMV(zAxis, 0, rotationMatrix, 0, zAxisInit, 0);

    }

    public void Scale( float x, float y, float z )
    {
        scaleM( scaleMatrix, 0, x, y, z );
    }

    public void ScaleTo( float x, float y, float z )
    {
        setIdentityM( scaleMatrix, 0 );
        Scale( x, y, z );
        //scaleM( scaleMatrix, 0, x, y, z );
    }

    public void SetInitOrientation( float[] vector )
    {
        initOrientation = vector.clone();
        orientation = initOrientation.clone();
    }

    public void UpdatePosition() // posebna dretva ? ili 1 dretva za update svih objekata?
    {
        Translate( velocity[0] /** orientation[0]*/, velocity[1] /** orientation[1]*/, velocity[2] /** orientation[2]*/ );
    }

    // TODO
    public void UpdateRotation()
    {

//        if ( angularVelocity < 0.001f )
//            return;
//        Rotate(angularVelocity, rotationAxis[0], rotationAxis[1], rotationAxis[2]);

        for ( i=0; i < rotationAxes.size(); i++ )
        {
            if ( (float) angularVelocities.get(i) < 0.001f )
                continue;
            Rotate(   (float)angularVelocities.get(i),
                    ((float[])rotationAxes.get(i))[0],((float[])rotationAxes.get(i))[1],((float[])rotationAxes.get(i))[2]);
        }

    }

    public void InitCollisionObject( Context context, int resourceId )
    {
        Vector objects = TextResourceReader.LoadCollisionObject( context, resourceId );

        // sfera koja obuhvaca cijeli objekt
        boundingSpheres.add( BoundingSphere.Calculate1( (float[][]) objects.elementAt( objects.size() - 1 ) ) );
        centerOfMassInit = boundingSpheres.get(0).center.clone();
        centerOfMass = centerOfMassInit.clone();
        boundingSpheres.remove(0);

        for(int i = 0; i < objects.size()-1; i++)
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
        // TODO - dodati sferu koja opisuje cijeli objekt pa prvo samo nju provjeriti
        // TODO - ali njoj ne korigirati radijus

        //if ( ! boundingSpheres.get(0).Collision( object.boundingSpheres.get(0) ) )
        //    return false;

        // TODO - ovo racuna za sve sfere pa i omedujucu !!!
        for( BoundingSphere boundingSphere : boundingSpheres )
        {
            for( BoundingSphere objectBoundingSphere : object.boundingSpheres )
            {
                if ( boundingSphere.Collision( objectBoundingSphere ) )
                {

                    // savrseno elasticni sudar

                    // ------------------------------------------------------------------------------

                    collisionPoint = boundingSphere.CollisionPoint( objectBoundingSphere );

                    collisionRadius = new float[4];
                    collisionRadius[0] =  collisionPoint[0] - centerOfMass[0];
                    collisionRadius[1] =  collisionPoint[1] - centerOfMass[1];
                    collisionRadius[2] =  collisionPoint[2] - centerOfMass[2];
                    collisionRadius[3] =  1;

                    collisionRadiusScalar = Matrix.length(      collisionRadius[0],
                                                                collisionRadius[1],
                                                                collisionRadius[2] );

                    object.collisionRadius = new float[4];
                    object.collisionRadius[0] =  collisionPoint[0] - object.centerOfMass[0];
                    object.collisionRadius[1] =  collisionPoint[1] - object.centerOfMass[1];
                    object.collisionRadius[2] =  collisionPoint[2] - object.centerOfMass[2];
                    object.collisionRadius[3] =  1;

                    object.collisionRadiusScalar = Matrix.length(      object.collisionRadius[0],
                            object.collisionRadius[1],
                            object.collisionRadius[2] );

                    tangentialVelocity = new float[4];
                    object.tangentialVelocity = new float[4];

                    // obodna brzina od dotadasnje rotacija

                    // TODO - umjesto xAxis, y i z napraviti Axes[3]

                    // TODO - rotira se samo naprijed-nazad, nesto se ne rotira?

                    // ovaj objekt
                    for (i = 0; i < rotationAxes.size(); i++)
                    {
                        if ( (float) angularVelocities.get(i) > 0.001f)
                        {
                            angularVelocities.set(i, (float) Math.toRadians( (float) angularVelocities.get(i) ));
                            tangentialVelocityTemp = new float[4];
                            CrossProduct(tangentialVelocityTemp, (float[]) rotationAxes.get(i), collisionRadius);
                            tangentialVelocityScalar = Matrix.length(tangentialVelocityTemp[0], tangentialVelocityTemp[1], tangentialVelocityTemp[2]);
                            if (tangentialVelocityScalar > 0.001)
                            {
                                for (j = 0; j < tangentialVelocityTemp.length; j++)
                                    tangentialVelocityTemp[j] /= tangentialVelocityScalar;
                                tangentialVelocityScalar = (float) angularVelocities.get(i) * collisionRadiusScalar;
                                for (j = 0; j < tangentialVelocityTemp.length; j++)
                                    tangentialVelocityTemp[j] *= tangentialVelocityScalar;
//                            if (angularVelocity < 0) {
//                                for (i = 0; i < tangentialVelocityTemp.length; i++)
//                                    tangentialVelocityTemp[i] = -tangentialVelocityTemp[i];
//                            }
                            }
                            for (j = 0; j < tangentialVelocity.length; j++)
                            {
                                tangentialVelocity[j] += tangentialVelocityTemp[j];
                                //tangentialVelocity[j] += velocity[j]; // ovo tek nakon projekcije
                            }
                        }
                    }
                    translationalVelocity = new float[4];
                    VectorProjection(translationalVelocity, velocity, collisionRadius);
                    for (i = 0; i < tangentialVelocity.length; i++) {
                        tangentialVelocity[i] += (velocity[i] - translationalVelocity[i]);
                    }


                    // drugi objekt
                    for (i = 0; i < object.rotationAxes.size(); i++)
                    {
                        if ( (float) object.angularVelocities.get(i) > 0.001f) {
                            object.angularVelocities.set(i, (float) Math.toRadians((float)object.angularVelocities.get(i)));
                            tangentialVelocityTemp = new float[4];
                            CrossProduct(tangentialVelocityTemp, (float[]) object.rotationAxes.get(i), object.collisionRadius);
                            //CrossProduct( tangentialVelocityTemp, object.collisionRadius, object.rotationAxis );
                            object.tangentialVelocityScalar = Matrix.length(tangentialVelocityTemp[0], tangentialVelocityTemp[1], tangentialVelocityTemp[2]);
                            for (j = 0; j < tangentialVelocityTemp.length; j++)
                                tangentialVelocityTemp[j] /= object.tangentialVelocityScalar;
                            object.tangentialVelocityScalar = (float) object.angularVelocities.get(i) * object.collisionRadiusScalar;
                            if (object.tangentialVelocityScalar > 0.001) {

                                for (j = 0; j < tangentialVelocityTemp.length; j++)
                                    tangentialVelocityTemp[j] *= object.tangentialVelocityScalar;
//                            if (object.angularVelocity < 0) {
//                                for (i = 0; i < tangentialVelocityTemp.length; i++)
//                                    tangentialVelocityTemp[i] = -tangentialVelocityTemp[i];
//                            }
                            }

                            for (j = 0; j < tangentialVelocity.length; j++) {
                                object.tangentialVelocity[j] += tangentialVelocityTemp[j];
                                //object.tangentialVelocity[j] += object.velocity[j];
                            }
                        }
                    }
                    object.translationalVelocity = new float[4];
                    VectorProjection( object.translationalVelocity, object.velocity, object.collisionRadius );
                    for(i = 0; i<object.tangentialVelocity.length; i++)
                    {
                        object.tangentialVelocity[i] += (object.velocity[i] - object.translationalVelocity[i]);
                    }


                    for(i = 0; i < velocity.length; i++)
                    {
                        tangentialVelocityTp1[i] =        (tangentialVelocity[i] * (momentOfIntertia - object.momentOfIntertia) + 2 * object.momentOfIntertia * object.tangentialVelocity[i])
                                /   (momentOfIntertia + object.momentOfIntertia); // treba ici moment tromosti, na masa

                        object.tangentialVelocityTp1[i] =      (object.tangentialVelocity[i] * (object.momentOfIntertia - momentOfIntertia) + 2 * momentOfIntertia * tangentialVelocity[i])
                                /   (momentOfIntertia + object.momentOfIntertia); // treba ici moment tromosti, na masa
                    }

                    // ovaj objekt
                    float[] newRotationAxis = {0,0,0,1};

                    //CrossProduct( newRotationAxis, collisionRadius, tangentialVelocityTp1 );
                    CrossProduct( newRotationAxis, tangentialVelocityTp1, collisionRadius );
                    tangentialVelocityScalar = Matrix.length(tangentialVelocityTp1[0],tangentialVelocityTp1[1],tangentialVelocityTp1[2]);

                    //rotationAxis = newRotationAxis.clone();
                    if ( rotationAxes.size() == 3 )
                        rotationAxes.remove(0);
                    rotationAxes.add( newRotationAxis.clone() );
                    //angularVelocity = (float) Math.toDegrees(tangentialVelocityScalar / collisionRadiusScalar);
                    if ( angularVelocities.size() == 3 )
                        angularVelocities.remove(0);
                    angularVelocities.add( (float) Math.toDegrees(tangentialVelocityScalar / collisionRadiusScalar) );


                    // drugi objekt
                    newRotationAxis = new float[4];

                    //CrossProduct( newRotationAxis, object.collisionRadius, object.tangentialVelocityTp1 );
                    CrossProduct( newRotationAxis, object.tangentialVelocityTp1, object.collisionRadius );
                    tangentialVelocityScalar = Matrix.length(object.tangentialVelocityTp1[0],object.tangentialVelocityTp1[1],object.tangentialVelocityTp1[2]);

                    //object.rotationAxis = newRotationAxis.clone();
                    if ( object.rotationAxes.size() == 3 )
                        object.rotationAxes.remove(0);
                    object.rotationAxes.add( newRotationAxis.clone() );
                    //object.angularVelocity = (float) Math.toDegrees(tangentialVelocityScalar / object.collisionRadiusScalar);
                    if ( object.angularVelocities.size() == 3 )
                        object.angularVelocities.remove(0);
                    object.angularVelocities.add( (float) Math.toDegrees(tangentialVelocityScalar / object.collisionRadiusScalar) );

                    //-------------------------------------------------------------------------------------------------------
                    collisionDepth =        boundingSphere.radius + objectBoundingSphere.radius
                                        -   Matrix.length(  boundingSphere.center[0] - objectBoundingSphere.center[0],
                                                            boundingSphere.center[1] - objectBoundingSphere.center[1],
                                                            boundingSphere.center[2] - objectBoundingSphere.center[2] );

                    for(i = 0; i < centerToCenter.length; i++)
                        centerToCenter[i] = objectBoundingSphere.center[i] - boundingSphere.center[i];

                    centerToCenterDistance = Matrix.length( centerToCenter[0], centerToCenter[1], centerToCenter[2] );

                    for(i = 0; i < centerToCenter.length; i++)
                    {
                        centerToCenter[i] /= centerToCenterDistance;
                        centerToCenter[i] *= collisionDepth;
                    }

                    object.Translate( centerToCenter[0],centerToCenter[1],centerToCenter[2] );

                    // TODO - dodati rotaciju

                    // TODO - rastaviti brzine na onu koja ce ici na translacijsku i onu koja ce ici na rotacijsku komponentu


                    for(i = 0; i < velocity.length; i++)
                    {
//                        velocityTp1[i] =        (velocity[i] * (mass - object.mass) + 2 * object.mass * object.velocity[i])
//                                            /   (mass + object.mass);
//
//                        objectVelocityTp1[i] =      (object.velocity[i] * (object.mass - mass) + 2 * mass * velocity[i])
//                                                /   (mass + object.mass);

                        velocityTp1[i] =        (translationalVelocity[i] * (mass - object.mass) + 2 * object.mass * object.translationalVelocity[i])
                                /   (mass + object.mass);

                        object.velocityTp1[i] =      (object.translationalVelocity[i] * (object.mass - mass) + 2 * mass * translationalVelocity[i])
                                /   (mass + object.mass);
                    }

                    // ponovo izracunaj skalar brzine
                    velocity = velocityTp1.clone();
                    object.velocity = object.velocityTp1.clone();

                    velocityScalar = Matrix.length(velocity[0],velocity[1],velocity[2]);
                    object.velocityScalar = Matrix.length(object.velocity[0],object.velocity[1],object.velocity[2]);

                    return true;
                }
            }
        }
        return false;
    }

    public void Fire()
    {
        // TODO - ako je izvan raspona
        // TODO - ako je daleko pretvoriti u neaktivan

        if ( Math.abs( (GLCommon.frameCounter - lastFired) ) >= fireRate )
        {
            lastFired = GLCommon.frameCounter;
            //fireRateCounter = 0;

            //TODO - napraviti guns[i]

            GLCommon.projectileIndex = (GLCommon.projectileIndex + 1) % GLCommon.projectilesCount;
            GLCommon.projectiles[GLCommon.projectileIndex].rotationMatrix = rotationMatrix.clone();
            GLCommon.projectiles[GLCommon.projectileIndex].TranslateTo(gunLeftWing[0], gunLeftWing[1], gunLeftWing[2]);
            //TODO - dodati POSITION_COUNT u sve petlje
            for ( i = 0; i < velocity.length; i++)
                GLCommon.projectiles[GLCommon.projectileIndex].velocity[i] = velocity[i] + orientation[i] * projectileVelocityScalar;
            GLCommon.projectilesActive[GLCommon.projectileIndex] = true;

            GLCommon.projectileIndex = (GLCommon.projectileIndex + 1) % GLCommon.projectilesCount;
            //tempVector = plane.initOrientation.clone();
            //multiplyMV(projectiles[projectileIndex].orientation, 0, plane.rotationMatrix, 0, tempVector, 0);
            GLCommon.projectiles[GLCommon.projectileIndex].rotationMatrix = rotationMatrix.clone();
            GLCommon.projectiles[GLCommon.projectileIndex].TranslateTo(gunRightWing[0], gunRightWing[1], gunRightWing[2]);
            for ( i = 0; i < velocity.length; i++)
                GLCommon.projectiles[GLCommon.projectileIndex].velocity[i] = velocity[i] + orientation[i] * projectileVelocityScalar;
            GLCommon.projectilesActive[GLCommon.projectileIndex] = true;
        }
    }

    public void ScampFire ( GLObject object )
    {
        //mozda u racunu malo povecati brzinu mete da gada malo ispred?
        if (object.position[0] > position[0])
        {
            ScampFire1(object);
            return;
        }

        double a = ( object.position[0] - position[0] );
        double b = ( object.position[1] - position[1] );
        double c = ( object.position[2] - position[2] );

        double d = projectileVelocityScalar;

        double g = object.velocity[0];
        double h = object.velocity[1];
        double i = object.velocity[2];

        double x; //vpx
        double y; //vpy
        double z; //vpz

//        x =        (float) (       Math.pow(b,2)*g + Math.pow(c,2)*g - a*c*i
//                        -   Math.sqrt   (
//                                                Math.pow(a,2)
//                                                *
//                                                (
//                                                            Math.pow(c,2)
//                                                    *   (   Math.pow(d,2) - Math.pow(g,2)) + 2*a*c*g*i
//                                                    +   Math.pow(a,2)*(Math.pow(d,2) - Math.pow(i,2))
//                                                    +   Math.pow(b,2)*(Math.pow(d,2) - Math.pow(g,2) - Math.pow(i,2))
//                                                )
//                                        )
//                    )
//                    /
//                    ((float)( Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2) ));
//
//
//        y =  (float)(      -( b *   (
//                            Math.pow(a,2)*g + a*c*i
//                            +   Math.sqrt(
//                                                Math.pow(a,2)
//                                            *   (
//                                                    Math.pow(c,2)*(Math.pow(d,2) - Math.pow(g,2))
//                                                +   2*a*c*g*i + Math.pow(a,2)*(Math.pow(d,2) - Math.pow(i,2))
//                                                +   Math.pow(b,2)*(Math.pow(d,2) - Math.pow(g,2) - Math.pow(i,2))
//                                                )
//                                    )
//                            )
//                    )
//                    /
//                    (a*(Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2))));
//
//        z =  (float)((-Math.pow(a,2)*c*g + Math.pow(a,3)*i + a*Math.pow(b,2)*i -
//            c*Math.sqrt(Math.pow(a,2)*(Math.pow(c,2)*(Math.pow(d,2) - Math.pow(g,2)) + 2*a*c*g*i + Math.pow(a,2)*(Math.pow(d,2) - Math.pow(i,2)) +
//            Math.pow(b,2)*(Math.pow(d,2) - Math.pow(g,2) - Math.pow(i,2)))))
//            /
//            (a*(Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2))));

        x =         -(       -Math.pow(b,2)*g - Math.pow(c,2)*g + a*b*h + a*c*i
                +   Math.sqrt   (
                -Math.pow(a,2)
                        *
                        (
                                Math.pow(c,2)
                                        *   (   -Math.pow(d,2) + Math.pow(g,2) + Math.pow(h,2)) - 2*b*c*h*i
                                        - 2*a*g * (b*h + c*i) + Math.pow(b,2)*( -Math.pow(d,2) + Math.pow(g,2) + Math.pow(i,2) )
                                        + Math.pow(a,2)*(-Math.pow(d,2) +Math.pow(h,2) +Math.pow(i,2))
                        )
        )
        )
                /
                (( Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2) ));


        y =  (1/(a*(Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2))))*(-Math.pow(a,2)*b*g + Math.pow(a,3)*h + a*Math.pow(c,2)*h - a*b*c*i -
            b*Math.sqrt(-Math.pow(a,2)*(Math.pow(c,2)*(-Math.pow(d,2) + Math.pow(g,2) + Math.pow(h,2)) - 2*b*c*h*i -
            2*a*g*(b*h + c*i) + Math.pow(b,2)*(-Math.pow(d,2) + Math.pow(g,2) + Math.pow(i,2)) +
                    Math.pow(a,2)*(-Math.pow(d,2) + Math.pow(h,2) + Math.pow(i,2)))));

        z =  (1/(a* (Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2))))*(-Math.pow(a,2) *c *g - a *b* c* h + Math.pow(a,3) *i + a *Math.pow(b,2)* i -
            c *Math.sqrt(-Math.pow(a,2)* (Math.pow(c,2)* (-Math.pow(d,2) + Math.pow(g,2) + Math.pow(h,2)) - 2* b *c *h* i -
            2* a *g* (b* h + c *i) + Math.pow(b,2)* (-Math.pow(d,2) + Math.pow(g,2) + Math.pow(i,2)) +
                    Math.pow(a,2)* (-Math.pow(d,2) + Math.pow(h,2) + Math.pow(i,2)))));

        GLCommon.projectileIndex = (GLCommon.projectileIndex + 1) % GLCommon.projectilesCount;
        //GLCommon.projectiles[GLCommon.projectileIndex].rotationMatrix = rotationMatrix.clone();
        GLCommon.projectiles[GLCommon.projectileIndex].TranslateTo(position[0],position[1],position[2]);
        //TODO - dodati POSITION_COUNT u sve petlje

        // ne radi kad se meta krece
        // TODO - provjeriti druga rjesenja

//        if ( object.position[0] > position[0] )
//        {
//            x = -x;
//            y = -y;
//            z = -z;
//        }


        GLCommon.projectiles[GLCommon.projectileIndex].velocity[0] = (float) x;
        GLCommon.projectiles[GLCommon.projectileIndex].velocity[1] = (float) y;
        GLCommon.projectiles[GLCommon.projectileIndex].velocity[2] = (float) z;

        GLCommon.projectilesActive[GLCommon.projectileIndex] = true;


    }

    public void ScampFire1 ( GLObject object )
    {
        double a = ( object.position[0] - position[0] );
        double b = ( object.position[1] - position[1] );
        double c = ( object.position[2] - position[2] );

        double d = projectileVelocityScalar;

        double g = object.velocity[0];
        double h = object.velocity[1];
        double i = object.velocity[2];

        double x; //vpx
        double y; //vpy
        double z; //vpz

        x =         (       Math.pow(b,2)*g + Math.pow(c,2)*g - a*b*h - a*c*i
                +   Math.sqrt   (
                -Math.pow(a,2)
                        *
                        (
                                Math.pow(c,2)
                                        *   (   -Math.pow(d,2) + Math.pow(g,2) + Math.pow(h,2)) - 2*b*c*h*i
                                        - 2*a*g * (b*h + c*i) + Math.pow(b,2)*( -Math.pow(d,2) + Math.pow(g,2) + Math.pow(i,2) )
                                        + Math.pow(a,2)*(-Math.pow(d,2) +Math.pow(h,2) +Math.pow(i,2))
                        )
        )
        )
                /
                (( Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2) ));


        y =  (1/(a*(Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2))))*(-Math.pow(a,2)*b*g + Math.pow(a,3)*h + a*Math.pow(c,2)*h - a*b*c*i +
                b*Math.sqrt(-Math.pow(a,2)*(Math.pow(c,2)*(-Math.pow(d,2) + Math.pow(g,2) + Math.pow(h,2)) - 2*b*c*h*i -
                        2*a*g*(b*h + c*i) + Math.pow(b,2)*(-Math.pow(d,2) + Math.pow(g,2) + Math.pow(i,2)) +
                        Math.pow(a,2)*(-Math.pow(d,2) + Math.pow(h,2) + Math.pow(i,2)))));

        z =  (1/(a* (Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2))))*(-Math.pow(a,2) *c *g - a *b* c* h + Math.pow(a,3) *i + a *Math.pow(b,2)* i +
                c *Math.sqrt(-Math.pow(a,2)* (Math.pow(c,2)* (-Math.pow(d,2) + Math.pow(g,2) + Math.pow(h,2)) - 2* b *c *h* i -
                        2* a *g* (b* h + c *i) + Math.pow(b,2)* (-Math.pow(d,2) + Math.pow(g,2) + Math.pow(i,2)) +
                        Math.pow(a,2)* (-Math.pow(d,2) + Math.pow(h,2) + Math.pow(i,2)))));

        GLCommon.projectileIndex = (GLCommon.projectileIndex + 1) % GLCommon.projectilesCount;
        //GLCommon.projectiles[GLCommon.projectileIndex].rotationMatrix = rotationMatrix.clone();
        GLCommon.projectiles[GLCommon.projectileIndex].TranslateTo(position[0],position[1],position[2]);
        //TODO - dodati POSITION_COUNT u sve petlje

        // ne radi kad se meta krece
        // TODO - provjeriti druga rjesenja

//        if ( object.position[0] > position[0] )
//        {
//            x = -x;
//            y = -y;
//            z = -z;
//        }


        GLCommon.projectiles[GLCommon.projectileIndex].velocity[0] = (float) x;
        GLCommon.projectiles[GLCommon.projectileIndex].velocity[1] = (float) y;
        GLCommon.projectiles[GLCommon.projectileIndex].velocity[2] = (float) z;

        GLCommon.projectilesActive[GLCommon.projectileIndex] = true;


    }

    void CrossProduct ( float[] result, float[] u, float[] v )
    {
        result[0] = u[1]*v[2] - u[2]*v[1];
        result[1] = u[2]*v[0] - u[0]*v[2];
        result[2] = u[0]*v[1] - u[1]*v[0];
    }

    void VectorProjection ( float[] result, float[] a, float[] b  )
    {
        int i;
        float bLength, dot = 0;
        float[] normalizedB = new float[4];

        bLength = (float) Matrix.length( b[0], b[1], b[2] );
        for (i = 0; i < 3; i++)
            b[i] /= bLength;

        for (i = 0; i < 3; i++)
            dot += a[i] * b[i];

        for (i = 0; i < 3; i++)
            result[i] = b[i] * dot;
    }

}


class BoundingSphere
{
    float[] center;
    float radius;

    float[] initCenter = {0,0,0,1};

    static float radiusCorrection = 0.8f;

    public BoundingSphere(){}

    public BoundingSphere( float[] pCenter, float pRadius )
    {
        center = pCenter.clone();
        initCenter = pCenter.clone();
        radius = pRadius;
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
        return  (Matrix.length(center[0] - boundingSphere.center[0],
            center[1] - boundingSphere.center[1],
            center[2] - boundingSphere.center[2]) <= radius + boundingSphere.radius);
    }

    public float[] CollisionPoint ( BoundingSphere boundingSphere )
    {
        float[] collisionPoint = new float[]{   (center[0] + boundingSphere.center[0]) / 2f,
                                                (center[0] + boundingSphere.center[1]) / 2f,
                                                (center[0] + boundingSphere.center[2]) / 2f,
                                                1
                                            };
        return collisionPoint;
    }

}
