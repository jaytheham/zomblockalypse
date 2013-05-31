import org.lwjgl.util.vector.Vector3f;

public class Player {

    private Vector3f position;

    public Player() {
        position = new Vector3f(15.0f, 15.0f, 0.0f);
    }

    public void setPosition(Vector3f newPosition) {
        position = newPosition;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getZ() {
        return position.z;
    }
}
