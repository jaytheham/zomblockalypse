package root;

import root.Utilities.Constants;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;


public class Zombie implements Collidable{

    private static final float VIEW_DISTANCE = 64.0f;
    private static final float VIEW_ALERT_ANGLE = 90.0f / 2;
    private static final float VIEW_INTEREST_ANGLE = 180.0f / 2;

    private int step = 1;
    private int climb = 3;

    private Vector3f position;
    private Vector3f nextPosition;
    private Vector3f targetPosition;
    private float yaw;
    private float velocity;
    private float unitsPerSecond;

    private boolean alerted;

    private int vbo;
    private FloatBuffer matrixBuffer;


    public Zombie(Vector3f p) {
        position = p;
        nextPosition = p;
        targetPosition = new Vector3f();
        velocity = 0.0f;
        unitsPerSecond = 4.0f;
        yaw = 0.0f;
        alerted = false;

        //---------

        vbo = GL15.glGenBuffers();

        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(18);
        matrixBuffer = BufferUtils.createFloatBuffer(16);

        vertBuf.put(0.0f);
        vertBuf.put(0.0f);
        vertBuf.put(0.0f);

        vertBuf.put(0.0f);
        vertBuf.put(5.0f);
        vertBuf.put(0.0f);


        vertBuf.put(-0.5f);
        vertBuf.put(4.0f);
        vertBuf.put(-0.5f);

        vertBuf.put(0.0f);
        vertBuf.put(4.0f);
        vertBuf.put(0.0f);


        vertBuf.put(0.5f);
        vertBuf.put(4.0f);
        vertBuf.put(-0.5f);

        vertBuf.put(0.0f);
        vertBuf.put(4.0f);
        vertBuf.put(0.0f);

        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void update(Vector3f playerPos, int timeDelta) {
        if (alerted) {
            searchForTarget(playerPos);
            Vector3f.add(position, movement(timeDelta), nextPosition);

            if (Math.abs(targetPosition.x - position.x) < 0.3f
                    && Math.abs(targetPosition.y - position.y) < 0.3f
                    && Math.abs(targetPosition.z - position.z) < 0.3f) {
                alerted = false;
                velocity = 0.0f;
            }
        }
        else {
            searchForTarget(playerPos);
        }
    }

    private void searchForTarget(Vector3f playerPos) {

        Vector3f direction = new Vector3f();
        Vector3f.sub(playerPos, position, direction);
        Vector3f facingDirection = new Vector3f((float)Math.sin(Math.toRadians(yaw)),
                                                0.0f,
                                                -(float)Math.cos(Math.toRadians(yaw)));

        float distance = direction.length();

        // If within alert viewing angle and visible distance
        if (Math.toDegrees(Vector3f.angle(direction, facingDirection)) <= VIEW_ALERT_ANGLE
                && direction.length() <= VIEW_DISTANCE) {


            direction.normalise();
            // Need to add eye height to position
            //
            int[] result = RayCaster.getIntercept(position, direction, distance);

            if (result == null) {
                alerted = true;
                targetPosition.x = playerPos.x;
                targetPosition.y = playerPos.y;
                targetPosition.z = playerPos.z;
            }
        }
        // If within interest viewing angle and visible distance
        // Or within half a metre
        else if (Math.toDegrees(Vector3f.angle(direction, facingDirection)) <= VIEW_INTEREST_ANGLE
                && direction.length() <= VIEW_DISTANCE
                || distance < Constants.ONE_METRE_IN_UNITS / 2) {

            int[] result = RayCaster.getIntercept(position, direction, distance);
            if (result == null) {
                targetPosition.x = playerPos.x;
                targetPosition.y = playerPos.y;
                targetPosition.z = playerPos.z;
                Vector3f d = new Vector3f();
                Vector3f.sub(targetPosition, position, d);
                doRotation(d, 0.02f);
            }
        }
    }

    /**
     * Calculate how far and in which direction to try move this frame.
     * @param timeDelta Number of milliseconds since the last frame
     * @return A vector containing the desired next position
     */
    private Vector3f movement(int timeDelta) {
        if (velocity < 1.0f)
            velocity += 0.05f;

        Vector3f direction = new Vector3f();
        Vector3f.sub(targetPosition, position, direction);

        doRotation(direction, 0.1f);

        direction.normalise();
        direction.scale(unitsPerSecond * velocity * (timeDelta / 1000.0f));

        return direction;
    }

    private void doRotation(Vector3f directionOfTarget, float scale) {

        Vector2f facingDirection = new Vector2f((float)Math.sin(Math.toRadians(yaw)),
                -(float)Math.cos(Math.toRadians(yaw)));
        Vector2f targetDirection = new Vector2f(directionOfTarget.x, directionOfTarget.z);

        float angleToTarget = (float)Math.toDegrees(Vector2f.angle(facingDirection, targetDirection));
        if (angleToTarget > 0.5f) {
            facingDirection.x += position.x;
            facingDirection.y += position.z;
            if ((facingDirection.x - position.x)*(targetPosition.z - position.z) -
                    (facingDirection.y - position.z)*(targetPosition.x - position.x) < 0) {
                angleToTarget = -angleToTarget;
            }

            angleToTarget *= scale;
        }

        yaw += angleToTarget;

        if (yaw > 360.0f)
            yaw -= 360.0f;
        else if (yaw < 0.0f)
            yaw += 360.0f;
    }

    public void render(int programId, int uniformMatrixId, Matrix4f perspectiveMatrix) {

        Matrix4f worldMatrix = new Matrix4f();

        worldMatrix.translate(position);
        worldMatrix.rotate((float)Math.toRadians((double)-yaw), new Vector3f(0.0f, 1.0f, 0.0f));
        Matrix4f.mul(perspectiveMatrix, worldMatrix, worldMatrix);
        matrixBuffer.position(0);
        worldMatrix.store(matrixBuffer);
        matrixBuffer.flip();

        GL20.glUseProgram(programId);

        GL20.glUniformMatrix4(uniformMatrixId, false, matrixBuffer);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_LINES, 0, 6);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    @Override
    public Vector3f getNextPosition() {
        return new Vector3f(nextPosition);
    }

    @Override
    public void setPosition(Vector3f newPosition) {
        position = newPosition;
    }

    @Override
    public Vector3f getBoundingBoxSize() {
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    @Override
    public void collided() {
        velocity /= 2;
    }

    @Override
    public int getStep() {
        return step;
    }

    @Override
    public int getClimb() {
        return climb;
    }
}
