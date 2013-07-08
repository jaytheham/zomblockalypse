package root.Entities;

import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;

public class CmpMovement extends Component implements Serializable {

    public Vector3f nextPosition;
    public Vector3f rotation;
    public Vector3f target;
    public Vector3f velocity;
    public float maxVelocity;

    /**
     * Constructor.
     * @param np Initial value of nextPostion, should be current position
     * @param p Process to run on this Component
     */
    public CmpMovement(Vector3f np, Process p) {
        type = ComponentType.MOVEMENT;
        nextPosition = np;
        rotation = new Vector3f();
        velocity = new Vector3f();
        process = p;
    }

}
