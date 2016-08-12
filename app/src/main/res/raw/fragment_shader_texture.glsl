#version 100

precision highp float;

varying vec2 v_TexCoordinate;

uniform sampler2D u_Texture;
uniform mat4 MVP;
uniform float alpha;

void main()
{
    //gl_FragColor = vec4(texture2D( u_Texture, v_TexCoordinate).rgb, alpha );
    gl_FragColor = texture2D( u_Texture, v_TexCoordinate);
}
