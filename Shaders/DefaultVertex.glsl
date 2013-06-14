#version 150 core

in vec3 in_Position;

uniform mat4 transformMatrix;

out vec4 pass_Color;

void main(void) {
    gl_Position = transformMatrix * vec4(in_Position, 1.0f);

    pass_Color = vec4(1.0f, 1.0f, 1.0f, 1.0f);
}