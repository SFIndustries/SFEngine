#version 100

precision highp float;

attribute vec4 a_Position, a_Normal;

uniform mat4 MVP, M, V;
uniform vec3 LightPosition_worldspace;
uniform vec3 ociste;

varying vec3 LightDirection_cameraspace;
varying vec3 Position_worldspace;
varying vec3 Normal_cameraspace;
varying vec3 EyeDirection_cameraspace;
varying float distance;

void main()
{
    Position_worldspace = (M * a_Position).xyz;
    distance = length(Position_worldspace - LightPosition_worldspace);

    gl_Position = MVP * a_Position;

    LightDirection_cameraspace = ( M * vec4(LightPosition_worldspace,1.0) -  M * a_Position).xyz;

    EyeDirection_cameraspace = ociste - (V * M * a_Position).xyz;

    Normal_cameraspace = ( M * a_Normal).xyz;



}
