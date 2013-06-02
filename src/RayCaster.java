import org.lwjgl.util.vector.Vector3f;

public class RayCaster {
    /**
     * Call the callback with (x,y,z,value,face) of all blocks along the line
     * segment from point 'origin' in vector direction 'direction' of length
     * 'radius'. 'radius' may be infinite.
     *
     * 'face' is the normal vector of the face of that block that was entered.
     * It should not be used after the callback returns.
     *
     * If the callback returns a true value, the traversal will be stopped.
     */
    static int[] raycast(Vector3f origin, Vector3f direction, float radius) throws Exception {
        // From "A Fast Voxel Traversal Algorithm for Ray Tracing"
        // by John Amanatides and Andrew Woo, 1987
        // <http://www.cse.yorku.ca/~amana/research/grid.pdf>
        // <http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.42.3443>
        // Extensions to the described algorithm:
        //   • Imposed a distance limit.
        //   • The face passed through to reach the current cube is provided to
        //     the callback.

        // The foundation of this algorithm is a parameterized representation of
        // the provided ray,
        //                    origin + t * direction,
        // except that t is not actually stored; rather, at any given point in the
        // traversal, we keep track of the *greater* t values which we would have
        // if we took a step sufficient to cross a cube boundary along that axis
        // (i.e. change the integer part of the coordinate) in the variables
        // tMaxX, tMaxY, and tMaxZ.

        ChunkManager cm = ChunkManager.getInstance(null);

        // Cube containing origin point.
        int x = (int)Math.floor(origin.x);
        int y = (int)Math.floor(origin.y);
        int z = (int)Math.floor(origin.z);
        // Break out direction vector.
        float dx = direction.x;
        float dy = direction.y;
        float dz = direction.z;
        // Direction to increment x,y,z when stepping.
        int stepX = signum(dx);
        int stepY = signum(dy);
        int stepZ = signum(dz);
        // See description above. The initial values depend on the fractional
        // part of the origin.
        float tMaxX = intbound(origin.x, dx);
        float tMaxY = intbound(origin.y, dy);
        float tMaxZ = intbound(origin.z, dz);
        // The change in t when taking a step (always positive).
        float tDeltaX = stepX/dx;
        float tDeltaY = stepY/dy;
        float tDeltaZ = stepZ/dz;
        // Buffer for reporting faces to the callback.
        int[] face = new int[3];

        // Avoids an infinite loop.
        if (dx == 0 && dy == 0 && dz == 0)
            throw new Exception("Ray cast in 0 direction");

        // Rescale from units of 1 cube-edge to units of 'direction' so we can
        // compare with 't'.
        radius /= Math.sqrt(dx*dx+dy*dy+dz*dz);

        while (true) {

            // If this coordinate is a block return it
            if (cm.getBlock(x, y, z) != 0)
                return new int[] {x, y, z, face[0], face[1], face[2]};

            // tMaxX stores the t-value at which we cross a cube boundary along the
            // X axis, and similarly for Y and Z. Therefore, choosing the least tMax
            // chooses the closest cube boundary. Only the first case of the four
            // has been commented in detail.
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    if (tMaxX > radius) break;
                    // Update which cube we are now in.
                    x += stepX;
                    // Adjust tMaxX to the next X-oriented boundary crossing.
                    tMaxX += tDeltaX;
                    // Record the normal vector of the cube face we entered.
                    face[0] = -stepX;
                    face[1] = 0;
                    face[2] = 0;
                } else {
                    if (tMaxZ > radius) break;
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                    face[0] = 0;
                    face[1] = 0;
                    face[2] = -stepZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    if (tMaxY > radius) break;
                    y += stepY;
                    tMaxY += tDeltaY;
                    face[0] = 0;
                    face[1] = -stepY;
                    face[2] = 0;
                } else {
                    // Identical to the second case, repeated for simplicity in
                    // the conditionals.
                    if (tMaxZ > radius) break;
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                    face[0] = 0;
                    face[1] = 0;
                    face[2] = -stepZ;
                }
            }
        }
        return null;
    }

    static private float intbound(float s, float ds) {
        // Find the smallest positive t such that s+t*ds is an integer.
        if (ds < 0) {
            return intbound(-s, -ds);
        } else {
            s = mod(s, 1);
            // problem is now s+t*ds = 1
            return (1-s)/ds;
        }
    }

    static private int signum(float x) {
        return x > 0 ? 1 : x < 0 ? -1 : 0;
    }

    private static float mod(float value, float modulus) {
        return (value % modulus + modulus) % modulus;
    }
}
