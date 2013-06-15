#version 150 core

in vec2 in_Position;
in vec2 in_TexCoord;

out vec2 pass_TexCoord;

void main(void) {
    gl_Position = vec4(in_Position, -1.0f, 1.0f);
    pass_TexCoord = in_TexCoord;
}