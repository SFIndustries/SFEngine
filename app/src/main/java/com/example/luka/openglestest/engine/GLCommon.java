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
    public static int program;

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

    public static float[] eyePosition = {1.0f, 1.0f, 1.0f};
    public static int eyePositionLocation;

    public static float[] center = {0.0f, 0.0f, 0.0f};

    public static float[] up = {0.0f, 0.0f, 1.0f};

    public static float fov = 45;
    public static float nearZ = 0.1f;
    public static float farZ = 100;

    public static List<Integer> textureIDList = new ArrayList<>();

    public static void InitShaders(Context context, int vertexShaderId, int fragmentShaderId)
    {
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, vertexShaderId);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, fragmentShaderId);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        glUseProgram(program);

        aPositionLocation = glGetAttribLocation(program, "a_Position");
        aNormalLocation = glGetAttribLocation(program, "a_Normal");
        aTextureLocation = glGetAttribLocation(program, "a_Texture");
        textureUniformLocation = glGetUniformLocation(program, "u_Texture");

        viewMatrixLocation = glGetUniformLocation(program, "V");
        modelMatrixLocation = glGetUniformLocation(program, "M");
        modelViewProjectionMatrixLocation = glGetUniformLocation(program, "MVP");

        lightPositionLocation = glGetUniformLocation(program, "LightPosition_worldspace");
        eyePositionLocation = glGetUniformLocation(program, "ociste");

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

        // Read in the resource
        //int resID = context.getResources().getIdentifier(resourceName, "raw", context.getPackageName());
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceName, options);

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
}
