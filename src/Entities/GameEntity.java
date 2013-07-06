package Entities;

import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;
import java.util.ArrayList;

public class GameEntity implements Serializable, Comparable<GameEntity> {

    private int componentFlags;
    public int id;
    private Vector3f position;
    private ArrayList<Component> components;

    public GameEntity(int parId, Vector3f p) {
        id = parId;
        componentFlags = 0;
        position = p;
        components = new ArrayList<Component>();
    }

    public GameEntity(int parId, float x, float y, float z) {
        this(parId, new Vector3f(x,y,z));
    }

    public void addComponent(Component comp) {
        components.add(comp);
        componentFlags = componentFlags | comp.getType();
    }

    public boolean hasComponent(int type) {
        return (componentFlags & type) == type;
    }

    public Component getComponent(int type) {
        for (Component x : components) {
            if (x.getType() == type) {
                return x;
            }
        }
        return null;
    }

    public void removeComponent(int type) {
        Component c = null;
        for (Component x : components) {
            if (x.getType() == type) {
                c = x;
                break;
            }
        }

        if (c != null) {
            components.remove(c);
            componentFlags = componentFlags ^ type;
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f p) {
        position = p;
    }

    @Override
    public int compareTo(GameEntity e) {
        if (this.id < e.id) return -1;
        if (this.id == e.id) return 0;
        return 1;

    }
}
