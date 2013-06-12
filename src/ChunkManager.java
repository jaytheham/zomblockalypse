import Shaders.ShaderUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ChunkManager {

    private static ChunkManager instance = null;
    //These must be odd numbers
    private final int CHUNKS_WIDE = 7;
    private final int CHUNKS_HIGH = 5;

    private Chunk[] activeChunks;
    private Player player;
    private Vector3f activeChunksPlayerPos;
    private Vector3f activeChunksCenter;

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

        setupShader("src/Shaders/BlockVertex.glsl", "src/Shaders/BlockFragment.glsl");

        activeChunks = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

        activeChunksPlayerPos = new Vector3f();

        activeChunksPlayerPos.x = (int)Math.floor(player.getX() / Chunk.CHUNK_WIDTH);
        activeChunksPlayerPos.y = (int)Math.floor(player.getY() / Chunk.CHUNK_HEIGHT);
        activeChunksPlayerPos.z = (int)Math.floor(player.getZ() / Chunk.CHUNK_WIDTH);

        activeChunksCenter = new Vector3f(activeChunksPlayerPos);

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
        lightPositions.put(1.5f);
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
        lightColors.put(15.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(150.0f);
        lightColors.put(0.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(15.0f);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(15.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(0.0f);
        lightColors.put(45.0f);
        lightColors.put(0.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(15.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(1.0f);
        lightColors.put(15.0f);
        lightColors.put(0.5f);
        lightColors.put(0.5f);
        lightColors.put(0.5f);
        lightColors.put(15.0f);

        lightColors.flip();

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

        xChange = playerNewChunk[0] - (int)activeChunksCenter.x;
        yChange = playerNewChunk[1] - (int)activeChunksCenter.y;
        zChange = playerNewChunk[2] - (int)activeChunksCenter.z;

        activeChunksPlayerPos.x = playerNewChunk[0];
        activeChunksPlayerPos.y = playerNewChunk[1];
        activeChunksPlayerPos.z = playerNewChunk[2];

        if (xChange < -1 || xChange > 1
                || zChange < -1 || zChange > 1
                || yChange < -1 || yChange > 1) {

            //This is used as a buffer so no need to null any chunks
            Chunk[] activeChunksTempBuffer = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

            activeChunksCenter.x = playerNewChunk[0];
            activeChunksCenter.y = playerNewChunk[1];
            activeChunksCenter.z = playerNewChunk[2];

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
        return this.activeChunks[
                x +
                (y * CHUNKS_WIDE * CHUNKS_WIDE) +
                (z * CHUNKS_WIDE)];
    }

    /**
     * Returns the Chunk that the given world coordinates are inside,
     * if it is in activeChunks
     * @param x X position in world
     * @param y Y position in world
     * @param z Z position in world
     * @return the Chunk that contains position (x,y,z)
     */
    private Chunk getChunkAtWorldCoords(int x, int y, int z) {

        float[] centerPosition = this.activeChunks[
                (this.CHUNKS_WIDE / 2) +
                (this.CHUNKS_HIGH / 2) * CHUNKS_WIDE * CHUNKS_WIDE +
                (this.CHUNKS_WIDE / 2) * CHUNKS_WIDE
                ].getPosition();

        float xDif = x - centerPosition[0];
        float yDif = y - centerPosition[1];
        float zDif = z - centerPosition[2];

        xDif = (float)Math.floor(xDif / Chunk.CHUNK_WIDTH);
        yDif = (float)Math.floor(yDif / Chunk.CHUNK_HEIGHT);
        zDif = (float)Math.floor(zDif / Chunk.CHUNK_WIDTH);

        return this.activeChunks[
                ((this.CHUNKS_WIDE / 2) + (int)xDif) +
                (((this.CHUNKS_HIGH / 2) + (int)yDif) * CHUNKS_WIDE * CHUNKS_WIDE) +
                (((this.CHUNKS_WIDE / 2) + (int)zDif) * CHUNKS_WIDE)];
    }

    /*
     * Return the block at the given world coordinates
     * It should be in activeChunks
     */
    public int getBlock(int x, int y, int z) {
        Chunk c = getChunkAtWorldCoords(x, y, z);

        // Hmmmmmmmmmmmmmmm
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

                    if (getActiveChunk(x, y, z) == null) {

                        activeChunks[
                                 x +
                                (z * CHUNKS_WIDE) +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE)]
                                = this.loadChunk(
                                x - (CHUNKS_WIDE/2),
                                y - (CHUNKS_HIGH/2),
                                z - (CHUNKS_WIDE/2),
                                activeChunksCenter);
                        i++;
                        if (i == maxChunksToLoad)
                            return;
                    }

                }

            }
        }
    }

    private Chunk loadChunk(int x, int y ,int z, Vector3f playerChunk) {
        Chunk newChunk = new Chunk(
                (float)(x + playerChunk.x) * Chunk.CHUNK_WIDTH,
                (float)(y + playerChunk.y) * Chunk.CHUNK_HEIGHT,
                (float)(z + playerChunk.z) * Chunk.CHUNK_WIDTH);

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

        Utils.TextureLoader.loadPNG("res/block_textures.png");
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
