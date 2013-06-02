#version 150 core

flat in vec4 pass_Color;

void main(void) {
    gl_FragColor = pass_Color;
    }