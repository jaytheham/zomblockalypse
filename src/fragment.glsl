#version 150 core

in vec4 pass_Color;

void main(void) {
    gl_FragColor = pass_Color;
    }