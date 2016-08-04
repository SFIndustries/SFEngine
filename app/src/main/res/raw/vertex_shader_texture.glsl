#version 100


varying vec2 v_TexCoordinate;

attribute vec2 a_Texture;
attribute vec4 a_Position;

uniform mat4 MVP;

void main()
{
    gl_Position = MVP * a_Position;

    v_TexCoordinate = a_Texture;
}
