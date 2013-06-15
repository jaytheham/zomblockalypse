package Utils;

public class Vector3i {

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


}
