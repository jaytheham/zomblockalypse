import Utils.ShaderUtils;
import Utils.Vector3i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class ChunkManager {

    private static ChunkManager instance = null;
    //These must be odd numbers
    private final int CHUNKS_WIDE = 7;
    private final int CHUNKS_HIGH = 5;

    private Chunk[] activeChunks;
    private Player player;
    private Vector3i activeChunksPlayerPos;
    private Vector3i activeChunksCenter;

    private int programId;
    private int uniformTextureId;
    private int textureId;

    private int uniformMatrixId;

    private int uniformLightPositionsId;
    private int uniformLightColorsId;
    private FloatBuffer lightPositions;
    private FloatBuffer lightColors;



    protected ChunkManager(Player newPlayer) {
        player = newPlayer;

        setupShader("Shaders/BlockVertex.glsl", "Shaders/BlockFragment.glsl");

        activeChunks = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

        activeChunksPlayerPos = new Vector3i();

        activeChunksPlayerPos.x = (int)Math.floor(player.getX() / Chunk.CHUNK_WIDTH);
        activeChunksPlayerPos.y = (int)Math.floor(player.getY() / Chunk.CHUNK_HEIGHT);
        activeChunksPlayerPos.z = (int)Math.floor(player.getZ() / Chunk.CHUNK_WIDTH);

        activeChunksCenter = new Vector3i(activeChunksPlayerPos);

        updateNullChunks(CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH);

        // This shouldn't be here eventually
        lightPositions = BufferUtils.createFloatBuffer(16 * 3);
        lightPositions.put(3.5f);
        lightPositions.put(7.5f);
        lightPositions.put(1.5f);
        lightPositions.put(5.5f);
        lightPositions.put(7.5f);
        lightPositions.put(1.5f);
        lightPositions.put(7.5f);
        lightPositions.put(7.5f);
        lightPositions.put(1.5f);
        lightPositions.put(9.5f);
        lightPositions.put(7.5f);
        lightPositions.put(25.5f);
        lightPositions.put(12.5f);
        lightPositions.put(7.5f);
        lightPositions.put(31.5f);
        lightPositions.put(14.5f);
        lightPositions.put(7.5f);
        lightPositions.put(1.5f);
        lightPositions.put(16.5f);
        lightPositions.put(7.5f);
        lightPositions.put(1.5f);
        lightPositions.put(18.5f);
        lightPositions.put(7.5f);
        lightPositions.put(1.5f);
        lightPositions.flip();
        lightColors = BufferUtils.createFloatBuffer(16 * 4);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(0.0f);
        lightColors.put(25.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(25.0f);
        lightColors.put(0.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(25.0f);
        lightColors.put(1.0f);
        lightColors.put(0.5f);
        lightColors.put(1.0f);
        lightColors.put(200.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(0.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(0.5f);
        lightColors.put(0.5f);
        lightColors.put(0.5f);
        lightColors.put(0.0f);

        lightColors.flip();

    }

    public static ChunkManager getInstance(Player player) {
        if (instance == null) {
            instance = new ChunkManager(player);
        }
        return instance;
    }

    public void update() {

        int xChange, yChange, zChange;

        activeChunksPlayerPos.x = (int)Math.floor(this.player.getX() / Chunk.CHUNK_WIDTH);
        activeChunksPlayerPos.y = (int)Math.floor(this.player.getY() / Chunk.CHUNK_HEIGHT);
        activeChunksPlayerPos.z = (int)Math.floor(this.player.getZ() / Chunk.CHUNK_WIDTH);

        xChange = activeChunksPlayerPos.x - activeChunksCenter.x;
        yChange = activeChunksPlayerPos.y - activeChunksCenter.y;
        zChange = activeChunksPlayerPos.z - activeChunksCenter.z;

        // Only perform a load if player has moved 2 or more chunks
        // to prevent them flip-flopping between chunks and load thrashing
        // It would be nice to have Y change at 1.5 instead of 2
        if (       xChange < -1 || xChange > 1
                || zChange < -1 || zChange > 1
                || yChange < -1 || yChange > 1) {

            //This is used as a buffer so no need to null any chunks
            Chunk[] activeChunksTempBuffer = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

            activeChunksCenter.x = activeChunksPlayerPos.x;
            activeChunksCenter.y = activeChunksPlayerPos.y;
            activeChunksCenter.z = activeChunksPlayerPos.z;

            int xStart = CHUNKS_WIDE - 1;
            int xEnd = -1 + -xChange;
            int xInc = -1;

            if (xChange > 0) {
                xStart = 0;
                xEnd = CHUNKS_WIDE + -xChange;
                xInc = 1;
            }

            int zStart = CHUNKS_WIDE - 1;
            int zEnd = -1 + -zChange;
            int zInc = -1;

            if (zChange > 0) {
                zStart = 0;
                zEnd = CHUNKS_WIDE + -zChange;
                zInc = 1;
            }

            int yStart = CHUNKS_HIGH - 1;
            int yEnd = -1 + -yChange;
            int yInc = -1;

            if (yChange > 0) {
                yStart = 0;
                yEnd = CHUNKS_HIGH + -yChange;
                yInc = 1;
            }

            //This won't handle changes of more than (CHUNKS_WIDE/HIGH - 1)
            for (int y = yStart; y != yEnd; y += yInc) {
                for (int z = zStart; z != zEnd; z += zInc) {
                    for (int x = xStart; x != xEnd; x += xInc) {


                        //save this if it will be lost
                        // Could compress these into: ?
                        // ((xChange != 0) && (x != xStart + xChange))
                        if ((xChange > 0) && (x < xStart + xChange)) {
                            saveChunk(getActiveChunk(x, y, z));
                        }
                        else if ((xChange < 0) && (x > xStart + xChange)) {
                            saveChunk(getActiveChunk(x, y, z));
                        }
                        else if ((zChange > 0) && (z < zStart + zChange)) {
                            saveChunk(getActiveChunk(x, y, z));
                        }
                        else if ((zChange < 0) && (z > zStart + zChange)) {
                            saveChunk(getActiveChunk(x, y, z));
                        }
                        else if ((yChange > 0) && (y < yStart + yChange)) {
                            saveChunk(getActiveChunk(x, y, z));
                        }
                        else if ((yChange < 0) && (y > yStart + yChange)) {
                            saveChunk(getActiveChunk(x, y, z));
                        }

                        //set this to (this + change)
                        activeChunksTempBuffer[
                                 x +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE) +
                                (z * CHUNKS_WIDE)]
                                = activeChunks[
                                          x + xChange +
                                        ((y + yChange) * CHUNKS_WIDE * CHUNKS_WIDE) +
                                        ((z + zChange) * CHUNKS_WIDE)];

                    }
                }
            }

            activeChunks = activeChunksTempBuffer;

        }

        updateNullChunks(1);
    }

    /**
     * Returns the chunk at the given location in this.activeChunks
     * @param x X position in activeChunks
     * @param y Y position in activeChunks
     * @param z Z position in activeChunks
     * @return the Chunk
     */
    private Chunk getActiveChunk(int x, int y, int z) {
        return activeChunks[
                 x +
                (y * CHUNKS_WIDE * CHUNKS_WIDE) +
                (z * CHUNKS_WIDE)];
    }

    /**
     * Returns the Chunk that the given world coordinates are inside.
     * Does NOT check if these coordinates are within activeChunks.
     * @param x X position in world
     * @param y Y position in world
     * @param z Z position in world
     * @return the Chunk that contains position (x,y,z)
     */
    private Chunk getChunkAtWorldCoords(int x, int y, int z) {

        int[] position = activeChunks[0].getPosition();
        // Should be safe to base off chunk 0 as it is always loaded first
        // after any changes to activeChunks.

        position[0] = (x - position[0]) / Chunk.CHUNK_WIDTH;
        position[1] = (y - position[1]) / Chunk.CHUNK_HEIGHT;
        position[2] = (z - position[2]) / Chunk.CHUNK_WIDTH;

        return activeChunks[
                position[0] +
                (position[1] * CHUNKS_WIDE * CHUNKS_WIDE) +
                (position[2] * CHUNKS_WIDE)];
    }

    /**
     * Return the value of the block at the given world coordinates.
     * Does NOT check if these coordinates are within activeChunks.
     * @param x
     * @param y
     * @param z
     * @return the value of the block, or 0 if the chunk is not loaded.
     */
    public int getBlock(int x, int y, int z) {
        Chunk c = getChunkAtWorldCoords(x, y, z);

        if (c == null || !c.isLoaded()){
            return  0;
        }

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

                    if (getActiveChunk(x, y, z) == null) {

                        activeChunks[
                                 x +
                                (z * CHUNKS_WIDE) +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE)]
                                = loadChunk(
                                x - (CHUNKS_WIDE / 2),
                                y - (CHUNKS_HIGH / 2),
                                z - (CHUNKS_WIDE / 2),
                                activeChunksCenter);
                        i++;
                        if (i == maxChunksToLoad)
                            return;
                    }

                }
            }
        }
    }

    private Chunk loadChunk(int x, int y ,int z, Vector3i playerChunk) {
        Chunk newChunk = new Chunk(
                (x + playerChunk.x) * Chunk.CHUNK_WIDTH,
                (y + playerChunk.y) * Chunk.CHUNK_HEIGHT,
                (z + playerChunk.z) * Chunk.CHUNK_WIDTH);

        ChunkLoader loader = new ChunkLoader(newChunk);
        loader.load();

        //for (int i = 0; i < 1024; i++)
        //    newChunk.setBlock(i, 1);
        //newChunk.hasBeenLoaded();

        return newChunk;
    }

    private void saveChunk(Chunk c) {
        if (c.hasUnsavedChanges() && c.isLoaded()) {
            ChunkSaver saver = new ChunkSaver(c);
            saver.save();
        }
    }

    public void saveAllChunks() {
        for (Chunk c : activeChunks)
            saveChunk(c);
    }

    public void setupShader(String vertShader, String fragShader) {
        int vsId = ShaderUtils.loadShader(vertShader, GL20.GL_VERTEX_SHADER);
        int fsId = ShaderUtils.loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vsId);
        GL20.glAttachShader(programId, fsId);

        GL20.glBindAttribLocation(programId, 0, "in_Position");
        GL20.glBindAttribLocation(programId, 1, "in_TexCoord");
        GL20.glBindAttribLocation(programId, 2, "in_BlockType");
        GL20.glBindAttribLocation(programId, 3, "in_VertNormal");

        GL20.glLinkProgram(programId);

        int status = GL20.glGetShaderi(programId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shaders failed to link!");
            System.exit(-1);
        }

        uniformTextureId = GL20.glGetUniformLocation(programId, "uTexture");
        uniformMatrixId = GL20.glGetUniformLocation(programId, "transformMatrix");
        uniformLightPositionsId = GL20.glGetUniformLocation(programId, "uLightPositions");
        uniformLightColorsId = GL20.glGetUniformLocation(programId, "uLightColors");

        GL20.glDetachShader(programId, vsId);
        GL20.glDetachShader(programId, fsId);

        // Block textures
        textureId = GL11.glGenTextures();
        GL13.glActiveTexture(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Utils.TextureLoader.loadPNG("res/block_textures.png", GL11.GL_RGB);


        // Unbind stuff?

    }

    public void render(Matrix4f perspectiveMatrix) {

        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        perspectiveMatrix.store(matrixBuffer);
        matrixBuffer.flip();

        GL20.glUseProgram(programId);

        GL13.glActiveTexture(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL20.glUniform1i(uniformTextureId, 0);

        GL20.glUniformMatrix4(uniformMatrixId, false, matrixBuffer);
        GL20.glUniform3(uniformLightPositionsId, lightPositions);
        GL20.glUniform4(uniformLightColorsId, lightColors);

        for (Chunk c : activeChunks) {
            if (c != null && c.isLoaded())
                c.render();
        }

        GL20.glUseProgram(0);
    }
}
