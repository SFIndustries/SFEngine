package com.example.luka.openglestest.engine;

/**
 * Created by Jasmin on 27.8.2016..
 */

public class Particle
{
    int lifetime, lifetimeCounter;
    float[] pos, prevPos, velocity;
    float edge;
    boolean alive;

    Particle()
    {
        alive = false;
    }

    Particle(Particle p)
    {
        alive = p.alive;
    }


    void setParams(float[] velocityP, float[] posP, int lifetimeP, float edgeP)
    {
        for (int i = 0; i < 3; i++)
        {
            velocity[i] = velocityP[i];
            pos[i] = posP[i];
            edge = edgeP;
        }

        lifetime = lifetimeP;
        lifetimeCounter = 0;
        alive = true;
    }

    void findNewPos (float[] netForce)
    {
        if (lifetimeCounter >= lifetime)
        {
            alive = false;
        }
        else
        {
            lifetimeCounter++;

            for (int i = 0; i < 3; i++)
            {
                velocity[i] += netForce[i];
                pos[i] += velocity[i];
            }
        }
    }

    void draw ()
    {
        // -------------------------------------------------------------------------------------------------------------------
    }
}
