import org.lwjgl.util.vector.Matrix4f;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChunkManager {
    private final int CHUNKS_WIDE = 7;
    private final int CHUNKS_HIGH = 3;
    private Chunk[] loadedChunks;
    private Player player;
    private int[] playerChunk;

    public ChunkManager(Player newPlayer) {
        this.player = newPlayer;
        loadedChunks = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

        playerChunk = new int[3];

        playerChunk[0] = (int)Math.floor(this.player.getX() / Chunk.CHUNK_WIDTH);
        playerChunk[1] = (int)Math.floor(this.player.getY() / Chunk.CHUNK_HEIGHT);
        playerChunk[2] = (int)Math.floor(this.player.getZ() / Chunk.CHUNK_WIDTH);

        updateNullChunks(CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH);
    }

    public void update() {
        int[] playerNewChunk = new int[3];
        int xChange, yChange, zChange;

        playerNewChunk[0] = (int)Math.floor(this.player.getX() / Chunk.CHUNK_WIDTH);
        playerNewChunk[1] = (int)Math.floor(this.player.getY() / Chunk.CHUNK_HEIGHT);
        playerNewChunk[2] = (int)Math.floor(this.player.getZ() / Chunk.CHUNK_WIDTH);

        xChange = playerNewChunk[0] - this.playerChunk[0];
        yChange = playerNewChunk[1] - this.playerChunk[1];
        zChange = playerNewChunk[2] - this.playerChunk[2];

        this.playerChunk[0] = playerNewChunk[0];
        this.playerChunk[1] = playerNewChunk[1];
        this.playerChunk[2] = playerNewChunk[2];

        if (xChange != 0 || zChange != 0 || yChange != 0) {

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

                        ChunkSaver saver = new ChunkSaver(loadedChunks[x +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE) +
                                (z * CHUNKS_WIDE)]);
                        //save this if it will be lost
                        //This repeats some saves if change in more than one direction?
                        if ((xChange > 0) && (x < xStart + xChange)) {
                            saver.save();
                        }
                        else if ((xChange < 0) && (x > xStart + xChange)) {
                            saver.save();
                        }
                        if ((zChange > 0) && (z < zStart + zChange)) {
                            saver.save();
                        }
                        else if ((zChange < 0) && (z > zStart + zChange)) {
                            saver.save();
                        }
                        if ((yChange > 0) && (y < yStart + yChange)) {
                            saver.save();
                        }
                        else if ((yChange < 0) && (y > yStart + yChange)) {
                            saver.save();
                        }

                        //set this to (this + change)
                        loadedChunks[x +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE) +
                                (z * CHUNKS_WIDE)]
                                =
                                loadedChunks[x + xChange +
                                        ((y + yChange) * CHUNKS_WIDE * CHUNKS_WIDE) +
                                        ((z + zChange) * CHUNKS_WIDE)];
                        //set (this + change) to null
                        loadedChunks[x + xChange +
                                ((y + yChange) * CHUNKS_WIDE * CHUNKS_WIDE) +
                                ((z + zChange) * CHUNKS_WIDE)]
                                = null;
                    }
                }
            }


        }
        updateNullChunks(1);
    }

    private void updateNullChunks(int maxChunksToLoad) {

        int i = 0;
        for (int x = 0; x < CHUNKS_WIDE; x++) {
            for (int z = 0; z < CHUNKS_WIDE; z++) {
                for (int y = 0; y < CHUNKS_HIGH; y++) {

                    if (loadedChunks[x +
                            (z * CHUNKS_WIDE) +
                            (y * CHUNKS_WIDE * CHUNKS_WIDE)] == null) {
                        loadedChunks[x +
                                (z * CHUNKS_WIDE) +
                                (y * CHUNKS_WIDE * CHUNKS_WIDE)]
                                = this.loadChunk(x - (CHUNKS_WIDE/2),
                                y - (CHUNKS_HIGH/2), z - (CHUNKS_WIDE/2), playerChunk);
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

        for (int i = 0; i < 1024; i++)
            newChunk.setBlock(i, 1);
        return newChunk;
    }

    public void render(int a, int b, Matrix4f d) {
        for (Chunk c : loadedChunks) {
            if (c != null)
                c.render(a,b,d);
        }
    }
}
