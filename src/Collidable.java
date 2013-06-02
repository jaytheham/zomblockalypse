import org.lwjgl.util.vector.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Jay
 * Date: 2/06/13
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
interface Collidable {

    Vector3f getPosition();

    Vector3f getNextPosition();

    void setPosition(Vector3f newPosition);
}
