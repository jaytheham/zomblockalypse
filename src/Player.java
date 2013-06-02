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

    private Vector3f boundingBoxSize;

    private final float MOVE_UNITS_PER_SECOND = 12.0f;
    private final float ACCELERATION = 0.05f;
    private final float DECELERATION = 0.75f;
    private final float SINGLE_AXIS_DECELERATION = 0.20f;

    private Vector3f moveDirection;
    private float speed;

    private int vbo;
    private FloatBuffer matrixBuffer;

    public Player() {
        position = new Vector3f(0.0f, 2.0f, 0.0f);
        nextPosition = new Vector3f(0.0f, 0.0f, 0.0f);
        moveDirection = new Vector3f(0.0f, 0.0f, 0.0f);
        boundingBoxSize = new Vector3f(1.0f, 5.0f, 1.0f);

        vbo = GL15.glGenBuffers();
        
        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(36);
        matrixBuffer = BufferUtils.createFloatBuffer(16);
        
        int i = 0;
        
        vertBuf.put(-0.5f);
        vertBuf.put(0.0f);
        vertBuf.put(0.5f);

        vertBuf.put(0.5f);
        vertBuf.put(0.0f);
        vertBuf.put(0.5f);

        vertBuf.put(0.5f);
        vertBuf.put(5.0f);
        vertBuf.put(0.5f);


        vertBuf.put(-0.5f);
        vertBuf.put(0.0f);
        vertBuf.put(0.5f);

        vertBuf.put(0.5f);
        vertBuf.put(5.0f);
        vertBuf.put(0.5f);

        vertBuf.put(-0.5f);
        vertBuf.put(5.0f);
        vertBuf.put(0.5f);


        vertBuf.put(-0.5f);
        vertBuf.put(0.1f);
        vertBuf.put(0.5f);

        vertBuf.put(0.5f);
        vertBuf.put(0.1f);
        vertBuf.put(0.5f);

        vertBuf.put(0.5f);
        vertBuf.put(0.1f);
        vertBuf.put(-0.5f);

        vertBuf.put(-0.5f);
        vertBuf.put(0.1f);
        vertBuf.put(0.5f);

        vertBuf.put(0.5f);
        vertBuf.put(0.1f);
        vertBuf.put(-0.5f);

        vertBuf.put(-0.5f);
        vertBuf.put(0.1f);
        vertBuf.put(-0.5f);

        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void move(int timeDelta) {
        if (Keyboard.isKeyDown(Keyboard.KEY_A) && this.moveDirection.x > -1.0f) {
            this.moveDirection.x -= ACCELERATION;
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_D) && this.moveDirection.x < 1.0f) {
            this.moveDirection.x += ACCELERATION;
        }
        else {
            if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S))
                this.moveDirection.x *= (DECELERATION + SINGLE_AXIS_DECELERATION);
            else
                this.moveDirection.x *= DECELERATION;
            if (Math.abs(this.moveDirection.x) < 0.001)
                this.moveDirection.x = 0.0f;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_W) && this.moveDirection.z > -1.0f) {
            this.moveDirection.z -= ACCELERATION;
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_S) && this.moveDirection.z < 1.0f) {
            this.moveDirection.z += ACCELERATION;
        }
        else {
            if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D))
                this.moveDirection.z *= (DECELERATION + SINGLE_AXIS_DECELERATION);
            else
                this.moveDirection.z *= DECELERATION;
            if (Math.abs(this.moveDirection.z) < 0.001)
                this.moveDirection.z = 0.0f;
        }

        if (Math.abs(this.moveDirection.x) + Math.abs(this.moveDirection.z) > 1.45f)
            this.moveDirection.normalise(this.moveDirection);

        //this.moveDirection.y -= ACCELERATION;wdwdwd


        this.nextPosition.x += moveDirection.x * (MOVE_UNITS_PER_SECOND * (timeDelta / 1000.0f));
        this.nextPosition.z += moveDirection.z * (MOVE_UNITS_PER_SECOND * (timeDelta / 1000.0f));
        this.nextPosition.y = this.position.y;
    }

    public void setPosition(Vector3f newPosition) {
        position = newPosition;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getNextPosition() {
        return  nextPosition;
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
        Matrix4f.mul(perspectiveMatrix, worldMatrix, worldMatrix);
        matrixBuffer.position(0);
        worldMatrix.store(matrixBuffer);
        matrixBuffer.flip();

        GL20.glUseProgram(programId);

        GL20.glUniformMatrix4(uniformMatrixId, false, matrixBuffer);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, 12);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }
}
