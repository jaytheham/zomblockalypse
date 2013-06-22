#version 150 core

in vec2 pass_TexCoord;

uniform sampler2D uTexture;

void main(void) {

    vec4 color = texture2D(uTexture, pass_TexCoord);
    if (color.a < 0.5f) {
        discard;
    }
    gl_FragColor = color;

}