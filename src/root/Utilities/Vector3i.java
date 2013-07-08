package root.Utilities;

import java.io.Serializable;

public class Vector3i implements Comparable<Vector3i>, Serializable {

    public int x,y,z;

    public Vector3i() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3i(Vector3i source) {
        x = source.x;
        y = source.y;
        z = source.z;
    }

    public Vector3i(int newX, int newY, int newZ) {
        x = newX;
        y = newY;
        z = newZ;
    }

    public int compareTo(Vector3i i) {
        if (this.x == i.x && this.y == i.y && this.z == i.z)
            return 0;
        if (this.y > i.y)
            return 1;
        if (this.y == i.y && this.z > i.z)
            return 1;
        if (this.y == i.y && this.z == i.z && this.x > i.x)
            return 1;
        return -1;
    }

}
