#version 150 core

in vec3 in_Position;

out vec4 pass_Color;

void main(void) {
    gl_Position = vec4(in_Position, 1.0f);

    pass_Color = vec4(1.0f, 0.7f, 0.7f, 1.0f);
}