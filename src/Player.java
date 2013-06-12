import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class Player implements Collidable{

    private Vector3f position;
    private Vector3f nextPosition;
    private Vector3f forwardsVector;
    private Vector3f rightVector;
    private float rotation; //0 degrees = -z axis
    private float velocity;

    public final int STEP = 1;
    public final int CLIMB = 6;

    private Vector3f boundingBoxSize;

    private final float MOVE_UNITS_PER_SECOND = 12.0f;
    private final float ACCELERATION = 0.02f;
    private final float DECELERATION = 0.05f;

    private Vector3f moveDirection;

    private int vbo;
    private FloatBuffer matrixBuffer;

    public Player() {
        position = new Vector3f(2.0f, 1.0f, 2.0f);
        nextPosition = new Vector3f(2.0f, 1.0f, 2.0f);
        forwardsVector = new Vector3f(0,0,0);
        rightVector = new Vector3f(0,0,0);
        rotation = 0.0f;
        velocity = 0.0f;
        moveDirection = new Vector3f(0.0f, 0.0f, 0.0f);
        boundingBoxSize = new Vector3f(1.0f, 5.0f, 1.0f);

        vbo = GL15.glGenBuffers();
        
        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(30);
        matrixBuffer = BufferUtils.createFloatBuffer(16);
        
        int i = 0;
        
        vertBuf.put(0.0f);
        vertBuf.put(0.0f);
        vertBuf.put(0.0f);

        vertBuf.put(0.0f);
        vertBuf.put(5.0f);
        vertBuf.put(0.0f);


        vertBuf.put(0.5f);
        vertBuf.put(0.0f);
        vertBuf.put(0.5f);

        vertBuf.put(-0.5f);
        vertBuf.put(0.0f);
        vertBuf.put(-0.5f);

        vertBuf.put(-0.5f);
        vertBuf.put(0.0f);
        vertBuf.put(0.5f);

        vertBuf.put(0.5f);
        vertBuf.put(0.0f);
        vertBuf.put(-0.5f);


        vertBuf.put(0.0f);
        vertBuf.put(1.0f);
        vertBuf.put(0.0f);

        vertBuf.put(0.0f);
        vertBuf.put(1.0f);
        vertBuf.put(-0.5f);

        vertBuf.put(0.0f);
        vertBuf.put(3.0f);
        vertBuf.put(0.0f);

        vertBuf.put(0.0f);
        vertBuf.put(3.0f);
        vertBuf.put(-0.5f);

        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void setMoveDirection(Vector3f forwards, Vector3f right) {
        forwardsVector = forwards;
        rightVector = right;
    }

    public void move(int timeDelta) {
        float destinationAngle = -1.0f;

        moveDirection.set(0.0f, 0.01f, 0.0f);

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            moveDirection = forwardsVector;
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            forwardsVector.negate(moveDirection);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            Vector3f.add(rightVector, moveDirection, moveDirection);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            rightVector.negate();
            Vector3f.add(rightVector, moveDirection, moveDirection);
        }

        moveDirection.normalise();

        if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D) ||
                Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S)) {
            velocity += ACCELERATION;
            if (velocity > 1.0f) {
                velocity = 1.0f;
            }
        }
        else if (velocity > 0.0f) {
            velocity -= DECELERATION;
        }
        else {
            velocity = 0.0f;
        }

        moveDirection.scale(velocity * (MOVE_UNITS_PER_SECOND * (timeDelta / 1000.0f)));

        this.nextPosition.x = this.position.x;
        this.nextPosition.y = this.position.y - 0.1f; //Gravity
        this.nextPosition.z = this.position.z;

        this.nextPosition.x += moveDirection.x;// * (MOVE_UNITS_PER_SECOND * (timeDelta / 1000.0f));
        this.nextPosition.z += moveDirection.z;// * (MOVE_UNITS_PER_SECOND * (timeDelta / 1000.0f));

    }

    // This should be a function of the angle at which the collision took place
    public void collided() {
        velocity -= DECELERATION;
    }

    public void setPosition(Vector3f newPosition) {
        position.x = newPosition.getX();
        position.y = newPosition.getY();
        position.z = newPosition.getZ();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(Vector3f pos) {
        nextPosition.x = pos.getX();
        nextPosition.y = pos.getY();
        nextPosition.z = pos.getZ();
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getZ() {
        return position.z;
    }

    public Vector3f getBoundingBoxSize() {
        return boundingBoxSize;
    }

    public void render(int programId, int uniformMatrixId, Matrix4f perspectiveMatrix) {

        Matrix4f worldMatrix = new Matrix4f();

        worldMatrix.translate(position);
        worldMatrix.rotate((float)Math.toRadians((double)-rotation), new Vector3f(0.0f, 1.0f, 0.0f));
        Matrix4f.mul(perspectiveMatrix, worldMatrix, worldMatrix);
        matrixBuffer.position(0);
        worldMatrix.store(matrixBuffer);
        matrixBuffer.flip();

        GL20.glUseProgram(programId);

        GL20.glUniformMatrix4(uniformMatrixId, false, matrixBuffer);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_LINES, 0, 10);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }
}
