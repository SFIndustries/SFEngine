#version 100

precision lowp float;
uniform vec4 colour;

void main()
{
    gl_FragColor = colour;
}
