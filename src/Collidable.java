import org.lwjgl.util.vector.Vector3f;

interface Collidable {

    /**
     * Get the objects current position.
     * @return the objects current position.
     */
    Vector3f getPosition();

    /**
     * Get the position the object wishes to move to.
     * @return the objects projected next position.
     */
    Vector3f getNextPosition();

    /**
     * Set the objects position (after finishing all collision detection).
     * @param newPosition the position the object can move to without colliding.
     */
    void setPosition(Vector3f newPosition);
}
