package com.example.luka.openglestest;

/**
 * Created by Luka on 17.11.2015..
 */

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import static android.opengl.GLES20.glDisable;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;

import static com.example.luka.openglestest.engine.GLCommon.*;

import com.example.luka.openglestest.engine.Controls;
import com.example.luka.openglestest.engine.GLCommon;
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

    public GLObjectData sphereData, planeData, gridData, projectileData;

    public GLObjectData sphere100data, sphere150data;
    public GLObject[] sphere100 = new GLObject[2];
    public GLObject[] sphere150 = new GLObject[27];

    int projectilesCount = 20, projectileIndex = 0;
    public GLObject[] projectiles;
    boolean[] projectilesActive = new boolean[projectilesCount];


    float planeSphereDistancesMin;

    public int currentSphere = 0;
    public float radiusThreshold = 100, radiusAlpha0 = 125;

    int waterTexture, blueTexture, torusTexture, spaceTexture, EarthTexture;
    public static GLObject plane, plane1, sphere, grid, spaceSphere, projectile;
    public static GLObjectStatic Earth, EarthBloom;
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


    public MojRenderer(Context contextp)
    {
        context = contextp;
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
        //plane.velocity = 0.05f; //0.1f;
        //plane.Translate(0, 0, 1);
        plane.InitCollisionObject( context, R.raw.main_ship_collision );
        plane.colour = new float[]{1, 0, 0, 1};
        //plane.SetRenderMode(COLOUR);

        plane1 = new GLObject(context, R.raw.main_ship, LoadTexture(R.raw.main_ship_texture, context));
        plane1.InitCollisionObject( context, R.raw.main_ship_collision );
        plane1.TranslateTo(0,-10,0);
        plane1.mass = 1f;
        plane1.momentOfIntertia = 1f;

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
        Earth.alpha = 1f;
        Earth.ScaleTo(1f,1f,1f);
        EarthBloom = new GLObjectStatic(context, R.raw.earth_700, EarthTexture);
        EarthBloom.alpha = 0.5f;
        EarthBloom.ScaleTo(1.2f,1.2f,1.2f);

        //Earth = new GLObject(context, R.raw.earth_700, EarthTexture);

        Earth.Translate(0, -700, 0);
        EarthBloom.Translate(0, -700, 0);

        projectileData = new GLObjectData(context, R.raw.projectile, 0);
        projectileData.InitCollisionObject(context, R.raw.projectile);
        projectiles = new GLObject[projectilesCount];
        projectilesActive = new boolean[projectilesCount];
        projectileIndex = -1;
        for (i = 0; i<projectilesCount; i++)
        {
            projectiles[i] = new GLObject( projectileData );
            //projectiles[i].SetInitOrientation(new float[]{-plane.yAxis[0], -plane.yAxis[1], -plane.yAxis[2], 1});
            projectilesActive[i] = false;
            projectiles[i].SetRenderMode( COLOUR );
            projectiles[i].mass = .001f;
            projectiles[i].momentOfIntertia = .01f;

        }

        GLCommon.boundingSphere = new GLObject(context, R.raw.sphere1, 0);
        GLCommon.boundingSphere.SetRenderMode(COLOUR);

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
        programColour = InitProgram(context, R.raw.vertex_shader_colour, R.raw.fragment_shader_colour);

        SetEyePosition( 0, 2f, 2f ); // iza aviona, avion gleda prema -y

        //glDisable(GL_DITHER);

        setLookAtM( viewMatrix, 0, eyePosition[0], eyePosition[1], eyePosition[2],
                    center[0], center[1], center[2], up[0], up[1], up[2]);

        //UseProgram(programTexture);


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

//        if ( plane.Collision( plane1 ) )
//            plane.SetRenderMode( COLOUR );
//        else
//            plane.SetRenderMode( TEXTURE );

        plane.Collision( plane1 );
        for(i = 0; i < plane.angularVelocities.size(); i++)
            plane.angularVelocities.set(i, 0f);

        plane.UpdatePosition();
        plane1.UpdatePosition();
        plane1.UpdateRotation();

        Controls.SetOrientation(); // dretva 1 +
        camera.UpdateCamera(); // dretva 2 ?

        if ( Controls.accelerationControlledObject ) {
            plane.velocity[0] += plane.orientation[0] * plane.accelerationScalar;
            plane.velocity[1] += plane.orientation[1] * plane.accelerationScalar;
            plane.velocity[2] += plane.orientation[2] * plane.accelerationScalar;
            plane.velocityScalar = (float) Matrix.length(plane.velocity[0],plane.velocity[1],plane.velocity[2]);
        }

        else if ( Controls.decelerationControlledObject ) {
            plane.velocity[0] -= plane.orientation[0] * plane.accelerationScalar;
            plane.velocity[1] -= plane.orientation[1] * plane.accelerationScalar;
            plane.velocity[2] -= plane.orientation[2] * plane.accelerationScalar;
            plane.velocityScalar = (float) Matrix.length(plane.velocity[0],plane.velocity[1],plane.velocity[2]);
        }


//        for(i = 0; i < projectilesCount; i++)
//        {
//            if ( projectilesActive[i] == false )
//                continue;
//
//            projectiles[i].UpdatePosition();
//        }

        plane.fireRateCounter++;
        if ( Controls.fire )
        {
            if ( plane.fireRateCounter >= plane.fireRate )
            {
                plane.fireRateCounter = 0;

                projectileIndex = (projectileIndex + 1) % projectilesCount;
                //tempVector = plane.initOrientation.clone();
                //multiplyMV(projectiles[projectileIndex].orientation, 0, plane.rotationMatrix, 0, tempVector, 0);
                projectiles[projectileIndex].rotationMatrix = plane.rotationMatrix.clone();
                projectiles[projectileIndex].TranslateTo(plane.gunLeftWing[0], plane.gunLeftWing[1], plane.gunLeftWing[2]);
                projectiles[projectileIndex].velocity[0] = plane.velocity[0] + plane.orientation[0] * 2;
                projectiles[projectileIndex].velocity[1] = plane.velocity[1] + plane.orientation[1] * 2;
                projectiles[projectileIndex].velocity[2] = plane.velocity[2] + plane.orientation[2] * 2;
                projectilesActive[projectileIndex] = true;

                projectileIndex = (projectileIndex + 1) % projectilesCount;
                //tempVector = plane.initOrientation.clone();
                //multiplyMV(projectiles[projectileIndex].orientation, 0, plane.rotationMatrix, 0, tempVector, 0);
                projectiles[projectileIndex].rotationMatrix = plane.rotationMatrix.clone();
                projectiles[projectileIndex].TranslateTo(plane.gunRightWing[0], plane.gunRightWing[1], plane.gunRightWing[2]);
                projectiles[projectileIndex].velocity[0] = plane.velocity[0] + plane.orientation[0] * 2;
                projectiles[projectileIndex].velocity[1] = plane.velocity[1] + plane.orientation[1] * 2;
                projectiles[projectileIndex].velocity[2] = plane.velocity[2] + plane.orientation[2] * 2;
                projectilesActive[projectileIndex] = true;
            }

        }


        // pomakni sferu na mjesto aviona
        spaceSphere.TranslateTo( plane.position[0], plane.position[1], plane.position[2] );

        glDisable(GL_DEPTH_TEST);
        //
        glEnable(GL_BLEND);
        //glBlendEquation( GL_FUNC_SUBTRACT );
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        spaceSphere.Draw();

        // TODO - pretvoriti ovu drugu sferu u static object
//        Earth.ScaleTo(1.02f,1.02f,1.02f);
//        Earth.alpha = 0.5f;
//        Earth.Draw();
//        Earth.ScaleTo(1f,1f,1f);
//        Earth.alpha = 1;
//        Earth.Draw();

        EarthBloom.Draw();
        Earth.Draw();

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        for(i = 0; i < projectilesCount; i++)
        {
            if ( projectilesActive[i] == false )
                continue;

            projectiles[i].UpdatePosition();
            plane1.Collision( projectiles[i] );

            projectiles[i].Draw();
            //projectiles[i].DrawBoundingSpheres();
        }

        plane.Draw();
        //plane.DrawBoundingSpheres();

        plane1.Draw();

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

        //glDisable( GL_DEPTH_TEST );



        //sphere.Draw();

        //plane.Rotate(0.5f, 0.0f, 1.0f, 0);
        //plane.Translate(0.0001f, 0.0001f, 0.0001f);

    }

//    public void handleTouchPress(float normalizedX, float normalizedY)
//    {
//        stariX = normalizedX;
//        stariY = normalizedY;
//    }

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


