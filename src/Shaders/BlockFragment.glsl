#version 150 core

in vec2 pass_TexCoord;
in vec3 pass_WorldPosition;

uniform sampler2D tex;

void main(void) {
    float a1 = pass_WorldPosition.x / 64.0f;
    gl_FragColor = mix(texture2D( tex, pass_TexCoord ), vec4(0.0f,0.0f,0.0f,1.0f), a1);
    }