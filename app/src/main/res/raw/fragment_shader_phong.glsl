#version 100

precision highp float;

varying vec3 LightDirection_cameraspace;
varying vec3 Position_worldspace;
varying vec3 Normal_cameraspace;
varying vec3 EyeDirection_cameraspace;
varying float distance;

void main()
{
    vec3 LightSourceDiffuseIntensity = vec3(1.0, 1.0, 1.0);
    vec3 LightSourceSpecularIntensity = vec3(1.0, 1.0, 1.0);
    float alpha = 1.0;
    float LightPower = 1.0;

    vec3 MaterialDiffuseColor = vec3(0.7, 0.7, 0.7);
    vec3 MaterialAmbientColor = vec3(0.3, 0.3, 0.3);
    vec3 MaterialSpecularColor = vec3(0.3, 0.3, 0.3);

    vec3 Ambient = MaterialAmbientColor;

    vec3 normalisedLightDirection = normalize( LightDirection_cameraspace );
    vec3 normalisedNormal = normalize( Normal_cameraspace );
    vec3 normalisedEyeDirection = normalize( EyeDirection_cameraspace );

    float cosine = dot( normalisedLightDirection, normalisedNormal);
    if (cosine > 1.0)
        cosine = 1.0;
    else if (cosine < 0.0)
        cosine = 0.0;
    vec3 Diffuse = MaterialDiffuseColor * LightSourceDiffuseIntensity * cosine;

    vec3 Reflected = reflect( -normalisedLightDirection, normalisedNormal );
    cosine = dot( Reflected, normalisedEyeDirection);
        if (cosine > 1.0)
            cosine = 1.0;
        else if (cosine < 0.0)
            cosine = 0.0;

    vec3 Specular = MaterialSpecularColor * LightSourceSpecularIntensity * pow(cosine, alpha);

    gl_FragColor = vec4(Ambient + LightPower*Diffuse/pow(distance,2.0) + LightPower*Specular/pow(distance,2.0),1.0);

}
