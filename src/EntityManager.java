import Entities.*;
import Utilities.Constants;
import Utilities.Model;
import Utilities.ShaderProgram;
import Utilities.Vector3i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.mapdb.*;
import org.lwjgl.util.vector.Vector3f;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

public class EntityManager {

    private DB db;
    NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap;
    List<GameEntity> entities;
    ModelManager modelBaron;
    private int nextFreeId;

    FloatBuffer lightBuffer = BufferUtils.createFloatBuffer(Constants.MAX_NUM_LIGHTS * 7);

    private int chunksWide;
    private int chunksHigh;

    private Vector3f minCoords;
    private Vector3f maxCoords;

    /**
     *
     * @param w How many chunks wide to load
     * @param h How many chunks high to load
     * @param x X position of the central chunk
     * @param y Y position of the central chunk
     * @param z Z position of the central chunk
     */
    public EntityManager(int w, int h, int x, int y, int z) {

        db = DBMaker.newFileDB(new File("res/database")).make();
        nextFreeId = 0;
        chunksHigh = h;
        chunksWide = w;

        entities = new ArrayList<GameEntity>();
        modelBaron = new ModelManager();

        centerChanged(x,y,z);

        // Recreate database
        //NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.createTreeSet("EntityDatabase",32,false, BTreeKeySerializer.TUPLE2, null);

        // Open database
        //NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.getTreeSet("EntityDatabase");

        // Remove from database
        //for (GameEntity e : Bind.findSecondaryKeys(multiMap, new Vector3i(0,0,0))) {
        //    multiMap.remove(Fun.t2(new Vector3i(0,0,0),e));
        //}

        // Put into database
        /*GameEntity testItem = new GameEntity(0, 3, 3, 3);
        testItem.addComponent(new CmpRotation(new Vector3f(2,2,4)));
        testItem.addComponent(new CmpModel("res/items/Test.mdl"));

        GameEntity light1 = new GameEntity(1, 3, 7, 3);
        light1.addComponent(new CmpPointLight(1,0.8f,0.7f,20));

        GameEntity light2 = new GameEntity(2, 25, 7, 15);
        light2.addComponent(new CmpPointLight(0, 0.8f, 0.7f, 15));

        multiMap.add(Fun.t2(new Vector3i(0,0,0), testItem));
        multiMap.add(Fun.t2(new Vector3i(0,0,0), light1));
        multiMap.add(Fun.t2(new Vector3i(0,0,0), light2));

        db.commit();
        db.close();*/
    }

