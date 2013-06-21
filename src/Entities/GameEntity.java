package Entities;

import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;

public class GameEntity implements Serializable, Comparable<GameEntity> {

    private Vector3f position;
    public int id;

    /**
     * Default constructor, should probably NEVER be used
     */
    public GameEntity(int id) {
        position = new Vector3f(0,0,0);
        this.id = id;
    }

    public GameEntity(int id, Vector3f p) {
        this.id = id;
        position = new Vector3f(p);
    }

    public GameEntity(int id, float x, float y, float z) {
        this.id = id;
        position = new Vector3f(x, y, z);
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    @Override
    public int compareTo(GameEntity e) {
        if (this.id < e.id) return -1;
        if (this.id == e.id) return 0;
        return 1;

    }
}
