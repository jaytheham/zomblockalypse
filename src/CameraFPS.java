import org.lwjgl.util.vector.Vector3f;

public class CameraFPS extends Camera {

    Vector3f direction;
    float angleAboutY = 0.0f;
    float angleAboutX = (float)Math.PI/2;
    static Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

    public CameraFPS() {
        super();
        direction = new Vector3f();
    }

    public void moveBackwards(float distance) {
        Vector3f.sub(target, position, direction);
        direction.normalise();
        direction.scale(distance);
        Vector3f.sub(position, direction, position);
        Vector3f.sub(target, direction, target);

        changed = true;
    }

    public void moveForwards(float distance) {
        Vector3f.sub(target, position, direction);
        direction.normalise();
        direction.scale(distance);
        Vector3f.add(position, direction, position);
        Vector3f.add(target, direction, target);

        changed = true;
    }

    public void moveLeft(float distance) {
        Vector3f.sub(target, position, direction);
        direction.normalise();
        Vector3f.cross(direction, up, direction);
        direction.normalise();
        direction.negate();
        direction.scale(distance);
        Vector3f.add(position, direction, position);
        Vector3f.add(target, direction, target);

        changed = true;
    }

    public void moveRight(float distance) {
        Vector3f.sub(target, position, direction);
        direction.normalise();
        Vector3f.cross(direction, up, direction);
        direction.normalise();
        direction.scale(distance);
        Vector3f.add(position, direction, position);
        Vector3f.add(target, direction, target);
        changed = true;
    }

    public void moveTarget(float changeAboutY, float changeAboutX) {
        angleAboutY += changeAboutY * 2;
        angleAboutX -= changeAboutX * 2;

        angleAboutY %= 2 * Math.PI;
        if (angleAboutX > Math.PI)
            angleAboutX = (float)(Math.PI-0.0000001);
        else if (angleAboutX < 0)
            angleAboutX = 0.0000001f;

        direction.setZ(-(float)Math.cos((double)angleAboutY) * (float)Math.sin((double)angleAboutX));
        direction.setY((float)Math.cos((double)angleAboutX));
        direction.setX((float)(Math.sin((double)angleAboutY) * Math.sin((double)angleAboutX)));

        Vector3f.add(position, direction, target);

        changed = true;
    }

    /*
    @Override
    public Matrix4f getMatrix() {
        if (changed) {
            matrix.setIdentity();


            //Rotate around X
            matrix.rotate(angleAboutX, new Vector3f(1.0f, 0.0f, 0.0f));

            //Rotate around Y
            matrix.rotate(angleAboutY, new Vector3f(0.0f, 1.0f, 0.0f));
            matrix.translate(position);

            changed = false;
        }

        return matrix;
    }*/
}
