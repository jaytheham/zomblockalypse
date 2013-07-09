package root.Entities;


import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;

public class CmpCollision extends Component implements Serializable {

    public int step = 1;
    public int climb = 6;

    public Vector3f boundingBoxSize;

    public CmpCollision(int stepSize, int climbSize, Vector3f boundingBox, Process p) {
        type = ComponentType.COLLISION;
        step = stepSize;
        climb = climbSize;
        boundingBoxSize = boundingBox;
        process = p;
    }
}
