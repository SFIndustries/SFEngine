#version 100

precision highp float;

varying vec3 LightDirection_cameraspace;
varying vec3 Position_worldspace;
varying vec3 Normal_cameraspace;
varying vec3 EyeDirection_cameraspace;
varying float distance;

varying vec2 v_TexCoordinate;
uniform sampler2D u_Texture;

void main()
{
    vec3 LightSourceDiffuseIntensity = vec3(1.0, 1.0, 1.0);
    vec3 LightSourceSpecularIntensity = vec3(1.0, 1.0, 1.0);

    float alpha = 5.0;
    float LightPower = 1.5;
    float lightAmbientDamping = 2.0;
    float lightParallelSurface = 0.2;
    float zeroThreshold = 0.01;
    float lightDistanceDamping = 0.0;

    //vec3 MaterialDiffuseColor = vec3(0.7, 0.7, 0.7);
    vec3 MaterialDiffuseColor = texture2D( u_Texture, v_TexCoordinate ).rgb;
    vec3 MaterialAmbientColor = vec3(0.3, 0.3, 0.3);
    vec3 MaterialSpecularColor = vec3(0.5, 0.5, 0.5); // bilo je 0.3

    vec3 Ambient = MaterialAmbientColor;

    vec3 normalisedLightDirection = normalize( LightDirection_cameraspace );
    vec3 normalisedNormal = normalize( Normal_cameraspace );
    vec3 normalisedEyeDirection = normalize( EyeDirection_cameraspace );

    float cosine = dot( normalisedLightDirection, normalisedNormal );

//    Sive straznje povrsine
//    if ( cosine > 1.0 )
//        cosine = 1.0;
//    else if ( cosine < 0.0 )
//        cosine = 0.0;

//  Osvijetljene straznje povrsine
    if ( cosine > 1.0 )
        cosine = 1.0;
    else if ( cosine < 0.0 )
        cosine = -cosine / lightAmbientDamping;
//    else if ( abs( cosine ) < zeroThreshold ) // (cosine == 0)
//        cosine = lightParallelSurface;

    vec3 Diffuse = MaterialDiffuseColor * LightSourceDiffuseIntensity * cosine;


    vec3 Reflected = reflect( -normalisedLightDirection, normalisedNormal );
    cosine = dot( Reflected, normalisedEyeDirection);
        if (cosine > 1.0)
            cosine = 1.0;
        else if (cosine < 0.0)
            cosine = 0.0;

    vec3 Specular = MaterialSpecularColor * LightSourceSpecularIntensity * pow(cosine, alpha);

    gl_FragColor = vec4(Ambient + LightPower*Diffuse/pow(distance,lightDistanceDamping) + LightPower*Specular/pow(distance,lightDistanceDamping),1.0);

}
