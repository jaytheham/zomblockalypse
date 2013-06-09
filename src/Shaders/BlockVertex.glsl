#version 150 core

in vec3 in_Position;
in vec2 in_TexCoord;
in float in_BlockType;

uniform mat4 transformMatrix;

out vec2 pass_TexCoord;
varying out vec3 pass_WorldPosition;

void main(void) {
    pass_WorldPosition = in_Position;
    gl_Position = transformMatrix * vec4(in_Position, 1.0f);

    in_TexCoord.x += in_BlockType;

    // 20.0f is the number of block textures wide and high the texture atlas is
    while (in_TexCoord.x > 20.0f) {
        in_TexCoord.y += 1.0f;
        in_TexCoord.x -= 20.0f;
    }


    pass_TexCoord.x = in_TexCoord.x / 20.0f;
    pass_TexCoord.y = in_TexCoord.y / 20.0f;
}