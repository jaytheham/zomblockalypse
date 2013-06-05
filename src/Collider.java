import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector2f;

/**
 * Created with IntelliJ IDEA.
 * User: Jay
 * Date: 2/06/13
 * Time: 6:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class Collider {

    public Collider() {

    }

    public void checkBounds(Player p) {
        ChunkManager c = ChunkManager.getInstance(null);

        Vector3f curPos = p.getPosition();
        Vector3f nextPos = new Vector3f(p.getNextPosition());
        Vector3f bounds = new Vector3f(p.getBoundingBoxSize());

        if (nextPos.x < 0.0f)
            nextPos.x -= 1;

        if (nextPos.z < 0.0f)
            nextPos.z -= 1;

        Vector2f lowerBound = new Vector2f(nextPos.x - (bounds.x/2), nextPos.z - (bounds.z/2));
        Vector2f upperBound = new Vector2f(nextPos.x + (bounds.x/2), nextPos.z + (bounds.z/2));

        boolean hitBlock = false;

        for (int x = (int)lowerBound.x; x <= (int)upperBound.x; x++) {
            for (int z = (int)lowerBound.y; z <= (int)upperBound.y; z++) {
                if (c.getBlock(x, (int)nextPos.y, z) != 0) {
                    hitBlock = true;
                    break;
                }
            }
        }

        if (hitBlock) {
            hitBlock = false;
            for (int x = (int)lowerBound.x; x <= (int)upperBound.x; x++) {
                if (c.getBlock(x, (int)curPos.y, (int)curPos.z) != 0) {
                    hitBlock = true;
                    break;
                }
            }

            if (hitBlock) {
                hitBlock = false;
                for (int z = (int)lowerBound.y; z <= (int)upperBound.y; z++) {
                    if (c.getBlock((int)curPos.x, (int)curPos.y, z) != 0) {
                        hitBlock = true;
                        break;
                    }
                }

                if (!hitBlock) {
                    p.setPosition(new Vector3f(curPos.x, curPos.y, p.getNextPosition().z));
                }
            }
            else {
                p.setPosition(new Vector3f(p.getNextPosition().x, curPos.y, curPos.z));
            }

            p.setNextPosition(new Vector3f(p.getX(), p.getY(), p.getZ()));
        }
        else {
            p.setPosition(p.getNextPosition());
        }

    }

}
