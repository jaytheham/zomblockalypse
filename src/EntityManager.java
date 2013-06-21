import Entities.GameEntity;
import Utilities.Vector3i;
import org.mapdb.*;
import org.lwjgl.util.vector.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

public class EntityManager {

    private DB db;
    private int nextFreeId;

    private int backZ;
    private int leftX;

    private int chunksLoadedWidth;
    private int chunksLoadedHeight;

    public EntityManager(int w, int h) {

        db = DBMaker.newFileDB(new File("res/database")).make();
        nextFreeId = 0;
        chunksLoadedHeight = h;
        chunksLoadedWidth = w;

        //
        //
        //NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.createTreeSet("EntityDatabase",32,false, BTreeKeySerializer.TUPLE2, null);

        //NavigableSet<Fun.Tuple2<Vector3i, GameEntity>> multiMap = db.getTreeSet("test2");

        //List<GameEntity> ents = new ArrayList<GameEntity>();

        //for (GameEntity x : Bind.findSecondaryKeys(multiMap, new Vector3i(0,0,0))) {
        //    ents.add(x);
        //    multiMap.remove(Fun.t2(new Vector3i(0,0,0),x));
        //}


        //multiMap.add(Fun.t2(new Vector3i(0,0,0), (GameEntity)new Entities.PointLight(nextFreeId++, new Vector3f(0.0f, 7.0f, 0.0f), 1.0f, 0.0f, 0.0f, 25.0f)));
        //multiMap.add(Fun.t2(new Vector3i(0,0,0), (GameEntity)new Entities.PointLight(nextFreeId++, new Vector3f(20.0f, 7.0f, 0.0f), 0.0f, 1.0f, 0.0f, 25.0f)));
        //multiMap.add(Fun.t2(new Vector3i(0, 0, 0), (GameEntity)new Entities.PointLight(nextFreeId++, new Vector3f(39.0f, 7.0f, 0.0f), 0.0f, 0.0f, 1.0f, 25.0f)));
        //db.commit();
        db.close();
    }

    /**
     * Get the list of up to 8 lights which are loaded and closest to p.
     * @param p The position to get lights relative to
     * @return The list of lights nearest point p
     */
    //public List<GameEntity> getLights(Vector3f p) {
    //    List<GameEntity> lights = new ArrayList<GameEntity>();
    //}
}
