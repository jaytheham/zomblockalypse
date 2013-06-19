#version 150 core

in vec4 in_Position;
in vec2 in_BlockType;

uniform mat4 transformMatrix;

out vec3 pass_WorldPosition;
out vec3 pass_Normal;
out vec2 textureCoords;

void main(void) {

    // Calculating tex coords
    //
    const float TEX_ATLAS_WIDTH = 20.0f;
    int checker = int(in_Position.w);

    if (checker & 16) {
        textureCoords.x = 0.98f;
    }
    else {
        textureCoords.x = 0.02f;
    }

    if (checker & 32) {
        textureCoords.y = 0.98f;
    }
    else {
        textureCoords.y = 0.02f;
    }

    textureCoords.x += in_BlockType.x;
    while (textureCoords.x >= TEX_ATLAS_WIDTH) {
        textureCoords.x -= TEX_ATLAS_WIDTH;
        textureCoords.y += 1.0f;
    }
    textureCoords.x /= TEX_ATLAS_WIDTH;
    textureCoords.y /= TEX_ATLAS_WIDTH;


    // Calculate normal
    //
    vec3 normal;
    normal.x = (checker & 1);
    normal.y = (checker & 2) / 2;
    normal.z = (checker & 4) / 4;
    if (checker & 8) {
        normal *= -1;
    }

    pass_Normal = normal;

    pass_WorldPosition = in_Position.xyz;

    gl_Position = transformMatrix * vec4(in_Position.xyz, 1.0f);

}