package Entities;

import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;

public class CmpRotation extends Component implements Serializable {

    public static int type = ComponentType.ROTATION;
    public Vector3f rotation;

    /**
     * Constructor.
     * @param r Rotation
     */
    public CmpRotation(Vector3f r) {
        rotation = r;
    }

    public int getType() {
        return type;
    }
}
