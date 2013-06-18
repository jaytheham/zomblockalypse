#version 150 core

in vec3 in_Position;
in float in_VertNormal;
in float in_BlockType;

uniform mat4 transformMatrix;

out vec3 pass_WorldPosition;
out vec3 pass_Normal;
out float pass_BlockType;

void main(void) {

    vec3 normal;
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
        normal = vec3(1.0f, 0.0f, 0.0f);

    pass_Normal = normal;

    pass_BlockType = in_BlockType;
    pass_WorldPosition = in_Position;
    gl_Position = transformMatrix * vec4(in_Position, 1.0f);

}