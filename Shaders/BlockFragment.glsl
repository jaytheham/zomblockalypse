#version 150 core

in vec3 pass_WorldPosition;
in vec3 pass_Normal;
in float pass_BlockType;

uniform vec3 uLightPositions[8];
uniform vec4 uLightColors[8];
uniform sampler2D uTexture;

void main(void) {

    // Calculating tex coords
    //--
    const float TEX_ATLAS_WIDTH = 20.0f;

    vec2 textureCoords;

    if (pass_Normal.y != 0.0f) {
        textureCoords.x = mod(pass_WorldPosition.x, 1.0f);
        textureCoords.y = mod(pass_WorldPosition.z, 1.0f);
    }
    else {
        textureCoords.x = mod(pass_WorldPosition.x + pass_WorldPosition.z, 1.0f);
        textureCoords.y = mod(pass_WorldPosition.y, 1.0f);
        textureCoords.y = 1.0f - textureCoords.y;
    }

    if (textureCoords.x < 0.02f) {
        textureCoords.x = 0.02f;
    }
    else if (textureCoords.x > 0.98f) {
        textureCoords.x = 0.98f;
    }

    if (textureCoords.y < 0.02f) {
            textureCoords.y = 0.02f;
    }
    else if (textureCoords.y > 0.98f) {
        textureCoords.y = 0.98f;
    }

    textureCoords.x += pass_BlockType;
    while (textureCoords.x >= TEX_ATLAS_WIDTH) {
        textureCoords.x -= TEX_ATLAS_WIDTH;
        textureCoords.y += 1.0f;
    }
    textureCoords.x /= TEX_ATLAS_WIDTH;
    textureCoords.y /= TEX_ATLAS_WIDTH;
    //-

    int i = 0;
    vec3 finalLighting = vec3(0.0f, 0.0f, 0.0f);

    // Stop if reach a light with no range
    while (uLightColors[i].a != 0.0f && i < 8) {

        float dist = distance(uLightPositions[i], pass_WorldPosition);

        if (dist < uLightColors[i].a) {

            vec3 lightDirection = pass_WorldPosition - uLightPositions[i];
            lightDirection = normalize(lightDirection);

            float dotP = dot(lightDirection, pass_Normal);

            if (dotP < 0.0f) {

                // Scale distance to range {0.0-1.0}
                dist = dist / uLightColors[i].a;
                dist += (dist - pow(dist,2));
                finalLighting += uLightColors[i].rgb * ((1.0f - dist) * (-1 * dotP + 0.5));
                //finalLighting += uLightColors[i].rgb * (-1 * dot(lightDirection, pass_Normal)) *(1.0f - dist/uLightColors[i].a);
            }
        }
        i += 1;
    }

    vec4 textureColor = texture2D(uTexture, textureCoords);

    // Set ambient level
    if (finalLighting.r < 0.05) finalLighting.r = 0.05f;
    if (finalLighting.g < 0.05) finalLighting.g = 0.05f;
    if (finalLighting.b < 0.05) finalLighting.b = 0.05f;

    gl_FragColor = vec4((finalLighting.r * textureColor.r),
                        (finalLighting.g * textureColor.g),
                        (finalLighting.b * textureColor.b),1.0f);
}