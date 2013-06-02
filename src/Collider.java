import org.lwjgl.util.vector.Vector3f;

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

        Vector3f pos = new Vector3f(p.getNextPosition());
        Vector3f bounds = p.getBoundingBoxSize();

        //if (pos.z < 0.0f)
        //    pos.z -= 1.0f;
        if (pos.z > 0)
            pos.z += bounds.z;
        else
            pos.z -= bounds.z;

        if (c.getBlock(pos) == 0) {
            p.setPosition(p.getNextPosition());
        }
        else {
            System.out.println(c.getBlock(pos));
        }
    }

}
