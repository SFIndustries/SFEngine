package com.example.luka.openglestest;

/**
 * Created by Luka on 17.11.2015..
 */

import android.content.Context;
import android.opengl.GLSurfaceView;

import static android.opengl.Matrix.rotateM;

import static com.example.luka.openglestest.engine.GLCommon.*;

import com.example.luka.openglestest.engine.Controls;
import com.example.luka.openglestest.engine.GLObject;
import com.example.luka.openglestest.engine.GLObjectData;
import com.example.luka.openglestest.engine.GLObjectStatic;
import com.example.luka.openglestest.engine.TrackCamera;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.setLookAtM;
import static com.example.luka.openglestest.engine.GLCommon.SetEyePosition;

public class MojRenderer implements GLSurfaceView.Renderer
{
    private final Context context;

    private int program;

    float stariX = 0.0f, stariY = 0.0f;

    public GLObjectData sphereData, planeData, gridData;

    public GLObjectData sphere100data, sphere150data;
    public GLObject[] sphere100 = new GLObject[2];
    public GLObject[] sphere150 = new GLObject[27];
    float planeSphereDistancesMin;

    public int currentSphere = 0;
    public float radiusThreshold = 100, radiusAlpha0 = 125;

    int waterTexture, blueTexture, torusTexture, spaceTexture, EarthTexture;
    public static GLObject plane, sphere, grid, spaceSphere, Earth;
    public static List<GLObject> spheres = new ArrayList<>();

    int directionX = 1, directionY = -1, directionZ = 1;
    float speedX = 0.008f, speedY = 0.005f, speedZ = 0.003f;

    float[] eyePositionTemp = new float[4], upTm1 = new float[4];

    static float[] tempMatrix = new float[16], pitchMatrix = new float[16];
    static float[] tempVector = new float[4];
    float[] tempOrientation = new float[4], tempZAxis = new float[4];
    float[] spherePlaneDistanceTemp = new float[4];
    float[] planeCenterVector = new float[4];

    float spherePlaneDistance, planeCenterVectorLength;

    int i, j, k, x, y, z;
    int bufferIndex;

    float FPS = 0, FPSSum = 0, FPSFinal = 0, dt;
    long tStart = -1, t;
    float FPSInterval = 0.1f; // [s]
    int FPSi = 1, FPSWait = 10, FPSWaitI = 0;
    Runnable FPSRunnable = new Runnable()
    {
        @Override
        public void run()
        {
                ((MainActivity) context).textViewFPSCounter.setText(Float.toString(FPSFinal));
        }
    };


