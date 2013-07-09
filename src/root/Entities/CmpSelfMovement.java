package root.Entities;

import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;

public class CmpSelfMovement extends CmpMovement implements Serializable {

    public Vector3f moveDirection;
    public float maxUnitsPerSec;
    public float acceleration;

    public CmpSelfMovement(Vector3f np, Process p) {
        type = ComponentType.MOVEMENT;
        nextPosition = np;
        rotation = new Vector3f();
        velocity = new Vector3f();
        moveDirection = new Vector3f();
        acceleration = 1;
        maxUnitsPerSec = 12;
        process = p;
    }
}
