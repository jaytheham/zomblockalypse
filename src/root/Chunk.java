package root;

import root.Entities.GameEntity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
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
    private boolean objectsAreLoaded;

    private ArrayList<GameEntity> objectList;

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
        objectList = new ArrayList<GameEntity>();
        objectsAreLoaded = false;
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

    public void loadedObjects() {
        objectsAreLoaded = true;
    }

    public void attachGameObject(GameEntity o) {
        objectList.add(o);
    }

    public ArrayList<GameEntity> getGameObjects() {
        if (objectsAreLoaded)
            return objectList;
        else
            return null;
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

    /**
     * Get whether a block is opaque.
     * Will need to change? when transparent blocks exist
     * @param x X Position in the block
     * @param y Y Position in the block
     * @param z Z Position in the block
     * @return 1 if a block is opaque, 0 if not
     */
    private int isBlockOpaque(int x, int y, int z) {
        if (x >= 0 && x < CHUNK_WIDTH &&
                z >= 0 && z < CHUNK_WIDTH &&
                y >= 0 && y < CHUNK_HEIGHT &&
                blocks[ x +
                (y * CHUNK_WIDTH * CHUNK_WIDTH) +
                (z * CHUNK_WIDTH)] > 0) return 2;
        return 0;
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

        ShortBuffer bufVertices = BufferUtils.createShortBuffer(
                (CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH) / 2 * 5 * 4 * 6);
        IntBuffer indicesBuf = BufferUtils.createIntBuffer(
                (CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH) / 2 * 5 * 6);

        int indiceNum = 0;
        int blockVal;

        int pN, nX, pX, nZ, pZ, pXpZ, pXnZ, nXpZ, nXnZ;

        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_WIDTH; z++) {

                    blockVal = getBlock(x, y, z);
                    if (blockVal == 0) {
                        continue;
                    }

                    // Now that data is packed smaller, removing indices and having
                    // duplicate verts would be the same size... also faster?

                    // Try making the offset 0.999999 instead of 1 so tex coords can be
                    // calculated in the vertex shader
                    // = Some sparklies between edges

                    /*// Bottom tris should NEVER be seen with gamecam
                    //Bottom Tris
                    //If this is bottom of chunk or block below is nothing
                    if (y == 0 || getBlock(x, y-1, z) == 0) {
                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(this.position[2] + z));

                        bufVertices.put((short)blockVal);
                        bufVertices.put(-2.0f);

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(this.position[2] + z));

                        bufVertices.put((short)blockVal);
                        bufVertices.put(-2.0f);

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z + 1.0f));

                        bufVertices.put((short)blockVal);
                        bufVertices.put(-2.0f);

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z + 1.0f));

                        bufVertices.put((short)blockVal);
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
                    if (y < CHUNK_HEIGHT - 1 && getBlock(x, y+1, z) == 0
                        || y == (CHUNK_HEIGHT - 1) && chunkBaron.getBlock(position[0] + x,
                                                                          position[1] + y+1,
                                                                          position[2] + z) == 0) {
                        pX = isBlockOpaque(x + 1, y + 1, z);
                        pZ = isBlockOpaque(x, y + 1, z + 1);
                        nX = isBlockOpaque(x - 1, y + 1, z);
                        nZ = isBlockOpaque(x, y + 1, z - 1);
                        pXpZ = isBlockOpaque(x + 1, y + 1, z + 1);
                        pXnZ = isBlockOpaque(x + 1, y + 1, z - 1);
                        nXpZ = isBlockOpaque(x - 1, y + 1, z + 1);
                        nXnZ = isBlockOpaque(x - 1, y + 1, z - 1);

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(2 + 32));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nX + pZ, nXpZ));

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(2 + 16));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + pX, pXnZ));

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)2);
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + nX, nXnZ));

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(2 + 48));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + pX, pXpZ));

                        if (Math.abs(Math.max(nZ + pX, pXnZ) - Math.max(nX + pZ, nXpZ))
                                < Math.abs(Math.max(pZ + pX, pXpZ) - Math.max(nZ + nX, nXnZ))) {
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
                        else {
                            indicesBuf.put(indiceNum);
                            indicesBuf.put(indiceNum + 3);
                            indicesBuf.put(indiceNum + 2);
                            indiceNum++;
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum++);
                        }
                    }

                    //Left Tris
                    //If this is left of chunk or block left is nothing
                    if (x > 0 && this.getBlock(x-1, y, z) == 0
                        || x == 0 && chunkBaron.getBlock(position[0] + x - 1,
                                                         position[1] + y,
                                                         position[2] + z) == 0) {
                        pX = isBlockOpaque(x - 1, y + 1, z);
                        pZ = isBlockOpaque(x - 1, y, z + 1);
                        nX = isBlockOpaque(x - 1, y - 1, z);
                        nZ = isBlockOpaque(x - 1, y, z - 1);
                        pXpZ = isBlockOpaque(x - 1, y + 1, z + 1);
                        pXnZ = isBlockOpaque(x - 1, y + 1, z - 1);
                        nXpZ = isBlockOpaque(x - 1, y - 1, z + 1);
                        nXnZ = isBlockOpaque(x - 1, y - 1, z - 1);

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(9 + 32));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nX + nZ, nXnZ));

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(9 + 48));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + nX, nXpZ));

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(9 + 16));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + pX, pXpZ));

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(9));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + pX, pXnZ));

                        if (Math.abs(Math.max(nX + pZ, nXpZ) - Math.max(nZ + pX, pXnZ))
                                > Math.abs(Math.max(pZ + pX, pXpZ) - Math.max(nZ + nX, nXnZ))) {
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
                        else {
                            indiceNum++;
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum);

                            indicesBuf.put(indiceNum - 3);
                            indicesBuf.put(indiceNum - 2);
                            indicesBuf.put(indiceNum);
                            indiceNum++;
                        }
                    }

                    //Right Tris
                    //If this is right of chunk or block right is nothing
                    if (x < CHUNK_WIDTH - 1 && this.getBlock(x+1, y, z) == 0
                        || x == (CHUNK_WIDTH - 1) && chunkBaron.getBlock(position[0] + x + 1,
                                                                         position[1] + y,
                                                                         position[2] + z) == 0) {
                        pX = isBlockOpaque(x + 1, y + 1, z);
                        pZ = isBlockOpaque(x + 1, y, z + 1);
                        nX = isBlockOpaque(x + 1, y - 1, z);
                        nZ = isBlockOpaque(x + 1, y, z - 1);
                        pXpZ = isBlockOpaque(x + 1, y + 1, z + 1);
                        pXnZ = isBlockOpaque(x + 1, y + 1, z - 1);
                        nXpZ = isBlockOpaque(x + 1, y - 1, z + 1);
                        nXnZ = isBlockOpaque(x + 1, y - 1, z - 1);

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(1));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + pX, pXpZ));
                        
                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(1 + 32));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + nX, nXpZ));

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(1 + 48));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nX + nZ, nXnZ));

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(1 + 16));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + pX, pXnZ));

                        if (Math.abs(Math.max(nX + pZ, nXpZ) - Math.max(nZ + pX, pXnZ))
                                > Math.abs(Math.max(pZ + pX, pXpZ) - Math.max(nZ + nX, nXnZ))) {
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
                        else {
                            indiceNum++;
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum);

                            indicesBuf.put(indiceNum - 3);
                            indicesBuf.put(indiceNum - 2);
                            indicesBuf.put(indiceNum);
                            indiceNum++;
                        }
                    }

                    //Back Tris
                    //If this is back of chunk or block behind is nothing
                    if (z > 0 &&  this.getBlock(x, y, z-1) == 0
                        || z == 0  && chunkBaron.getBlock(position[0] + x,
                                                          position[1] + y,
                                                          position[2] + z - 1) == 0) {

                        pX = isBlockOpaque(x + 1, y, z - 1);
                        pZ = isBlockOpaque(x, y + 1, z - 1);
                        nX = isBlockOpaque(x - 1, y, z - 1);
                        nZ = isBlockOpaque(x, y - 1, z - 1);
                        pXpZ = isBlockOpaque(x + 1, y + 1, z - 1);
                        pXnZ = isBlockOpaque(x + 1, y - 1, z - 1);
                        nXpZ = isBlockOpaque(x - 1, y + 1, z - 1);
                        nXnZ = isBlockOpaque(x - 1, y - 1, z - 1);

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(12));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + pX, pXpZ));

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(12 + 32));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + pX, pXnZ));

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(12 + 48));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + nX, nXnZ));

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z));
                        bufVertices.put((short)(12 + 16));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + nX, nXpZ));

                        if (Math.abs(Math.max(nX + pZ, nXpZ) - Math.max(nZ + pX, pXnZ))
                                > Math.abs(Math.max(pZ + pX, pXpZ) - Math.max(nZ + nX, nXnZ))) {
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
                        else {
                            indiceNum++;
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum);

                            indicesBuf.put(indiceNum - 2);
                            indicesBuf.put(indiceNum);
                            indicesBuf.put(indiceNum - 3);
                            indiceNum++;
                        }
                    }

                    //Front Tris
                    //If this is front of chunk or block ahead is nothing
                    if (z < CHUNK_WIDTH - 1 &&  this.getBlock(x, y, z+1) == 0
                        || z == (CHUNK_WIDTH - 1) && chunkBaron.getBlock(position[0] + x,
                                                                         position[1] + y,
                                                                         position[2] + z + 1) == 0) {
                        pX = isBlockOpaque(x + 1, y, z + 1);
                        pZ = isBlockOpaque(x, y + 1, z + 1);
                        nX = isBlockOpaque(x - 1, y, z + 1);
                        nZ = isBlockOpaque(x, y - 1, z + 1);
                        pXpZ = isBlockOpaque(x + 1, y + 1, z + 1);
                        pXnZ = isBlockOpaque(x + 1, y - 1, z + 1);
                        nXpZ = isBlockOpaque(x - 1, y + 1, z + 1);
                        nXnZ = isBlockOpaque(x - 1, y - 1, z + 1);

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(4 + 32));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + nX, nXnZ));

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(4 + 48));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(nZ + pX, pXnZ));

                        bufVertices.put((short)(position[0] + x + 1.0f));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(4 + 16));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + pX, pXpZ));

                        bufVertices.put((short)(position[0] + x));
                        bufVertices.put((short)(position[1] + y + 1.0f));
                        bufVertices.put((short)(position[2] + z + 1.0f));
                        bufVertices.put((short)(4));
                        bufVertices.put((short)blockVal);
                        bufVertices.put((short)Math.max(pZ + nX, nXpZ));


                        if (Math.abs(Math.max(nX + pZ, nXpZ) - Math.max(nZ + pX, pXnZ))
                                > Math.abs(Math.max(pZ + pX, pXpZ) - Math.max(nZ + nX, nXnZ))) {
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
                        else {
                            indiceNum++;
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum++);
                            indicesBuf.put(indiceNum);

                            indicesBuf.put(indiceNum - 2);
                            indicesBuf.put(indiceNum);
                            indicesBuf.put(indiceNum - 3);
                            indiceNum++;
                        }
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

    public void render(ChunkManager cm, int inPosition, int inBlockData) {

        if (hasChanged) {
            update(cm);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, verticesVbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVbo);
        GL20.glEnableVertexAttribArray(inPosition);
        GL20.glEnableVertexAttribArray(inBlockData);

        GL20.glVertexAttribPointer(inPosition, 4, GL11.GL_SHORT, false, 12, 0);
        GL20.glVertexAttribPointer(inBlockData, 2, GL11.GL_SHORT, false, 12, 8);

        GL11.glDrawElements(GL11.GL_TRIANGLES, numIndicesInVBO, GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(inBlockData);
        GL20.glDisableVertexAttribArray(inPosition);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

    }
}