    public MojRenderer(Context context)
    {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        //glEnable(GL_BLEND);
        //glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);


        waterTexture = LoadTexture(R.raw.voda, context);
        blueTexture = LoadTexture(R.raw.wall, context);
        torusTexture = LoadTexture(R.raw.torus_texture, context);
        //spaceTexture = LoadTexture(R.raw.space_hd, context);
        spaceTexture = LoadTexture(R.raw.space_sphere_texture, context);
        try {
            EarthTexture = LoadTexture(R.raw.planet_texture_bake, context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        grid = new GLObject(context, R.raw.grid, LoadTexture(R.raw.checkers, context));

        //planeData = new GLObjectData(context, R.raw.f16_1_uv, LoadTexture(R.raw.avion_texture, context));
        //plane = new GLObject(context, R.raw.f16_1_uv, LoadTexture(R.raw.avion_texture, context));
        plane = new GLObject(context, R.raw.main_ship, LoadTexture(R.raw.main_ship_texture, context));
        plane.SetInitOrientation(new float[]{-plane.yAxis[0], -plane.yAxis[1], -plane.yAxis[2], 1});
        plane.velocity = 0.1f;
        //plane.Translate(0, 0, 1);
        Controls.SetControlledObject(plane);
        camera = new TrackCamera();
        ((TrackCamera) camera).SetTrackedObject( plane );

        sphereData = new GLObjectData(context, R.raw.sfera, waterTexture);

        Random rand = new Random();
        int dY = 10;
        for (int i = 0; i < 100; i++)
        {
            sphere = new GLObjectStatic( sphereData );
            sphere.TranslateTo( 0,/*rand.nextInt((200 - (-200)) + 1) - 200,*/
                                -i*dY,
                                /*rand.nextInt((200 - (-200)) + 1) - 200,*/
                                0/*rand.nextInt((200 - (-200)) + 1) - 200*/);
            spheres.add(sphere);
        }

        Earth = new GLObjectStatic(context, R.raw.earth_700, EarthTexture);
        Earth.Translate(0, -800, 0);

        //bufferIndex = spheres.get(0).bufferIndex;

//        sphere = new GLObject( sphereData );
//        sphere.SetTexture( torusTexture );
//        sphere.Translate(1.0f, -3.0f, 0);
//        spheres.add(sphere);
//        sphere = new GLObject( sphereData );
//        sphere.SetTexture( waterTexture );
//        sphere.Translate(0, -5.0f, 0);
//        spheres.add(sphere);
//        sphere = new GLObject( sphereData );
//        sphere.SetTexture( blueTexture );
//        sphere.Translate(-2.0f, -7.0f, 0);
//        spheres.add(sphere);

        spaceSphere = new GLObject( context, R.raw.sfera_unutra_150, spaceTexture );

        //spaceSphere.Rotate(90, 0, 0, 1);

        //------------------------------------------------------------------------------------
//        Random r = new Random();
//        sphere150data = new GLObjectData(context, R.raw.sfera_unutra_150, spaceTexture);
//        for(i = 0; i < 27; i++)
//        {
//            sphere150[i] = new GLObject( sphere150data );
//            sphere150[i].Rotate( r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat() );
//        }
//
//        currentSphere = 0;
//
//        i = 0;
//        for (x = -150; x <= 150; x+=150)
//        {
//            for (y = -150; y <= 150; y+=150)
//            {
//                for (z = -75; z <= 75; z+=75)
//                {
//                    if (       x == 0
//                            && y == 0
//                            && z == 0 )
//                    {
//                        currentSphere = i;
//                    }
//
//                    sphere150[i].TranslateTo(   sphere150[currentSphere].position[0] + x,
//                            sphere150[currentSphere].position[1] + y,
//                            sphere150[currentSphere].position[2] + z );
//
//                    i++;
//                }
//            }
//        }
        //------------------------------------------------------------------------------------

        programPhongTexture = InitProgram(context, R.raw.vertex_shader_phong, R.raw.fragment_shader_phong);
        programTexture = InitProgram(context, R.raw.vertex_shader_texture, R.raw.fragment_shader_texture);

        SetEyePosition( 0, 1.2f, 0.5f ); // iza aviona, avion gleda prema -y

        //glDisable(GL_DITHER);

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                    center[0], center[1], center[2], up[0], up[1], up[2]);

        UseProgram(programTexture);
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

        perspectiveM(projectionMatrix, 0, fov, (float) width / (float) height, nearZ, farZ);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        if (FPSWaitI > FPSWait)
            FPSCounter();
        else
            FPSWaitI++;

        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Showcase();

        if ( renderMode == TEXTURE_PHONG )
            SetUniformsShader();

        plane.UpdatePosition();

        Controls.SetOrientation(); // dretva 1 +
        camera.UpdateCamera(); // dretva 2 ?

        // pomakni sferu na mjesto aviona
        spaceSphere.TranslateTo( plane.position[0], plane.position[1], plane.position[2] );

        glDisable(GL_DEPTH_TEST);
        spaceSphere.Draw();
        glEnable(GL_DEPTH_TEST);

        plane.Draw();


        //------------------------------------------------------------------------------------

//        for(int i = 0; i<plane.position.length; i++)
//        {
//            planeCenterVector[i] = sphere150[currentSphere].position[i] - plane.position[i];
//        }
//
//        planeCenterVectorLength = length(planeCenterVector[0],planeCenterVector[1],planeCenterVector[2]);
//
//
//
//        if ( planeCenterVectorLength > 150 )
//        {
//            sphere150[currentSphere].alpha = 1f;
//
//            for(int i = 0; i<plane.position.length; i++)
//            {
//                planeCenterVector[i] = sphere150[0].position[i] - plane.position[i];
//            }
//            planeCenterVectorLength = length(planeCenterVector[0],planeCenterVector[1],planeCenterVector[2]);
//            currentSphere = 0;
//
//            for (j = 0; j < sphere150.length; j++)
//            {
//                if ( j == currentSphere )
//                    continue;
//
//                for(int i = 0; i<plane.position.length; i++)
//                {
//                    planeCenterVector[i] = sphere150[j].position[i] - plane.position[i];
//                }
//                if ( length(planeCenterVector[0],planeCenterVector[1],planeCenterVector[2]) < planeCenterVectorLength )
//                {
//                    currentSphere = j;
//                    planeCenterVectorLength = length(planeCenterVector[0],planeCenterVector[1],planeCenterVector[2]);
//                }
//            }
//        }
//
//        for(int i = 0; i<planeCenterVector.length; i++)
//        {
//            planeCenterVector[i] /= planeCenterVectorLength;
//        }
//
//        double sum = 0;
//        for(i = 0; i < 3; i++) //skalarni produkt
//        {
//            sum += planeCenterVector[i] * plane.orientation[i];
//        }
//
//        double angle = Math.toDegrees(Math.acos(sum));
//
//        if ( (angle > 90 || angle < -90) && planeCenterVectorLength > radiusThreshold  )
//        {
//            sphere150[currentSphere].alpha = 1 - ( planeCenterVectorLength - radiusThreshold )/(radiusAlpha0 - radiusThreshold);
//        }
//
//        for(i = 0; i<sphere150.length; i++)
//        {
//            sphere150[i].Draw();
//        }
//
//        sphere150[13].Draw();

        //------------------------------------------------------------------------------------

        //grid.Draw();

        // TODO - koristiti isti VBO za crtanje objekata istog tipa (npr. sfera)
//        for(GLObject object: spheres)
//        {
//            object.Draw();  // TODO - strpati sve objekte (npr staticne) u 1 VBO da se Draw() zove samo jednom,
//                            // TODO pomnoziti s matricama da se dobiju u prostoru
//        }

        Earth.Draw();

        //sphere.Draw();

        //plane.Rotate(0.5f, 0.0f, 1.0f, 0);
        //plane.Translate(0.0001f, 0.0001f, 0.0001f);

        // mozda glUniform za npr svjetlo tu staviti unaprijed samo jednom
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

    }

    public void handleTouchPress(float normalizedX, float normalizedY)
    {
//        stariX = normalizedX;
//        stariY = normalizedY;

    }

    public void handleTouchDrag(float normalizedX, float normalizedY)
    {
        if (normalizedX > stariX) eyePosition[0] += 10.0f * Math.abs(normalizedX - stariX);
        else if (normalizedX < stariX) eyePosition[0] -= 10.0f * Math.abs(normalizedX - stariX);

        if (normalizedY > stariY) eyePosition[2] += 10.0f * Math.abs(normalizedY - stariY);
        else if (normalizedY < stariY) eyePosition[2] -= 10.0f * Math.abs(normalizedY - stariY);

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2], center[0], center[1], center[2], 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);

