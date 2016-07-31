package com.example.luka.openglestest;

/**
 * Created by Luka on 17.11.2015..
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.example.luka.openglestest.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
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
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;

    private final FloatBuffer vertexData;
    private final FloatBuffer normalData;
    private final FloatBuffer uvData;

    private final Context context;

    private int program;

    private static final String U_COLOR = "u_Color";
    private int uColorLocation;
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;
    private static final String A_NORMAL = "a_Normal";
    private int aNormalLocation;
    private static final String A_TEXTURE = "a_Texture";
    private int aTextureLocation;

    int ocisteXLocation;

    private static final String MVP = "MVP";

    private int uMatrixLocation;

    float[] vrhoviObjekta;
    float[] normale;
    float[] uv;

    private final float[] modelMatrix = new float[16];
    int modelMatrixLocation;
    private final float[] viewMatrix = new float[16];
    int viewMatrixLocation;
    private final float[] projectionMatrix = new float[16];
    int projectionMatrixLocation;

    int lightPositionLocation, eyePositionLocation, varLocation;

    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    float[] trokut = {  0.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,};

    private PointF polozaj;

    float stariX = 0.0f, stariY = 0.0f;

    float ocisteX = 1.0f, ocisteY = 1.0f, ocisteZ = 1.0f;
    float gledisteX = 0.0f, gledisteY = 0.0f, gledisteZ = 0.0f;

    float[] lightPosition = {0.0f, 1.0f, 1.0f};
    int izvorSvjetlostiLocation;
    int textureID;
    int textureUniformLocation;

    public MojRenderer(Context context) {



        this.context = context;

        List lista;

        lista =  TextResourceReader.UcitajObjektIzDatoteke(context, R.raw.f16_1_uv);
        //lista =  TextResourceReader.UcitajObjektIzDatoteke(context, R.raw.kocka1);

        vrhoviObjekta =  (float[]) lista.get(0);
        normale = (float[]) lista.get(1);
        uv = (float[]) lista.get(2);

        //vrhoviObjekta = trokut;



        vertexData = ByteBuffer
                .allocateDirect(vrhoviObjekta.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vrhoviObjekta);


        normalData = ByteBuffer
                .allocateDirect(normale.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        normalData.put(normale);

        uvData = ByteBuffer
                .allocateDirect(uv.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        uvData.put(uv);


    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glEnable(GL_CULL_FACE);
        //glCullFace(GL_BACK);
        //glFrontFace(GL_CCW);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        textureID = LoadTexture("torus_texture", context);

        //String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);
        //String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.vertex_shader_phong);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.fragment_shader_phong);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        glUseProgram(program);

        uColorLocation = glGetUniformLocation(program, U_COLOR);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aNormalLocation = glGetAttribLocation(program, A_NORMAL);
        aTextureLocation = glGetAttribLocation(program, A_TEXTURE);


//        izvorSvjetlostiLocation = glGetUniformLocation(program, "izvorSvjetlosti");
//        glUniform3fv(izvorSvjetlostiLocation, 1, izvorSvjetlosti, 0);

        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        normalData.position(0);
        glVertexAttribPointer(aNormalLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, normalData);
        glEnableVertexAttribArray(aNormalLocation);

        uvData.position(0);
        glVertexAttribPointer(aTextureLocation, 2, GL_FLOAT, false, 0, uvData);
        glEnableVertexAttribArray(aTextureLocation);


        uMatrixLocation = glGetUniformLocation(program, "MVP");
        viewMatrixLocation = glGetUniformLocation(program, "V");
        modelMatrixLocation = glGetUniformLocation(program, "M");

        textureUniformLocation = glGetUniformLocation(program, "u_Texture");

        lightPositionLocation = glGetUniformLocation(program, "LightPosition_worldspace");
        eyePositionLocation = glGetUniformLocation(program, "ociste");

        polozaj = new PointF(0.0f, 0.0f);
        //glDisable(GL_DITHER);

        setLookAtM(viewMatrix, 0, ocisteX, ocisteY, ocisteZ, gledisteX, gledisteY, gledisteZ, 0.0f, 0.0f, 1.0f);

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

        perspectiveM(projectionMatrix, 0, 45, (float) width / (float) height, /*0f*/0.1f, 10f);


        setIdentityM(modelMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //glDisable(GL_CULL_FACE);

        //setLookAtM(viewMatrix, 0, ocisteX, ocisteY, ocisteZ, gledisteX, gledisteY, gledisteZ, 0.0f, 0.0f, 1.0f);

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
        glUniformMatrix4fv(uMatrixLocation, 1, false, modelViewProjectionMatrix, 0);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);
        glUniformMatrix4fv(modelMatrixLocation, 1, false, modelMatrix, 0);

        glUniform3f( eyePositionLocation, ocisteX, ocisteY, ocisteZ );
        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );

        //glEnable(GL_DEPTH_TEST);

        glActiveTexture(GL_TEXTURE0);
        // Set our "myTextureSampler" sampler to user Texture Unit 0
        //glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(textureUniformLocation, 0); // texture unit 0

        glDrawArrays(GL_TRIANGLES, 0, vrhoviObjekta.length / POSITION_COMPONENT_COUNT);

    }


    public void handleTouchPress(float normalizedX, float normalizedY)
    {
        stariX = normalizedX;
        stariY = normalizedY;
    }

    public void handleTouchDrag(float normalizedX, float normalizedY)
    {
        if (normalizedX > stariX) ocisteX += 10.0f * Math.abs(normalizedX - stariX);
        else if (normalizedX < stariX) ocisteX -= 10.0f * Math.abs(normalizedX - stariX);

        if (normalizedY > stariY) ocisteZ += 10.0f * Math.abs(normalizedY - stariY);
        else if (normalizedY < stariY) ocisteZ -= 10.0f * Math.abs(normalizedY - stariY);

        setLookAtM(viewMatrix, 0, ocisteX, ocisteY, ocisteZ, gledisteX, gledisteY, gledisteZ, 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);

        //glUniform1f(ocisteXLocation, ocisteX);

        stariX = normalizedX;
        stariY = normalizedY;


    }

    public int LoadTexture(String resourceName, Context context)
    {
        final int[] textureHandle = new int[1];

        glGenTextures(1, textureHandle, 0);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling

        // Read in the resource
        int resID = context.getResources().getIdentifier(resourceName, "raw", context.getPackageName());
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resID, options);

        // Bind to the texture in OpenGL

        glBindTexture(GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Load the bitmap into the bound texture.

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();

        return textureHandle[0];


    }
}


