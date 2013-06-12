#version 150 core

in vec3 in_Position;
in vec2 in_TexCoord;
in float in_BlockType;
in float in_VertNormal;

uniform mat4 transformMatrix;

out vec2 pass_TexCoord;
varying out vec3 pass_WorldPosition;
varying out vec3 pass_Normal;

void main(void) {

    vec3 normal;
    float n;
    if (in_VertNormal == 2.0f)
        normal = vec3(0.0f, 1.0f, 0.0f);
    else if (in_VertNormal == 1.0f)
        normal = vec3(1.0f, 0.0f, 0.0f);
    else if (in_VertNormal == 3.0f)
        normal = vec3(0.0f, 0.0f, 1.0f);
    else if (in_VertNormal == -1.0f)
        normal = vec3(-1.0f, 0.0f, 0.0f);
    else if (in_VertNormal == -3.0f)
        normal = vec3(0.0f, 0.0f, -1.0f);
    else // -2
        normal = vec3(0.0f, -1.0f, 0.0f);

    pass_Normal = normal;


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