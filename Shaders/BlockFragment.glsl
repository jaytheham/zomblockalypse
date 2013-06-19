#version 150 core

in vec3 pass_WorldPosition;
in vec3 pass_Normal;
in vec2 textureCoords;

uniform vec3 uLightPositions[8];
uniform vec4 uLightColors[8];
uniform sampler2D uTexture;

void main(void) {

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
                        (finalLighting.b * textureColor.b),
                         1.0f);
}