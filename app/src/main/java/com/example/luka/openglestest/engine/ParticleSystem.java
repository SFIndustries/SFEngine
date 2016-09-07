package com.example.luka.openglestest.engine;

import java.util.List;
import java.util.Random;

/**
 * Created by Jasmin on 27.8.2016..
 */

public class ParticleSystem
{
    List<Particle> particleVector;

    int particleCount, spawningRate, particleLifetime;
    float particleSide; // ???????????????????????????????????????????????????????????????????????????????????

    float[] initPosVector;// new float[3];
    float[] initVelocityVector;

    List<float[]> forceVector;
    float[] netForce;

    int newParticleIndex = 0;
    short i;

    float CONST_sumVelocity, CONST_sumPos, CONST_sumLifetime, CONST_sumSide;

    ParticleSystem(int particleCountP,
                   int spawningRateP,
                   int particleLifetimeP,
                   float particleSideP,
                   float[] initPosVectorP,
                   float[] initVelocityVectorP,
                   List<float[]> forceVectorP)

    {
        particleCount = particleCountP;
        spawningRate = spawningRateP;
        particleLifetime = particleLifetimeP;
        particleSide = particleSideP;
        initPosVector = initPosVectorP;
        initVelocityVector = initVelocityVectorP;

        for (i = 0; i < particleCount; i++) particleVector.add(new Particle());
        forceVector = forceVectorP;
    }

    void findNewState()
    {
        Random generator = new Random();
        float rand = generator.nextFloat();

        netForce = new float[] {0.0f, 0.0f, 0.0f};

        for (i = 0; i < forceVector.size(); i++)
        {
            netForce[0] += forceVector.get(i)[0];
            netForce[1] += forceVector.get(i)[1];
            netForce[2] += forceVector.get(i)[2];
        }

        i = 0;

        if (rand <= 1.0f / spawningRate)
        {
            while(particleVector.get(i).alive) i++;

            if (i < particleCount - 1)
            {
                float[] temp = new float[3];

                particleVector.get(i).setParams(new float[] {initVelocityVector[0] + generator.nextFloat() * CONST_sumVelocity, // ---------------- CONST_sumVelocity -------------------
                                initVelocityVector[1] + generator.nextFloat() * CONST_sumVelocity,
                                initVelocityVector[2] /*+ glm::linearRand(-sumBrzina, sumBrzina)*/},

                        new float[] {initPosVector[0] + generator.nextFloat() * CONST_sumPos * 2 - CONST_sumPos, // ---------------- CONST_sumPos -------------------
                            initPosVector[1] + generator.nextFloat() * CONST_sumPos * 2 - CONST_sumPos,
                            initPosVector[2] /*+ glm::linearRand(-sumPolozaj, sumPolozaj)*/},

                        (int)(particleLifetime * (1.0f + generator.nextFloat() * CONST_sumLifetime * 2 - CONST_sumLifetime)),

                        particleSide * (1.0f + generator.nextFloat() * CONST_sumSide * 2 - CONST_sumSide));

            }
        }

        for (i = 0; i < particleCount; i++)
        {
            if (particleVector.get(i).alive)
            {
                particleVector.get(i).findNewPos(netForce);
            }
        }
    }

    void draw()
    {
        for (i = 0; i < particleCount; i++)
        {
            if (particleVector.get(i).alive)
            {
                particleVector.get(i).draw();
            }
        }
    }
}
