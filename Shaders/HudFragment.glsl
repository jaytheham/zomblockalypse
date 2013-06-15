#version 150 core

in vec2 pass_TexCoord;

uniform sampler2D uTexture;

void main(void) {

    gl_FragColor = texture2D(uTexture, pass_TexCoord);
    //gl_FragColor = vec4(1.0f, 0.0f, 1.0f, 1.0f);

}