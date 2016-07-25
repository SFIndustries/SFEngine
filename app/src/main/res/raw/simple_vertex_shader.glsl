#version 100

//attribute vec4 a_Position, a_Normal, izvorSvjetlosti;
attribute vec4 a_Position, a_Normal;
uniform mat4 MVP, M, V;
uniform vec3 LightPosition_worldspace;
uniform vec3 ociste;

uniform vec2 UV;

varying vec3 LightDirection_cameraspace;
varying vec2 _UV;
varying vec3 Position_worldspace;
varying vec3 Normal_cameraspace;
varying vec3 EyeDirection_cameraspace;

//uniform mat4 MVP;
//varying vec3 LightDirection_cameraspace, Normal_cameraspace;

void main()
{


//var = vec4(0, 0, 1, 1);

//vec3 vertexPosition_cameraspace = (V * M * a_Position).xyz;
//vec3 LightPosition_cameraspace = (V * izvorSvjetlosti).xyz;

//vec3 LightDirection_cameraspace = (izvorSvjetlosti - a_Position).xyz;
//vec3 Normal_cameraspace = (/*V * M **/ a_Normal).xyz;

//L = normalize(L);
//vec4 N = normalize(Normal_cameraspace);
//float KN = dot(L,N);


gl_Position = MVP * a_Position;

Position_worldspace = (M * a_Position).xyz;

vec3 vertexPosition_cameraspace = ( V * M * a_Position).xyz;
EyeDirection_cameraspace = ociste - vertexPosition_cameraspace;

vec3 LightPosition_cameraspace = ( V * M * vec4(LightPosition_worldspace,1)).xyz;
LightDirection_cameraspace = LightPosition_cameraspace + EyeDirection_cameraspace;

//Normal_cameraspace = ( transpose(inverse(V * M)) * a_Normal).xyz;
Normal_cameraspace = ( (V * M) * a_Normal).xyz;
//Normal_cameraspace = a_Normal.xyz;

}