    public void centerChanged(int x, int y, int z) {

        minCoords = new Vector3f(x - (chunksWide / 2) * Chunk.CHUNK_WIDTH,
                y - (chunksHigh / 2) * Chunk.CHUNK_HEIGHT,
                z - (chunksWide / 2) * Chunk.CHUNK_WIDTH);
        maxCoords = new Vector3f(x + (chunksWide / 2 + 1) * Chunk.CHUNK_WIDTH,
                y + (chunksHigh / 2 + 1) * Chunk.CHUNK_HEIGHT,
                z + (chunksWide / 2 + 1) * Chunk.CHUNK_WIDTH);

        NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.getTreeSet("EntityDatabase");

        if (!entities.isEmpty()) {
            Iterator<GameEntity> it = entities.iterator();
            GameEntity e;
            // Save out entities in unloaded chunks
            while (it.hasNext()) {
                e = it.next();
                if (e.getPosition().x >= maxCoords.x || e.getPosition().x < minCoords.x
                        || e.getPosition().z >= maxCoords.z || e.getPosition().z < minCoords.z
                        || e.getPosition().y >= maxCoords.y || e.getPosition().y < minCoords.y ) {
                    multiMap.add(Fun.t2(new Vector3i(
                            (int)e.getPosition().x / Chunk.CHUNK_WIDTH * Chunk.CHUNK_WIDTH,
                            (int)e.getPosition().y / Chunk.CHUNK_HEIGHT * Chunk.CHUNK_HEIGHT,
                            (int)e.getPosition().z / Chunk.CHUNK_WIDTH * Chunk.CHUNK_WIDTH), e));
                    it.remove();
                }
            }
        }

        // Load in entities from newly loaded chunks
        Vector3i cPos = new Vector3i();
        for (cPos.x = (int)minCoords.x; cPos.x < maxCoords.x; cPos.x += Chunk.CHUNK_WIDTH) {
            for (cPos.y = (int)minCoords.y; cPos.y < maxCoords.y; cPos.y += Chunk.CHUNK_HEIGHT) {
                for (cPos.z = (int)minCoords.z; cPos.z < maxCoords.z; cPos.z += Chunk.CHUNK_WIDTH) {
                    for (GameEntity a : Bind.findSecondaryKeys(multiMap, cPos)) {
                        entities.add(a);
                        multiMap.remove(Fun.t2(cPos, a));
                        if (a.hasComponent(ComponentType.MODEL)) {
                            modelBaron.loadModel(
                                    ((CmpModel)a.getComponent(ComponentType.MODEL)).modelPath);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the list of up to MAX_NUM_LIGHTS which are loaded and closest to p.
     * @param p The position to get lights relative to
     * @return The list of lights nearest point p
     */
    public FloatBuffer getLights(Vector3f p) {
        List<GameEntity> lights = new ArrayList<GameEntity>();

        int playerX, PlayerY, PlayerZ;
        playerX = ((int)p.x / Chunk.CHUNK_WIDTH) * Chunk.CHUNK_WIDTH;
        PlayerY = ((int)p.y / Chunk.CHUNK_HEIGHT) * Chunk.CHUNK_HEIGHT;
        PlayerZ = ((int)p.z / Chunk.CHUNK_WIDTH) * Chunk.CHUNK_WIDTH;

        if (p.x < 0)
            playerX -= Chunk.CHUNK_WIDTH;
        if (p.y < 0)
            PlayerY -= Chunk.CHUNK_HEIGHT;
        if (p.z < 0)
            PlayerZ -= Chunk.CHUNK_WIDTH;

        // Doesn't yet calculate closest to player.
        for (GameEntity a : entities) {
            if (a.hasComponent(ComponentType.POINT_LIGHT)) {
                lights.add(a);
            }
        }

        CmpPointLight l;
        for (GameEntity a : lights) {

            lightBuffer.put(a.getPosition().x);
            lightBuffer.put(a.getPosition().y);
            lightBuffer.put(a.getPosition().z);
        }
        lightBuffer.position(Constants.MAX_NUM_LIGHTS * 3);

        for (GameEntity a : lights) {
            l = (CmpPointLight)a.getComponent(ComponentType.POINT_LIGHT);
            lightBuffer.put(l.color.x);
            lightBuffer.put(l.color.y);
            lightBuffer.put(l.color.z);
            lightBuffer.put(l.intensity);
        }
        lightBuffer.flip();

        return lightBuffer;
    }

    public void drawModels(Matrix4f perspectiveMatrix) {
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

        for (GameEntity e : entities) {
            if (e.hasComponent(ComponentType.MODEL)) {

                Model m = modelBaron.getModel(((CmpModel) e.getComponent(ComponentType.MODEL)).modelPath);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, m.vbo);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, m.ibo);

                org.lwjgl.util.vector.Matrix4f worldMatrix = new Matrix4f();

                worldMatrix.translate(e.getPosition());
                //worldMatrix.rotate((float)Math.toRadians((double)-yaw), new Vector3f(0.0f, 1.0f, 0.0f));
                org.lwjgl.util.vector.Matrix4f.mul(perspectiveMatrix, worldMatrix, worldMatrix);
                matrixBuffer.position(0);
                worldMatrix.store(matrixBuffer);
                matrixBuffer.flip();

                GL20.glUseProgram(ShaderProgram.getDefaultProgramId());

                GL20.glUniformMatrix4(ShaderProgram.getDefaultTransformMatrixId(), false, matrixBuffer);

                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

                GL11.glDrawElements(GL11.GL_LINE_LOOP, m.indices.limit(), GL11.GL_UNSIGNED_INT, 0);

                GL20.glDisableVertexAttribArray(0);
                GL20.glUseProgram(0);
            }
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void closeDatabase() {

        NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.getTreeSet("EntityDatabase");

        for (GameEntity e : entities) {
            multiMap.add(Fun.t2(new Vector3i(
                    (int)e.getPosition().x / Chunk.CHUNK_WIDTH * Chunk.CHUNK_WIDTH,
                    (int)e.getPosition().y / Chunk.CHUNK_HEIGHT * Chunk.CHUNK_HEIGHT,
                    (int)e.getPosition().z / Chunk.CHUNK_WIDTH * Chunk.CHUNK_WIDTH), e));
        }

        entities.clear();
        db.commit();
        db.close();
    }
}
