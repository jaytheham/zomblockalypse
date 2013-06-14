import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class Chunk {

    public static final int CHUNK_WIDTH = 32;
    public static final int CHUNK_HEIGHT = 16;

    // Prevent the texture atlas from edge bleeding
    // Cuts off part of the edge pixel
    private static final float uvMin = 0.02f;
    private static final float uvMax = 0.98f;

    private int[] position;
    private int[] blocks;
    private int vbo;
    private int numFloatsInVBO;
    private boolean hasChanged;
    private boolean unsavedChanges;
    private boolean isLoaded;

    /**
     * Looking at the chunk from above
     * @param x The X position of the top left corner, a multiple of CHUNK_WIDTH
     * @param y Multiple of CHUNK_HEIGHT
     * @param z The Z position of the top left corner, a multiple of CHUNK_WIDTH
     */
    public Chunk(int x, int y, int z) {
        blocks = new int[CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH];
        position = new int[] {x, y, z};
        vbo = GL15.glGenBuffers();
        hasChanged = true;
        unsavedChanges = false;
        isLoaded = false;
    }

    public int[] getPosition() {
        return position.clone();
    }

    public void hasBeenLoaded() {
        isLoaded = true;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public int getNumBlocks() {
        return blocks.length;
    }

    /**
     * Get the block at these (chunk relative) coordinates
     * @param x X Position in the block
     * @param y Y Position in the block
     * @param z Z Position in the block
     * @return The int value of the block
     */
    public int getBlock(int x, int y, int z) {
        return blocks[ x +
                      (y * CHUNK_WIDTH * CHUNK_WIDTH) +
                      (z * CHUNK_WIDTH)];
    }

    public int getBlock(int i) {
        return blocks[i];
    }

    public int[] getBlocks() {
        return blocks;
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void hasBeenSaved() {
        unsavedChanges = false;
    }

    public void setBlock(int x, int y, int z, int newValue) {
        blocks[ x +
               (y * CHUNK_WIDTH * CHUNK_WIDTH) +
               (z * CHUNK_WIDTH)] = newValue;
        hasChanged = true;
        unsavedChanges = true;
    }

    public void setBlock(int block, int newValue) {
        blocks[block] = newValue;

        hasChanged = true;
        unsavedChanges = true;
    }

    public void update() {
        hasChanged = false;

        //
        // This potentially isn't big enough
        // Crash Crash Crash!!
        //
        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(
                CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH * 8 * 3);

        int numFloatsAdded = 0;
        int blockVal;

        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_WIDTH; z++) {

                    blockVal = getBlock(x, y, z);
                    if (blockVal == 0) {
                        continue;
                    }

                    /*

                    This should really really be changed to use an indexed array!

                     */

                    //Bottom Tris
                    //If this is bottom of chunk or block below is nothing
                    if (y == 0 || getBlock(x, y-1, z) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-2.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-2.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-2.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-2.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-2.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-2.0f);

                        numFloatsAdded += 42;
                    }

                    //Top Tris
                    //If this is top of chunk or block above is nothing
                    if (y == (CHUNK_HEIGHT - 1) || this.getBlock(x, y+1, z) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(2.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(2.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(2.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(2.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(2.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(2.0f);

                        numFloatsAdded += 42;
                    }

                    //Left Tris
                    //If this is left of chunk or block left is nothing
                    if (x == 0 || this.getBlock(x-1, y, z) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-1.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-1.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-1.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-1.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-1.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-1.0f);

                        numFloatsAdded += 42;
                    }

                    //Right Tris
                    //If this is right of chunk or block right is nothing
                    if (x == (CHUNK_WIDTH - 1) || this.getBlock(x+1, y, z) == 0) {
                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(1.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(1.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(1.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(1.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(1.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(1.0f);

                        numFloatsAdded += 42;
                    }

                    //Back Tris
                    //If this is back of chunk or block behind is nothing
                    if (z == 0 || this.getBlock(x, y, z-1) == 0) {
                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-3.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-3.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-3.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(-3.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-3.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(-3.0f);

                        numFloatsAdded += 42;
                    }

                    //Front Tris
                    //If this is front of chunk or block ahead is nothing
                    if (z == (CHUNK_WIDTH - 1) || this.getBlock(x, y, z+1) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(3.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(3.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(3.0f);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMax);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(3.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMin);
                        vertBuf.put(blockVal);
                        vertBuf.put(3.0f);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(uvMin);
                        vertBuf.put(uvMax);
                        vertBuf.put(blockVal);
                        vertBuf.put(3.0f);

                        numFloatsAdded += 42;
                    }


                }
            }
        }
        numFloatsInVBO = numFloatsAdded;
        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render() {

        if (hasChanged) {
            update();
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 28, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 28, 12);
        GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 28, 20);
        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, 28, 24);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numFloatsInVBO /7);

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

    }
}
