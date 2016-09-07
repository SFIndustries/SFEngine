package com.example.luka.openglestest.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import com.example.luka.openglestest.ShaderHelper;
import com.example.luka.openglestest.util.TextResourceReader;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;

/**
 * Created by Luka on 1.8.2016..
 */
public class GLCommon
{
    public static int program, programPhongTexture, programTexture, programColour;
    public static List<Integer> programList = new ArrayList<>();

    public static float[] modelMatrix = new float[16];
    public static int modelMatrixLocation;
    public static final float[] viewMatrix = new float[16];
    public static int viewMatrixLocation;
    public static final float[] projectionMatrix = new float[16];
    public static int projectionMatrixLocation;
    public static final float[] viewProjectionMatrix = new float[16];

    public static final float[] modelViewProjectionMatrix = new float[16];
    public static int modelViewProjectionMatrixLocation;

    public static int aPositionLocation, aNormalLocation, aTextureLocation;
    public static int textureUniformLocation;

    public static float[] lightPosition = {0.0f, 1.0f, 1.0f};
    public static int lightPositionLocation;

    public static float[]   eyePosition = {1.0f, 1.0f, 1.0f, 1.0f}, eyePositionTm1 = new float[4],
                            eyePositionSmooth = new float[4], eyePositionSmoothTm1 = new float[4];
    public static int eyePositionLocation;

    public static int alphaLocation;
    public static int colourLocation;

    public static float[] center = {0.0f, 0.0f, 0.0f}, centerTm1 = new float[4];

    public static float[] up = {0.0f, 0.0f, 1.0f, 1.0f}, upTm1 = new float[4];;

    public static float fov = 45;
    public static float nearZ = 1f;
    public static float farZ = 1000;

    public static List<Integer> textureIDList = new ArrayList<>();

    public static GLCamera camera;

    public static final int COLOUR = 0, TEXTURE = 1, TEXTURE_BLINN_PHONG = 2, TEXTURE_PHONG = 3;
    public static int renderMode = 1;

    public static GLObject boundingSphere;

    public static int InitProgram(Context context, int vertexShaderId, int fragmentShaderId)
    {
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, vertexShaderId);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, fragmentShaderId);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        int program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        programList.add(program);

        return program;
    }

    public static void UseProgram( int programP )
    {
        program = programP;

        glUseProgram( program );

        //TODO - ovisno o shaderu inicijalizirati lokacije samo za neke atribute/uniforme

        aPositionLocation = glGetAttribLocation(program, "a_Position");
        aNormalLocation = glGetAttribLocation(program, "a_Normal");
        aTextureLocation = glGetAttribLocation(program, "a_Texture");

        viewMatrixLocation = glGetUniformLocation(program, "V");
        modelMatrixLocation = glGetUniformLocation(program, "M");
        modelViewProjectionMatrixLocation = glGetUniformLocation(program, "MVP");

        textureUniformLocation = glGetUniformLocation(program, "u_Texture");

        lightPositionLocation = glGetUniformLocation(program, "LightPosition_worldspace");
        eyePositionLocation = glGetUniformLocation(program, "ociste");

        alphaLocation = glGetUniformLocation(program, "alpha");
        colourLocation = glGetUniformLocation(program, "colour");

        glEnableVertexAttribArray(GLCommon.aPositionLocation);
        glEnableVertexAttribArray(GLCommon.aNormalLocation);
        glEnableVertexAttribArray(GLCommon.aTextureLocation);
    }


    public static int LoadTexture(int resourceName, Context context)
    {
        final int[] textureHandle = new int[1];

        glGenTextures(1, textureHandle, 0);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        //options.inSampleSize = 1;

        // Read in the resource
        //int resID = context.getResources().getIdentifier(resourceName, "raw", context.getPackageName());
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resourceName, options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Bind to the texture in OpenGL
        glBindTexture(GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Load the bitmap into the bound texture.

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();

        textureIDList.add( textureHandle[0] );

        return textureHandle[0];

    }

    public static void SetEyePosition (float x, float y, float z) //ociste
    {
        eyePosition[0] = x;
        eyePosition[1] = y;
        eyePosition[2] = z;
    }

    public static void SetCenter (float x, float y, float z) //glediste
    {
        center[0] = x;
        center[1] = y;
        center[2] = z;
    }

    public static void SetUniformsShader()
    {
        glUniform3f( eyePositionLocation, eyePosition[0], eyePosition[1], eyePosition[2] );
        glUniform3f( lightPositionLocation, lightPosition[0], lightPosition[1], lightPosition[2] );
    }

    public static void SetRenderMode( int renderModep )
    {
        renderMode = renderModep;
        switch ( renderMode )
        {
            case TEXTURE:
                UseProgram( programTexture );
                break;
            case TEXTURE_PHONG:
                UseProgram( programPhongTexture );
                break;
            case COLOUR:
                UseProgram( programColour );
                break;
        }
    }
}
