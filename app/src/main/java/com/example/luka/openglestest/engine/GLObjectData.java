package com.example.luka.openglestest.engine;

import android.content.Context;

import com.example.luka.openglestest.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;


//  Sluzi za ucitavanje podataka o tipu objekta (vrhova, normala, UV koordinata i teksture),
//  da se oni ne bi morali ucitavati kod svakog instanciranja razreda GLObject
//
//  GLObjekt se moze instancirati tako da mu se kao parametar da objekt ovog razreda


public class GLObjectData
{
    float[] vertices, normals, UVs;

    FloatBuffer interleavedBuffer;
    int dataLength;
    public int bufferIndex;

    static final int BYTES_PER_FLOAT = 4;
    static final int VERTEX_DATA_SIZE = 3;
    static final int NORMAL_DATA_SIZE = 3;
    static final int UV_DATA_SIZE = 2;
    int stride = (VERTEX_DATA_SIZE + NORMAL_DATA_SIZE + UV_DATA_SIZE) * BYTES_PER_FLOAT;

    public int textureID;

    public GLObjectData() {}

    public GLObjectData(Context context, int resourceId, int textureIDp )
    {
        List tempList = TextResourceReader.LoadObjFromResource( context, resourceId );
        vertices =  (float[]) tempList.get(0);
        normals = (float[]) tempList.get(1);
        UVs = (float[]) tempList.get(2);

        textureID = textureIDp;

        //-------------------------------------------------------------------------
        // jedan buffer za objekte istog tipa   59.2 avg fps
        // bufferi za svaki objekt              58.8 avg fps
        //                                      ( stednja baterije ukljucena )

        dataLength = vertices.length + normals.length + UVs.length;

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
}
