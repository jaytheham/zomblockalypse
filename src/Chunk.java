import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class Chunk {

    public static final int CHUNK_WIDTH = 40;
    public static final int CHUNK_HEIGHT = 20;

    // Prevent the texture atlas from edge bleeding
    // Cuts off part of the edge pixel
    private static final float uvMin = 0.02f;
    private static final float uvMax = 0.98f;

    private int[] position;
    private int[] blocks;
    private int verticesVbo;
    private int indicesVbo;
    private int numIndicesInVBO;
    private boolean hasChanged;
    private boolean unsavedChanges;
    private boolean isLoaded;

    private ArrayList<GameObject> objectList;

    /**
     * Looking at the chunk from above
     * @param x The X position of the top left corner, a multiple of CHUNK_WIDTH
     * @param y Multiple of CHUNK_HEIGHT
     * @param z The Z position of the top left corner, a multiple of CHUNK_WIDTH
     */
    public Chunk(int x, int y, int z) {
        blocks = new int[CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH];
        position = new int[] {x, y, z};
        verticesVbo = GL15.glGenBuffers();
        indicesVbo = GL15.glGenBuffers();
        hasChanged = true;
        unsavedChanges = false;
        isLoaded = false;
        objectList = new ArrayList<GameObject>();
    }

    public void cleanUp() {
        GL15.glDeleteBuffers(verticesVbo);
        GL15.glDeleteBuffers(indicesVbo);
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

    public void invalidate() {
        hasChanged = true;
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

    private void update(ChunkManager chunkBaron) {
        hasChanged = false;

        FloatBuffer bufVertices = BufferUtils.createFloatBuffer(
                (CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH) / 2 * 5 * 4 * 5);
        IntBuffer indicesBuf = BufferUtils.createIntBuffer(
                (CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH) / 2 * 5 * 6);

        int indiceNum = 0;
        int blockVal;

        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_WIDTH; z++) {

                    blockVal = getBlock(x, y, z);
                    if (blockVal == 0) {
                        continue;
                    }

                    /*
                    This should probably be packed into non interleaved data of smaller types
                    Why when I tried to use shorts did it go wonky?
                     */

                    //
                    // Try making the offset 0.999999 instead of 1 so tex coords can be
                    // calculated in the vertex shader
                    // = Some sparklies between edges

                    /*// Bottom tris should NEVER be seen with gamecam
                    //Bottom Tris
                    //If this is bottom of chunk or block below is nothing
                    if (y == 0 || getBlock(x, y-1, z) == 0) {
                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-2.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-2.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(-2.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(-2.0f);

                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum - 3);
                        indiceNum++;

                        numFloatsAdded += 20;
                    } */


                    //Top Tris
                    //If this is top of chunk or block above is nothing
                    if (y == (CHUNK_HEIGHT - 1) && chunkBaron.getBlock(position[0] + x, position[1] + y+1, position[2] + z) == 0
                            || y < CHUNK_HEIGHT - 1 && getBlock(x, y+1, z) == 0) {
                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(2.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(2.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(2.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(2.0f);

                        indicesBuf.put(indiceNum); // 0
                        indiceNum++;
                        indicesBuf.put(indiceNum); // 1
                        indiceNum++;
                        indicesBuf.put(indiceNum); // 2
                        indicesBuf.put(indiceNum - 1); // 1
                        indicesBuf.put(indiceNum - 2); // 0
                        indiceNum++;
                        indicesBuf.put(indiceNum); // 3
                        indiceNum++;
                    }

                    //Left Tris
                    //If this is left of chunk or block left is nothing
                    if (x == 0 && chunkBaron.getBlock(position[0] + x - 1, position[1] + y, position[2] + z) == 0 ||
                            x > 0 && this.getBlock(x-1, y, z) == 0) {
                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-1.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(-1.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(-1.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-1.0f);

                        indicesBuf.put(indiceNum); // 0
                        indiceNum++;
                        indicesBuf.put(indiceNum); // 1
                        indiceNum++;
                        indicesBuf.put(indiceNum); // 2
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum - 3);
                        indiceNum++;
                    }

                    //Right Tris
                    //If this is right of chunk or block right is nothing
                    if (x == (CHUNK_WIDTH - 1) && chunkBaron.getBlock(position[0] + x + 1, position[1] + y, position[2] + z) == 0
                            || x < CHUNK_WIDTH - 1 && this.getBlock(x+1, y, z) == 0) {
                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(1.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(1.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(1.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(1.0f);

                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum - 3);
                        indiceNum++;
                    }

                    //Back Tris
                    //If this is back of chunk or block behind is nothing
                    if (z == 0  && chunkBaron.getBlock(position[0] + x, position[1] + y, position[2] + z - 1) == 0 ||
                            z > 0 &&  this.getBlock(x, y, z-1) == 0) {
                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-3.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-3.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-3.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z);

                        bufVertices.put(blockVal);
                        bufVertices.put(-3.0f);

                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum - 3);
                        indiceNum++;
                    }

                    //Front Tris
                    //If this is front of chunk or block ahead is nothing
                    if (z == (CHUNK_WIDTH - 1) && chunkBaron.getBlock(position[0] + x, position[1] + y, position[2] + z + 1) == 0 ||
                            z < CHUNK_WIDTH - 1 &&  this.getBlock(x, y, z+1) == 0) {
                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(3.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(3.0f);

                        bufVertices.put((float)this.position[0] + x + 1.0f);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(3.0f);

                        bufVertices.put((float)this.position[0] + x);
                        bufVertices.put((float)this.position[1] + y + 1.0f);
                        bufVertices.put((float)this.position[2] + z + 1.0f);

                        bufVertices.put(blockVal);
                        bufVertices.put(3.0f);

                        indicesBuf.put(indiceNum); // 0
                        indiceNum++;
                        indicesBuf.put(indiceNum); // 1
                        indiceNum++;
                        indicesBuf.put(indiceNum); // 2
                        indicesBuf.put(indiceNum);
                        indiceNum++;
                        indicesBuf.put(indiceNum);
                        indicesBuf.put(indiceNum - 3);
                        indiceNum++;
                    }


                }
            }
        }
        numIndicesInVBO = indicesBuf.position();
        indicesBuf.flip();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        bufVertices.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, verticesVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufVertices, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render(ChunkManager cm) {

        if (hasChanged) {
            update(cm);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, verticesVbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 20, 0);
        GL20.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 20, 12);
        GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 20, 16);

        //GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numFloatsInVBO /7);
        GL11.glDrawElements(GL11.GL_TRIANGLES, numIndicesInVBO, GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

    }
}
