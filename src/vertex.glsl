#version 150 core

in vec3 in_Position;
//in vec3 in_Color;

uniform mat4 transformMatrix;

out vec4 pass_Color;

void main(void) {
    gl_Position = transformMatrix * vec4(in_Position, 1.0f);
    float a = 0.0f;
    float b = 0.0f;
    float c = abs(in_Position.x / 128.0f);

    vec4 myColor;
    if (mod(in_Position.x, 2.0f) > 0) {
        a = 0.5f;
    }

    if (mod(in_Position.z, 2.0f) > 0) {
        b = 0.5f;
    }

    a = (mod(in_Position.x, 32.0f)/32);
    b = (mod(in_Position.z, 32.0f)/32);
    c = (mod(in_Position.y, 2.0f)/2);


    pass_Color = vec4(a, b, c, 1.0f);
    //pass_Color = vec4(in_Color, 1.0f);
}