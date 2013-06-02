import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Camera {
    Matrix4f matrix;
    Vector3f position;
    Vector3f target;
    boolean changed;

    public Camera() {
        matrix = new Matrix4f();
        position = new Vector3f(1.0f, 1.0f, -1.0f);
        target =  new Vector3f(1.0f, 1.0f, -2.0f);
        changed = true;
    }

    public void setPosition(Vector3f newPosition) {
        position = newPosition;
        changed = true;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y ,z);
        changed = true;
    }

    public void offsetPosition(Vector3f offset) {
        Vector3f.add(position, offset, position);
        changed = true;
    }

    public void offsetPosition(float offX, float offY, float offZ) {
        Vector3f offset = new Vector3f(offX, offY, offZ);
        Vector3f.add(position, offset, position);
        changed = true;
    }

    /**
     * Return the Camera's position
     * @return A Vector3f of the Camera's position
     */
    public Vector3f getPos() {
        return position;
    }

    public void setTarget(Vector3f newTarget) {
        target = newTarget;
        changed = true;
    }

    public void setTarget(float x, float y, float z) {
        target.set(x, y ,z);
        changed = true;
    }

    public void offsetTarget(Vector3f offset) {
        Vector3f.add(target, offset, target);
        changed = true;
    }

    public void offsetTarget(float offX, float offY, float offZ) {
        Vector3f offset = new Vector3f(offX, offY, offZ);
        Vector3f.add(target, offset, target);
        changed = true;
    }

    public Vector3f getTarget() {
        return target;
    }

    /**
     * Sets the camera's position to the position (target + offset).
     * @param offset The offset to add to the target
     */
    public void setPositionFromTarget(Vector3f offset) {
        Vector3f.add(this.target, offset, this.position);
    }

    private void lookAt(Vector3f eye, Vector3f center, Vector3f up) {

        Vector3f temp = new Vector3f();
        Vector3f f = new Vector3f();
        Vector3f u = new Vector3f();
        Vector3f s =new Vector3f();

        Vector3f.sub(center, eye, temp);
        temp.normalise(f);
        up.normalise(u);
        Vector3f.cross(f, u, temp);
        temp.normalise(s);
        Vector3f.cross(s, f, u);

        this.matrix.setIdentity();
        matrix.m00 = s.x;
        matrix.m10 = s.y;
        matrix.m20 = s.z;
        matrix.m01 = u.x;
        matrix.m11 = u.y;
        matrix.m21 = u.z;
        matrix.m02 = -f.x;
        matrix.m12 = -f.y;
        matrix.m22 = -f.z;

        matrix.translate(new Vector3f(-eye.x,-eye.y,-eye.z));
    }

    public Matrix4f getMatrix() {
        if (changed) {
            this.lookAt(position, target, new Vector3f(0.0f, 1.0f, 0.0f));
            /*
            matrix.setIdentity();

            Vector2f referenceAxis = new Vector2f(0.0f, -1.0f);
            Vector2f target2d = new Vector2f(target.getX() - position.getX(), target.getZ());
            float angleToRotate = Vector2f.angle(referenceAxis, target2d);

            //Rotate around X
            target2d.set(target.getY() - position.getY(), target.getZ());
            angleToRotate = Vector2f.angle(referenceAxis, target2d);
            if (target2d.getX() < 0.0f)
                angleToRotate *= -1.0f;
            matrix.rotate(angleToRotate, new Vector3f(1.0f, 0.0f, 0.0f));

            //Rotate around Y
            referenceAxis = new Vector2f(0.0f, -1.0f);
            target2d = new Vector2f(target.getX() - position.getX(), target.getZ());
            angleToRotate = Vector2f.angle(referenceAxis, target2d);
            if (target2d.getX() < 0.0f)
                angleToRotate *= -1.0f;
            matrix.rotate(angleToRotate, new Vector3f(0.0f, 1.0f, 0.0f));

            matrix.translate(position);
            */
            changed = false;
        }

        return matrix;
    }
}
