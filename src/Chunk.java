import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class Chunk {

    public static final int CHUNK_WIDTH = 32;
    public static final int CHUNK_HEIGHT = 16;

    private float[] position;
    private int[] blocks;
    private int vbo;
    private int elements;
    private boolean hasChanged;
    private boolean unsavedChanges;
    private boolean isLoaded;

    public Chunk(float x, float y, float z) {
        blocks = new int[CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH];
        position = new float[] {x, y, z};
        vbo = GL15.glGenBuffers();
        hasChanged = true;
        unsavedChanges = false;
        isLoaded = false;
    }

    public float[] getPosition() {
        return position;
    }

    public void setIsLoaded() {
        isLoaded = true;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public int getNumBlocks() {
        return blocks.length;
    }

    public int getBlock(int x, int y, int z) {
        return blocks[   x +
                        (y * CHUNK_WIDTH * CHUNK_WIDTH) +
                        (z * CHUNK_WIDTH)];
    }

    public int[] getBlocks() {
        return blocks;
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    public void wasSaved() {
        unsavedChanges = false;
    }

    public void setBlock(int x, int y, int z, int newValue) {
        blocks[x +
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

        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(
                CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_WIDTH * 8 * 3);
        int numFloatsAdded = 0;

        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int y = 0; y < CHUNK_HEIGHT; y++) {
                for (int z = 0; z < CHUNK_WIDTH; z++) {
                    if (this.getBlock(x, y, z) == 0) {
                        continue;
                    }

                    //Bottom Tris
                    //If this is bottom of chunk or block below is nothing
                    if (y - 1 < 0 || this.getBlock(x, y-1, z) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        numFloatsAdded += 18;
                    }

                    //Top Tris
                    //If this is top of chunk or block above is nothing
                    if (y + 1 > 15 || this.getBlock(x, y+1, z) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        numFloatsAdded += 18;
                    }

                    //Left Tris
                    //If this is left of chunk or block left is nothing
                    if (x - 1 < 0 || this.getBlock(x-1, y, z) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        numFloatsAdded += 18;
                    }

                    //Right Tris
                    //If this is right of chunk or block right is nothing
                    if (x + 1 > 31 || this.getBlock(x+1, y, z) == 0) {
                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        numFloatsAdded += 18;
                    }

                    //Back Tris
                    //If this is back of chunk or block behind is nothing
                    if (z - 1 < 0 || this.getBlock(x, y, z-1) == 0) {
                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z);

                        numFloatsAdded += 18;
                    }

                    //Front Tris
                    //If this is front of chunk or block ahead is nothing
                    if (z + 1 > 31 || this.getBlock(x, y, z+1) == 0) {
                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x + 1);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y + 1);
                        vertBuf.put(this.position[2] + z + 1);

                        vertBuf.put(this.position[0] + x);
                        vertBuf.put(this.position[1] + y);
                        vertBuf.put(this.position[2] + z + 1);

                        numFloatsAdded += 18;
                    }


                }
            }
        }
        this.elements = numFloatsAdded;
        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render() {

        if (hasChanged) {
            this.update();
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.elements/3);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);


    }
}
