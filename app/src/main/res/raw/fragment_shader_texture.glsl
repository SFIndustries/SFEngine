#version 100

precision highp float;

varying vec2 v_TexCoordinate;

uniform sampler2D u_Texture;
uniform mat4 MVP;

void main()
{
    gl_FragColor = texture2D( u_Texture, v_TexCoordinate );
}
