package root;

import root.Utilities.Constants;
import root.Utilities.ShaderUtils;
import root.Utilities.Vector3i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class ChunkManager {

	private static ChunkManager instance = null;
	//These must be odd numbers
	public static final int CHUNKS_WIDE = 5;
	public static final int CHUNKS_HIGH = 3;

	private Chunk[] activeChunks;
	// The center (in world coordinates) of the chunk in the middle of active chunks
	// If the chunk in the middle is the one at 0,0,0, this will be 20,10,20
	private static Vector3i activeChunksCenterPoint;

	private int programId;
	private int uniformTextureId;
	private int textureId;

	private int inPosition;
	private int inBlockData;

	private int uniformMatrixId;

	private int uniformLightPositionsId;
	private int uniformLightColorsId;

	private EntityManager entityBaron;


	protected ChunkManager(Vector3f playerPosition) {

		setupShader("Shaders/BlockVertex.glsl", "Shaders/BlockFragment.glsl");

		activeChunks = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

		activeChunksCenterPoint = new Vector3i();

		activeChunksCenterPoint.x = Chunk.CHUNK_WIDTH *
				(int)Math.floor(playerPosition.getX() / Chunk.CHUNK_WIDTH) + (Chunk.CHUNK_WIDTH / 2);
		activeChunksCenterPoint.y = Chunk.CHUNK_HEIGHT *
				(int)Math.floor(playerPosition.getY() / Chunk.CHUNK_HEIGHT) + (Chunk.CHUNK_HEIGHT / 2);
		activeChunksCenterPoint.z = Chunk.CHUNK_WIDTH *
				(int)Math.floor(playerPosition.getZ() / Chunk.CHUNK_WIDTH) + (Chunk.CHUNK_WIDTH / 2);

		EntityManager.init(
				CHUNKS_WIDE,
				CHUNKS_HIGH,
				activeChunksCenterPoint.x - Chunk.CHUNK_WIDTH / 2,
				activeChunksCenterPoint.y - Chunk.CHUNK_HEIGHT / 2,
				activeChunksCenterPoint.z - Chunk.CHUNK_WIDTH / 2);
		entityBaron = EntityManager.getInstance();

		updateNullChunks(CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH);
	}

	public static ChunkManager getInstance(Vector3f pp) {
		if (instance == null) {
			instance = new ChunkManager(pp);
		}
		return instance;
	}

	public static Vector3i getActiveChunksCenterPoint() {
		return activeChunksCenterPoint;
	}

	public void update() {

		int xChange, yChange, zChange;
		float horizontalThreshold = 0.8f;
		float verticalThreshold = 0.8f;

		float xC, yC, zC;

		xC = (Game.camera.playerPosition.getX() - activeChunksCenterPoint.x) / Chunk.CHUNK_WIDTH;
		yC = (Game.camera.playerPosition.getY() - activeChunksCenterPoint.y) / Chunk.CHUNK_HEIGHT;
		zC = (Game.camera.playerPosition.getZ() - activeChunksCenterPoint.z) / Chunk.CHUNK_WIDTH;

		// Only perform a load if player has moved 0.8/0.7 chunks from the center of the last
		// to prevent them flip-flopping between chunks and load thrashing
		if (    xC < -horizontalThreshold || xC > horizontalThreshold ||
				zC < -horizontalThreshold || zC > horizontalThreshold ||
				yC < -verticalThreshold || yC > verticalThreshold) {

			//This is used as a buffer so no need to null any chunks
			Chunk[] activeChunksTempBuffer = new Chunk[CHUNKS_WIDE * CHUNKS_WIDE * CHUNKS_HIGH];

			activeChunksCenterPoint.x = Chunk.CHUNK_WIDTH *
					(int)Math.floor(Game.camera.playerPosition.getX() / Chunk.CHUNK_WIDTH) + (Chunk.CHUNK_WIDTH / 2);
			activeChunksCenterPoint.y = Chunk.CHUNK_HEIGHT *
					(int)Math.floor(Game.camera.playerPosition.getY() / Chunk.CHUNK_HEIGHT) + (Chunk.CHUNK_HEIGHT / 2);
			activeChunksCenterPoint.z = Chunk.CHUNK_WIDTH *
					(int)Math.floor(Game.camera.playerPosition.getZ() / Chunk.CHUNK_WIDTH) + (Chunk.CHUNK_WIDTH / 2);

			entityBaron.centerChanged(
					activeChunksCenterPoint.x - Chunk.CHUNK_WIDTH / 2,
					activeChunksCenterPoint.y - Chunk.CHUNK_HEIGHT / 2,
					activeChunksCenterPoint.z - Chunk.CHUNK_WIDTH / 2);

			xChange = (int)(xC * 2);
			yChange = (int)(yC * 2);
			zChange = (int)(zC * 2);

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
							getActiveChunk(x, y, z).prepareForDeletion();
						}
						else if ((xChange < 0) && (x > xStart + xChange)) {
							saveChunk(getActiveChunk(x, y, z));
							getActiveChunk(x, y, z).prepareForDeletion();
						}
						else if ((zChange > 0) && (z < zStart + zChange)) {
							saveChunk(getActiveChunk(x, y, z));
							getActiveChunk(x, y, z).prepareForDeletion();
						}
						else if ((zChange < 0) && (z > zStart + zChange)) {
							saveChunk(getActiveChunk(x, y, z));
							getActiveChunk(x, y, z).prepareForDeletion();
						}
						else if ((yChange > 0) && (y < yStart + yChange)) {
							saveChunk(getActiveChunk(x, y, z));
							getActiveChunk(x, y, z).prepareForDeletion();
						}
						else if ((yChange < 0) && (y > yStart + yChange)) {
							saveChunk(getActiveChunk(x, y, z));
							getActiveChunk(x, y, z).prepareForDeletion();
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

	private Chunk getActiveChunk(int x, int y, int z) {
		return activeChunks[
				 x +
				(y * CHUNKS_WIDE * CHUNKS_WIDE) +
				(z * CHUNKS_WIDE)];
	}

	/**
	 * Returns the root.Chunk that the given world coordinates are inside.
	 * @return the root.Chunk that contains position (x,y,z) or null if out of bounds
	 */
	private Chunk getChunkAtWorldCoords(int x, int y, int z) {

		int[] position = activeChunks[0].getPosition();
		// Should be safe to base off chunk 0 as it is always loaded first
		// after any changes to activeChunks.

		position[0] = (int)Math.floor(((float)x - position[0]) / Chunk.CHUNK_WIDTH);
		position[1] = (int)Math.floor(((float)y - position[1]) / Chunk.CHUNK_HEIGHT);
		position[2] = (int)Math.floor(((float)z - position[2]) / Chunk.CHUNK_WIDTH);

		// Check chunk is currently loaded
		if (position[0] >= CHUNKS_WIDE || position[0] < 0 ||
			position[2] >= CHUNKS_WIDE || position[2] < 0 ||
			position[1] >= CHUNKS_HIGH || position[1] < 0)
			return null;

		return activeChunks[
				position[0] +
			   (position[1] * CHUNKS_WIDE * CHUNKS_WIDE) +
			   (position[2] * CHUNKS_WIDE)];
	}

	// Return the value of the block, or 0 if the chunk is not loaded.
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
		if (c == null)
			return;

		// If setting a boundary block update the blocks it touches
		Chunk c2;
		if (x % Chunk.CHUNK_WIDTH == 0) {
			c2 = getChunkAtWorldCoords(x - 1, y, z);
			if (c2 != null)
				c2.invalidate();
		}
		else if (x == Chunk.CHUNK_WIDTH - 1 || x % Chunk.CHUNK_WIDTH == -1) {
			c2 = getChunkAtWorldCoords(x + 1, y, z);
			if (c2 != null)
				c2.invalidate();
		}
		if (y % Chunk.CHUNK_HEIGHT == 0) {
			c2 = getChunkAtWorldCoords(x, y - 1, z);
			if (c2 != null)
				c2.invalidate();
		}
		else if (y == Chunk.CHUNK_HEIGHT - 1 || y % Chunk.CHUNK_HEIGHT == -1) {
			c2 = getChunkAtWorldCoords(x, y + 1, z);
			if (c2 != null)
				c2.invalidate();
			}
		if (z % Chunk.CHUNK_WIDTH == 0) {
			c2 = getChunkAtWorldCoords(x, y, z - 1);
			if (c2 != null)
				c2.invalidate();
		}
		else if (z == Chunk.CHUNK_WIDTH - 1 || z % Chunk.CHUNK_WIDTH == -1) {
			c2 = getChunkAtWorldCoords(x, y, z + 1);
			if (c2 != null)
				c2.invalidate();
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

		c.setBlock(x, y, z, newBlockVal);
	}

	public void deleteBlock(int x, int y, int z) {
		setBlock(x, y, z, 0);
	}

	public void deleteBlock(int[] pos) {
		setBlock(pos[0], pos[1], pos[2], 0);
	}

	private void updateNullChunks(int maxChunksToLoad) {

		int numChunksLoaded = 0;
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
								activeChunksCenterPoint);
						numChunksLoaded++;
						if (numChunksLoaded == maxChunksToLoad)
							return;
					}
				}
			}
		}
	}

	private Chunk loadChunk(int x, int y ,int z, Vector3i playerChunk) {
		Chunk newChunk = new Chunk(
				x * Chunk.CHUNK_WIDTH + (playerChunk.x - (Chunk.CHUNK_WIDTH / 2)),
				y * Chunk.CHUNK_HEIGHT + (playerChunk.y - (Chunk.CHUNK_HEIGHT / 2)),
				z * Chunk.CHUNK_WIDTH + (playerChunk.z - (Chunk.CHUNK_WIDTH / 2)));

		ChunkLoader loader = new ChunkLoader(newChunk);
		loader.load();

		return newChunk;
	}

	public void saveAllChunks() {
		for (Chunk c : activeChunks)
			saveChunk(c);
	}

	private void saveChunk(Chunk c) {
		if (c.isLoaded() && c.hasUnsavedChanges()) {
			ChunkSaver saver = new ChunkSaver(c);
			saver.save();
		}
	}

	public void setupShader(String vertShader, String fragShader) {
		int vsId = ShaderUtils.loadShader(vertShader, GL20.GL_VERTEX_SHADER);
		int fsId = ShaderUtils.loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);

		programId = GL20.glCreateProgram();
		GL20.glAttachShader(programId, vsId);
		GL20.glAttachShader(programId, fsId);

		GL20.glLinkProgram(programId);

		inPosition = GL20.glGetAttribLocation(programId, "in_Position");
		inBlockData = GL20.glGetAttribLocation(programId, "in_BlockType");

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

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);


		root.Utilities.TextureLoader.loadPNG(Constants.BLOCK_TEXTURES_PATH, GL11.GL_RGB);

		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);


		// Unbind stuff?

	}

	public void render(Matrix4f perspectiveMatrix, Vector3f camPosition, Vector3f camDirection) {

		entityBaron.drawModels(perspectiveMatrix);

		FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
		FloatBuffer lights = entityBaron.getLights(Game.camera.playerPosition);

		perspectiveMatrix.store(matrixBuffer);
		matrixBuffer.flip();

		GL20.glUseProgram(programId);

		GL13.glActiveTexture(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL20.glUniform1i(uniformTextureId, 0);

		GL20.glUniformMatrix4(uniformMatrixId, false, matrixBuffer);
		lights.limit(Constants.MAX_NUM_LIGHTS * 3);
		GL20.glUniform3(uniformLightPositionsId, lights);
		lights.position(Constants.MAX_NUM_LIGHTS * 3);
		lights.limit(lights.capacity());
		GL20.glUniform4(uniformLightColorsId, lights);

		int[] cPos;
		Vector3f cPosv = new Vector3f();
		camDirection.negate();
		camDirection.scale(56.0f);
		Vector3f.add(camPosition, camDirection, camPosition);

		for (Chunk c : activeChunks) {
			if (c != null && c.isLoaded()) {
				cPos = c.getPosition();
				cPosv.set(cPos[0], cPos[1], cPos[2]);
				Vector3f.sub(cPosv, camPosition, cPosv);
				if (Math.toDegrees(Vector3f.angle(cPosv, camDirection)) > 120.0f) {
					c.render(this, inPosition, inBlockData);
				}
			}
		}

		GL20.glUseProgram(0);
	}
}
