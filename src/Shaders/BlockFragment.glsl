#version 150 core

in vec2 pass_TexCoord;
in vec3 pass_WorldPosition;

// Lights should never be on cube boundaries on all axises
// Ideally they should be (x.5f, y.5f, z.5f)
uniform vec3 uLightPositions[16];
uniform vec4 uLightColors[16];
uniform vec3 uWorldPosOffset;
uniform sampler2D uTexture;
uniform isamplerBuffer uBlocks;

float intbound(in float s, in float ds) {
    // Find the smallest positive t such that s+t*ds is an integer.
    if (ds < 0.0f) {
        s = -s;
        ds = -ds;
    }
    s = mod(s, 1.0f);
    return ((1-s)/ds);

}

int signum(in float var) {
    if (var > 0.0f) {
        return 1;
    }
    else if (var < 0.0f) {
        return -1;
    }
    else {
        return 0;
    }
}

int raycast(in vec3 origin, in vec3 direction, in vec3 lightPos, in float lightPower) {

    // Otherwise pixels on a face facing the light will be unlit
    origin += 0.00001 * direction;

    // Cube containing origin point.
    int x = int(origin.x);
    int y = int(origin.y);
    int z = int(origin.z);

    // Break out direction vector.
    float dx = direction.x;
    float dy = direction.y;
    float dz = direction.z;

    // Direction to increment x,y,z when stepping.
    int stepX = signum(dx);
    int stepY = signum(dy);
    int stepZ = signum(dz);

    float tMaxX = intbound(origin.x, dx);
    float tMaxY = intbound(origin.y, dy);
    float tMaxZ = intbound(origin.z, dz);

    // The change in t when taking a step (always positive).
    float tDeltaX = stepX/dx;
    float tDeltaY = stepY/dy;
    float tDeltaZ = stepZ/dz;

    // Avoids an infinite loop.
    // Assumes we're on the light
    if (dx == 0 && dy == 0 && dz == 0) {
        return 0;
    }

    int lx = int(lightPos.x);
    int ly = int(lightPos.y);
    int lz = int(lightPos.z);

    // Rescale from units of 1 cube-edge to units of 'direction' so we can
    // compare with 't'.
    float radius = lightPower / sqrt(dx*dx+dy*dy+dz*dz);

    while (1) {

        // If this coordinate is a block return hit (-1)
        if (texelFetch(uBlocks, (x + (y * 32 * 32) + (z * 32))) != 0) {
            return -1;
        }
        else if (lx == x && ly == y && lz == z) {
            // Hit the light
            return 0;
        }

        if (tMaxX < tMaxY) {
            if (tMaxX < tMaxZ) {
                if (tMaxX > radius) break;
                x += stepX;
                tMaxX += tDeltaX;
            } else {
                if (tMaxZ > radius) break;
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
        } else {
            if (tMaxY < tMaxZ) {
                if (tMaxY > radius) break;
                y += stepY;
                tMaxY += tDeltaY;
            } else {
                if (tMaxZ > radius) break;
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
        }
    }
    return -1;
    // Presume if we get here light is too far away, so ignore it
}

void main(void) {

    float MAX_LIGHT_DISTANCE = 32.0f;
    int i = 0;
    vec3 finalLighting = vec3(0.0f, 0.0f, 0.0f);

    while (i < 16 && uLightColors[i].a != 0.0f) {

        float dist = distance(uLightPositions[i], pass_WorldPosition);
        if (dist < MAX_LIGHT_DISTANCE) {
            // Scale distance to range {0.0-1.0}
            dist = dist / uLightColors[i].a;
            dist += (dist - pow(dist,2));
            dist += (dist - pow(dist,2));

            //dist += 1.0f - (1.0f / uLightColors[i].a);

            vec3 lightDir = normalize(uLightPositions[i] - pass_WorldPosition);
            int result = raycast(pass_WorldPosition - uWorldPosOffset,
                                lightDir,
                                uLightPositions[i] - uWorldPosOffset,
                                uLightColors[i].a);

            if (result == 0) { // Hit the light
                finalLighting += uLightColors[i].rgb * (1.0f - dist);
            }
            //else {
                // Hit a block, ambient light
                //gl_FragColor = mix(vec4(0.0f,0.0f,0.0f,1.0f), texture2D(uTexture, pass_TexCoord), dist);
                //gl_FragColor = vec4(0.0f,0.0f,0.0f,1.0f);
            //}

        }
        //else {
            // Outside maximum light distance
            // Set ambient light
            //gl_FragColor = vec4(0.0f,0.0f,0.0f,1.0f);
        //}
        i += 1;
    }
    vec4 textureColor = texture2D(uTexture, pass_TexCoord);

    // Set ambient level
    if (finalLighting.r < 0.05) finalLighting.r = 0.05f;
    if (finalLighting.g < 0.05) finalLighting.g = 0.05f;
    if (finalLighting.b < 0.05) finalLighting.b = 0.05f;

    gl_FragColor = vec4((finalLighting.r * textureColor.r),
                        (finalLighting.g * textureColor.g),
                        (finalLighting.b * textureColor.b),1.0f);
}