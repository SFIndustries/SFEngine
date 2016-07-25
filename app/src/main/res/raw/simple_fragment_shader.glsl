#version 100

precision highp float;
uniform vec4 u_Color;
uniform vec3 LightPosition_worldspace;
uniform vec3 ociste;

varying vec3 LightDirection_cameraspace;
varying vec2 _UV;
varying vec3 Position_worldspace;
varying vec3 Normal_cameraspace;
varying vec3 EyeDirection_cameraspace;

//uniform vec3 izvorSvjetlosti;
//varying vec3 LightDirection_cameraspace, Normal_cameraspace;
//float kd = 1.0;
//float Ii = 1.0;

void main()
{


//vec3 n = normalize( Normal_cameraspace );
//vec3 l = normalize( LightDirection_cameraspace );
//float intenzitet = Ii * kd * dot(n, l);
//gl_FragColor = vec4(intenzitet, intenzitet, intenzitet, 1);
//gl_FragColor = vec4(0, 0, 0, 1);

//--------------------------------------------------------------

	vec4 LightColor = vec4(1.0,1.0,1.0,1.0);

	float LightPower = 1.0;
	//vec4 LightPower = vec4(10.0,10.0,10.0,10.0);

	// Material properties
	vec3 MaterialDiffuseColor = vec3(0.5, 0.5, 0.5);
	vec3 MaterialAmbientColor = vec3(0.1,0.1,0.1);
	vec3 MaterialSpecularColor = vec3(0.01,0.01,0.01);


	//vec3 MaterialAmbientColor = MaterialDiffuseColor/1.5;
	//vec3 MaterialSpecularColor = MaterialDiffuseColor;
//
//	// Distance to the light
	float distance = length( LightPosition_worldspace - Position_worldspace );

//
//	// Normal of the computed fragment, in camera space
	vec3 n = normalize( Normal_cameraspace );
//	// Direction of the light (from the fragment to the light)
	vec3 l = normalize( LightDirection_cameraspace );
//
    float cosTheta = dot( n,l );
    if ( cosTheta < 0.0 )
    {
        cosTheta = 0.0;
    }
    else
    {
        if ( cosTheta > 1.0 )
        {
            cosTheta = 1.0;
        }
    }


//
//	// Eye vector (towards the camera)
	vec3 E = normalize(EyeDirection_cameraspace);
//	// Direction in which the triangle reflects the light
	vec3 R = reflect(-l,n);
//	// Cosine of the angle between the Eye vector and the Reflect vector,
//	// clamped to 0
//	//  - Looking into the reflection -> 1
//	//  - Looking elsewhere -> < 1
	float cosAlpha = dot( E,R );
    if ( cosAlpha < 0.0 )
    {
        cosAlpha = 0.0;
    }
    else
    {
        if ( cosAlpha > 1.0 )
        {
            cosAlpha = 1.0;
        }
    }
//


	vec4 color =
//		// Ambient : simulates indirect lighting
		vec4 (MaterialAmbientColor,1.0) +
//		// Diffuse : "color" of the object
		vec4 (MaterialDiffuseColor,1.0) * LightColor * LightPower * cosTheta /(distance*distance) + //+
//		// Specular : reflective highlight, like a mirror
		vec4 (MaterialSpecularColor,1.0) * LightColor * LightPower * cosAlpha*cosAlpha*cosAlpha*cosAlpha*cosAlpha / (distance*distance);

	gl_FragColor = color;

	//gl_FragColor = vec4(Normal_cameraspace,1.0);


}
