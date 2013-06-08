import Utils.Constants;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class ChunkManager {

    private static ChunkManager instance = null;
    //These must be odd numbers
    private final int CHUNKS_WIDE = 7;
    private final int CHUNKS_HIGH = 5;

    private Chunk[] loadedChunks;
    private Chunk[] loadedChunksBackBuffer;
    private Player player;
    private int[] playerChunk;
    private int[] centerChunk;

    private Texture texture;

    protected ChunkManager(Player newPlayer) {
        this.player = newPlayer;
        texture = new Texture(Constants.BLOCK_TEXTURES_FILE_PATH);

        loadedChunks = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

        playerChunk = new int[3];

        playerChunk[0] = (int)Math.floor(this.player.getX() / Chunk.CHUNK_WIDTH);
        playerChunk[1] = (int)Math.floor(this.player.getY() / Chunk.CHUNK_HEIGHT);
        playerChunk[2] = (int)Math.floor(this.player.getZ() / Chunk.CHUNK_WIDTH);

        centerChunk = (playerChunk).clone();

        updateNullChunks(CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH);
    }

    public static ChunkManager getInstance(Player player) {
        if (instance == null) {
            instance = new ChunkManager(player);
        }
        return instance;
    }

    public void update() {
        int[] playerNewChunk = new int[3];
        int xChange, yChange, zChange;


        playerNewChunk[0] = (int)Math.floor(this.player.getX() / Chunk.CHUNK_WIDTH);
        playerNewChunk[1] = (int)Math.floor(this.player.getY() / Chunk.CHUNK_HEIGHT);
        playerNewChunk[2] = (int)Math.floor(this.player.getZ() / Chunk.CHUNK_WIDTH);

        xChange = playerNewChunk[0] - this.centerChunk[0];
        yChange = playerNewChunk[1] - this.centerChunk[1];
        zChange = playerNewChunk[2] - this.centerChunk[2];

        this.playerChunk[0] = playerNewChunk[0];
        this.playerChunk[1] = playerNewChunk[1];
        this.playerChunk[2] = playerNewChunk[2];

        if (xChange < -1 || xChange > 1
                || zChange < -1 || zChange > 1
                || yChange < -1 || yChange > 1) {

            //This is used as a buffer so no need to null any chunks
            loadedChunksBackBuffer = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

            this.centerChunk[0] = playerNewChunk[0];
            this.centerChunk[1] = playerNewChunk[1];
            this.centerChunk[2] = playerNewChunk[2];

            int xStart = CHUNKS_WIDE - 1;
            int xEnd = -1 + (-1 * xChange);
            int xInc = -1;

            if (xChange > 0) {
                xStart = 0;
                xEnd = CHUNKS_WIDE + (-1 * xChange);
                xInc = 1;
            }

            int zStart = CHUNKS_WIDE - 1;
            int zEnd = -1 + (-1 * zChange);
            int zInc = -1;

            if (zChange > 0) {
                zStart = 0;
                zEnd = CHUNKS_WIDE + (-1 * zChange);
                zInc = 1;
            }

            int yStart = CHUNKS_HIGH - 1;
            int yEnd = -1 + (-1 * yChange);
            int yInc = -1;

            if (yChange > 0) {
                yStart = 0;
                yEnd = CHUNKS_HIGH + (-1 * yChange);
                yInc = 1;
            }

            //This won't handle changes of more than (CHUNKS_WIDE/HIGH - 1)
            for (int y = yStart; y != yEnd; y += yInc) {
                for (int z = zStart; z != zEnd; z += zInc) {
                    for (int x = xStart; x != xEnd; x += xInc) {


                        //save this if it will be lost
                        if ((xChange > 0) && (x < xStart + xChange)) {
                            saveChunk(getChunkLoadedCoords(x, y, z));
                        }
                        else if ((xChange < 0) && (x > xStart + xChange)) {
                            saveChunk(getChunkLoadedCoords(x, y, z));
                        }
                        else if ((zChange > 0) && (z < zStart + zChange)) {
                            saveChunk(getChunkLoadedCoords(x, y, z));
                        }
                        else if ((zChange < 0) && (z > zStart + zChange)) {
                            saveChunk(getChunkLoadedCoords(x, y, z));
                        }
                        else if ((yChange > 0) && (y < yStart + yChange)) {
                            saveChunk(getChunkLoadedCoords(x, y, z));
                        }
                        else if ((yChange < 0) && (y > yStart + yChange)) {
                            saveChunk(getChunkLoadedCoords(x, y, z));
                        }

                        //set this to (this + change)
                        loadedChunksBackBuffer[x +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE) +
                                (z * CHUNKS_WIDE)]
                                =
                                loadedChunks[x + xChange +
                                        ((y + yChange) * CHUNKS_WIDE * CHUNKS_WIDE) +
                                        ((z + zChange) * CHUNKS_WIDE)];

                    }
                }
            }

            loadedChunks = loadedChunksBackBuffer;
            loadedChunksBackBuffer = null;
        }

        updateNullChunks(1);
    }

    /**
     * Returns the chunk at the given location in this.loadedChunks
     * @param x X position in loadedChunks
     * @param y Y position in loadedChunks
     * @param z Z position in loadedChunks
     * @return the Chunk
     */
    private Chunk getChunkLoadedCoords(int x, int y, int z) {
        return this.loadedChunks[
                x +
                (y * CHUNKS_WIDE * CHUNKS_WIDE) +
                (z * CHUNKS_WIDE)];
    }

    /**
     * Returns the Chunk that the given world coordinates are inside,
     * if it is in loadedChunks
     * @param x X position in world
     * @param y Y position in world
     * @param z Z position in world
     * @return the Chunk that contains position (x,y,z)
     */
    private Chunk getChunkAtWorldCoords(int x, int y, int z) {
        Chunk center = this.loadedChunks[
                (this.CHUNKS_WIDE / 2) +
                (this.CHUNKS_HIGH / 2) * CHUNKS_WIDE * CHUNKS_WIDE +
                (this.CHUNKS_WIDE / 2) * CHUNKS_WIDE];

        float[] centerPosition = center.getPosition();

        float xDif = x - centerPosition[0];
        float yDif = y - centerPosition[1];
        float zDif = z - centerPosition[2];

        xDif = (float)Math.floor(xDif / Chunk.CHUNK_WIDTH);
        yDif = (float)Math.floor(yDif / Chunk.CHUNK_HEIGHT);
        zDif = (float)Math.floor(zDif / Chunk.CHUNK_WIDTH);

        return this.loadedChunks[
                ((this.CHUNKS_WIDE / 2) + (int)xDif) +
                (((this.CHUNKS_HIGH / 2) + (int)yDif) * CHUNKS_WIDE * CHUNKS_WIDE) +
                (((this.CHUNKS_WIDE / 2) + (int)zDif) * CHUNKS_WIDE)];
    }

    /*
     * Return the block at the given world coordinates
     * It should be in loadedChunks
     */
    public int getBlock(int x, int y, int z) {
        Chunk c = getChunkAtWorldCoords(x, y, z);

        x %= Chunk.CHUNK_WIDTH;
        y %= Chunk.CHUNK_HEIGHT;
        z %= Chunk.CHUNK_WIDTH;

        if (x < 0)
            x = Chunk.CHUNK_WIDTH + x;
        if (y < 0)
            y = Chunk.CHUNK_HEIGHT + y;
        if (z < 0)
            z = Chunk.CHUNK_WIDTH + z;

        return c.getBlock(x, y, z);
    }

    public int getBlock(Vector3f p) {
        return getBlock((int)Math.floor(p.x), (int)Math.floor(p.y), (int)Math.floor(p.z));
    }

    public int getBlock(float x, float y, float z) {
        return getBlock((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
    }

    public void setBlock(Vector3f p, int newBlockVal) {
        if (p == null)
            return;
        this.setBlock((int)Math.floor(p.x), (int)Math.floor(p.y), (int)Math.floor(p.z), newBlockVal);
    }

    public void setBlock(int[] p, int newBlockVal) {
        this.setBlock(p[0], p[1], p[2], newBlockVal);
    }

    public void setBlock(int x, int y, int z, int newBlockVal) {

        Chunk c = getChunkAtWorldCoords(x, y, z);

        x %= Chunk.CHUNK_WIDTH;
        y %= Chunk.CHUNK_HEIGHT;
        z %= Chunk.CHUNK_WIDTH;

        // Could remove if statements here by doing the adding first, then modding??
        if (x < 0)
            x = Chunk.CHUNK_WIDTH + x;
        if (y < 0)
            y = Chunk.CHUNK_HEIGHT + y;
        if (z < 0)
            z = Chunk.CHUNK_WIDTH + z;

        c.setBlock(x, y, z, newBlockVal);
    }

    public void deleteBlock(int x, int y, int z) {
        setBlock(x, y, z, 0);
    }

    public void deleteBlock(int[] pos) {
        setBlock(pos[0], pos[1], pos[2], 0);
    }

    private void updateNullChunks(int maxChunksToLoad) {

        int i = 0;
        for (int x = 0; x < CHUNKS_WIDE; x++) {
            for (int z = 0; z < CHUNKS_WIDE; z++) {
                for (int y = 0; y < CHUNKS_HIGH; y++) {

                    if (this.getChunkLoadedCoords(x, y, z) == null) {

                        loadedChunks[
                                 x +
                                (z * CHUNKS_WIDE) +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE)]
                                = this.loadChunk(
                                x - (CHUNKS_WIDE/2),
                                y - (CHUNKS_HIGH/2),
                                z - (CHUNKS_WIDE/2),
                                this.playerChunk);
                        i++;
                        if (i == maxChunksToLoad)
                            break;
                    }

                }

            }
        }
    }

    private Chunk loadChunk(int x, int y ,int z, int[] playerChunk) {
        Chunk newChunk = new Chunk(
                (float)(x + playerChunk[0]) * Chunk.CHUNK_WIDTH,
                (float)(y + playerChunk[1]) * Chunk.CHUNK_HEIGHT,
                (float)(z + playerChunk[2]) * Chunk.CHUNK_WIDTH);

        ChunkLoader loader = new ChunkLoader(newChunk);
        loader.load();

        //for (int i = 0; i < 1024; i++)
        //    newChunk.setBlock(i, 1);
        //newChunk.setIsLoaded();

        return newChunk;
    }

    private void saveChunk(Chunk c) {
        if (c.hasUnsavedChanges() && c.isLoaded()) {
            ChunkSaver saver = new ChunkSaver(c);
            saver.save();
        }
    }

    public void saveAllChunks() {
        for (Chunk c : loadedChunks)
            saveChunk(c);
    }

    public void render(int programId, int uniformMatrixId, Matrix4f perspectiveMatrix) {

        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        perspectiveMatrix.store(matrixBuffer);
        matrixBuffer.flip();

        GL20.glUseProgram(programId);

        GL20.glUniformMatrix4(uniformMatrixId, false, matrixBuffer);

        for (Chunk c : loadedChunks) {
            if (c != null && c.isLoaded())
                c.render();
        }

        GL20.glUseProgram(0);
    }
}
