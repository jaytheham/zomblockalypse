import Entities.GameEntity;
import Entities.PointLight;
import Utilities.Constants;
import Utilities.Vector3i;
import org.lwjgl.BufferUtils;
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
    private int nextFreeId;

    private int backZ;
    private int leftX;

    private int chunksWide;
    private int chunksHigh;
    private int centerX;
    private int centerY;
    private int centerZ;

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

        centerChanged(x,y,z);


        //
        //
        //NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.createTreeSet("EntityDatabase",32,false, BTreeKeySerializer.TUPLE2, null);

        //NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.getTreeSet("EntityDatabase");

        //List<GameEntity> ents = new ArrayList<GameEntity>();

        //for (GameEntity x : Bind.findSecondaryKeys(multiMap, new Vector3i(0,0,0))) {
        //    multiMap.remove(Fun.t2(new Vector3i(0,0,0),x));
        //}


        //multiMap.add(Fun.t2(new Vector3i(0,0,0), (GameEntity)new Entities.PointLight(2, new Vector3f(5.0f, 7.0f, 0.0f), 1.0f, 0.0f, 0.0f, 15.0f)));
        //multiMap.add(Fun.t2(new Vector3i(0,0,0), (GameEntity)new Entities.PointLight(nextFreeId++, new Vector3f(20.0f, 7.0f, 0.0f), 0.0f, 1.0f, 0.0f, 25.0f)));
        //multiMap.add(Fun.t2(new Vector3i(0, 0, 0), (GameEntity)new Entities.PointLight(nextFreeId++, new Vector3f(39.0f, 7.0f, 0.0f), 0.0f, 0.0f, 1.0f, 25.0f)));
        //db.commit();
        //db.close();
    }

    public void centerChanged(int x, int y, int z) {
        centerX = x;
        centerY = y;
        centerZ = z;

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
                    }
                }
            }
        }
    }

    /**
     * Get the list of up to 8 lights which are loaded and closest to p.
     * @param p The position to get lights relative to
     * @return The list of lights nearest point p
     */
    public FloatBuffer getLights(Vector3f p) {
        NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.getTreeSet("EntityDatabase");
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
            if (PointLight.class.isAssignableFrom(a.getClass())) {
                lights.add(a);
            }
        }

        FloatBuffer lightBuffer = BufferUtils.createFloatBuffer(Constants.MAX_NUM_LIGHTS * 7);

        PointLight l;
        for (GameEntity a : lights) {
            l = (PointLight)a;
            lightBuffer.put(l.getPosition().x);
            lightBuffer.put(l.getPosition().y);
            lightBuffer.put(l.getPosition().z);
        }
        lightBuffer.position(Constants.MAX_NUM_LIGHTS * 3);

        for (GameEntity a : lights) {
            l = (PointLight)a;
            lightBuffer.put(l.getColor().x);
            lightBuffer.put(l.getColor().y);
            lightBuffer.put(l.getColor().z);
            lightBuffer.put(l.getIntensity());
        }
        lightBuffer.flip();

        return lightBuffer;
    }

    public void closeDatabase() {

        NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.getTreeSet("EntityDatabase");

        for (GameEntity e : entities) {
            multiMap.add(Fun.t2(new Vector3i(
                    (int)e.getPosition().x / Chunk.CHUNK_WIDTH * Chunk.CHUNK_WIDTH,
                    (int)e.getPosition().y / Chunk.CHUNK_HEIGHT * Chunk.CHUNK_HEIGHT,
                    (int)e.getPosition().z / Chunk.CHUNK_WIDTH * Chunk.CHUNK_WIDTH), e));
        }

        db.commit();
        db.close();
    }
}
