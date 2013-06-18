import org.lwjgl.util.vector.Vector3f;

public class GameObject {

    private Vector3f position;

    /**
     * Default constructor, should probably NEVER be used
     */
    public GameObject() {
        position = new Vector3f(0,0,0);
    }

    public GameObject(Vector3f p) {
        position = new Vector3f(p);
    }

    public GameObject(float x, float y, float z) {
        position = new Vector3f(x, y, z);
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }
}