        stariX = normalizedX;
        stariY = normalizedY;
    }

    public void Showcase()
    {
        if ( eyePosition[0] < -1 )
            directionX = 1;
        else if ( eyePosition[0] > 1 )
            directionX = -1;
        if ( eyePosition[1] < -1 )
            directionY = 1;
        else if ( eyePosition[1] > 1 )
            directionY = -1;
        if ( eyePosition[2] < -1 )
            directionZ = 1;
        else if ( eyePosition[2] > 1 )
            directionZ = -1;

        eyePosition[0] += directionX * speedX;
        eyePosition[1] += directionY * speedY;
        eyePosition[2] += directionZ * speedZ;

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2], center[0], center[1], center[2], 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix, 0);
    }

    // prosjecni fps
    // osvjezava se svakih FPSInterval sekundi
    public void FPSCounter()
    {
        t = System.nanoTime();
        if ( tStart == -1 )
            tStart = t;
        dt = (t - tStart) / 1000000000f;
        if ( dt >= FPSInterval )
        {
            FPSSum = FPSSum + 1f * FPS / dt;
            FPSFinal = FPSSum / FPSi;
            ((MainActivity) context).runOnUiThread(FPSRunnable);
            FPS = 0;
            tStart = t;
            FPSi++;
        }
        FPS++;
    }
}


