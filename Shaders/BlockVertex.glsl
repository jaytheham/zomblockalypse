#version 150 core

in vec4 in_Position;
in vec2 in_BlockType;

uniform mat4 transformMatrix;

out vec3 pass_WorldPosition;
out vec3 pass_Normal;
out vec2 pass_TextureCoords;
out vec3 pass_AmbientOcclusion;

void main(void) {

    pass_AmbientOcclusion = vec3(1 - in_BlockType.y / 8,
                                 1 - in_BlockType.y / 8,
                                 1 - in_BlockType.y / 8);

    // Calculating tex coords
    //
    const float TEX_ATLAS_WIDTH = 20.0f;
    int checker = int(in_Position.w);

    if (checker & 16) {
        pass_TextureCoords.x = 0.98f;
    }
    else {
        pass_TextureCoords.x = 0.02f;
    }

    if (checker & 32) {
        pass_TextureCoords.y = 0.98f;
    }
    else {
        pass_TextureCoords.y = 0.02f;
    }

    pass_TextureCoords.x += in_BlockType.x;
    while (pass_TextureCoords.x >= TEX_ATLAS_WIDTH) {
        pass_TextureCoords.x -= TEX_ATLAS_WIDTH;
        pass_TextureCoords.y += 1.0f;
    }
    pass_TextureCoords.x /= TEX_ATLAS_WIDTH;
    pass_TextureCoords.y /= TEX_ATLAS_WIDTH;


    // Calculate normal
    //
    pass_Normal.x = (checker & 1);
    pass_Normal.y = (checker & 2) / 2;
    pass_Normal.z = (checker & 4) / 4;
    if (checker & 8) {
        pass_Normal *= -1;
    }

    pass_WorldPosition = in_Position.xyz;

    gl_Position = transformMatrix * vec4(in_Position.xyz, 1.0f);

}