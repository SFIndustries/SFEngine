#version 100

attribute vec4 a_Position;
uniform mat4 MVP;

void main()
{
    gl_Position = MVP * a_Position;
}
