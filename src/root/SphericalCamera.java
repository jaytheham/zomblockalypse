package root;

import root.Utilities.Constants;
import root.Utilities.Settings;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class SphericalCamera {

    protected double pitch;
    protected double yaw;
    protected Vector3f position;
    private Matrix4f matrix;

    protected int vbo;

    private float moveSpeed = 0.05f;
    protected float rotationSpeed = 90.0f;

    public SphericalCamera() {
        pitch = 0.0;
        yaw = 0.0;
        position = new Vector3f(0,0,0);
        matrix = new Matrix4f();
        vbo = GL15.glGenBuffers();
    }

    public SphericalCamera(Vector3f p) {
        pitch = 0.0;
        yaw = 0.0;
        position = p;
        matrix = new Matrix4f();
        vbo = GL15.glGenBuffers();
    }

    public void setPosition(Vector3f p) {
        position = p;
    }

    public void changeYaw(float change) {
        yaw += change;

        if (yaw > 360.0)
            yaw %= 360.0;
        else if (yaw < 0.0) {
            yaw += 360.0;
        }
    }

    public void changePitch(float change) {
        pitch -= change;

        if (pitch > 90.0)
            pitch = 90.0;
        else if (pitch < -90.0) {
            pitch = -90.0;
        }
    }

    public void moveCamera(float xChange, float yChange, int timeDelta) {
        calculateMatrix();

        Vector3f lVector = new Vector3f(matrix.m00, matrix.m10, matrix.m20);
        Vector3f uVector = new Vector3f(matrix.m01, matrix.m11, matrix.m21);
        Vector3f fVector = new Vector3f(matrix.m02, matrix.m12, matrix.m22);

        lVector.normalise(lVector);
        uVector.normalise(uVector);
        fVector.normalise(fVector);

        lVector.scale(0.24f);
        uVector.scale(0.24f);
        fVector.scale(0.24f);

        if (Mouse.isButtonDown(0)) {
            changeYaw(rotationSpeed * xChange);
            changePitch(rotationSpeed * yChange);
        }

        if (Keyboard.isKeyDown(Settings.fpsCameraRight)) {
            lVector.negate();
            Vector3f.add(position, lVector, position);
        }
        else if (Keyboard.isKeyDown(Settings.fpsCameraLeft)) {
            Vector3f.add(position, lVector, position);
        }

        if (Keyboard.isKeyDown(Settings.fpsCameraForwards)) {
            Vector3f.add(position, fVector, position);
        }
        else if (Keyboard.isKeyDown(Settings.fpsCameraBackwards)) {
            fVector.negate();
            Vector3f.add(position, fVector, position);
        }

        if (Keyboard.isKeyDown(Settings.keyboardUp)) {
            Vector3f.add(position, uVector, position);
        }
        else if (Keyboard.isKeyDown(Settings.keyboardDown)) {
            uVector.negate();
            Vector3f.add(position, uVector, position);
        }
    }

    public Vector3f getForwardsVector() {
        calculateMatrix();

        Vector3f fVector = new Vector3f(matrix.m02, matrix.m12, matrix.m22);
        fVector.normalise();
        fVector.negate();

        return fVector;
    }

    public Vector3f getUpVector() {
        calculateMatrix();

        Vector3f up = new Vector3f(matrix.m01, matrix.m11, matrix.m21);
        up.normalise();
        return up;
    }

    public Vector3f getRightVector() {
        calculateMatrix();

        Vector3f right = new Vector3f(matrix.m00, matrix.m10, matrix.m20);
        right.normalise();
        return right;
    }

    public Vector3f getPosition() {
        Vector3f pVector = new Vector3f(position);
        pVector.negate();

        return pVector;
    }

    public Vector3f getRayToMousePosition() {
        return getForwardsVector();
    }

    private void calculateMatrix() {
        matrix.setIdentity();
        matrix.rotate((float)Math.toRadians(pitch), new Vector3f(1,0,0));
        matrix.rotate((float)Math.toRadians(yaw), new Vector3f(0,1,0));
        matrix.translate(position);
    }

    public Matrix4f getMatrix() {
        calculateMatrix();
        return matrix;
    }

    public void renderTargetBlock(int pId, int uId, Matrix4f perspectiveMatrix) {

        try {
            int[] blockPos = RayCaster.getIntercept(getPosition(), getForwardsVector(), Constants.MAX_PICK_DISTANCE);

            blockPos[0] += blockPos[3];
            blockPos[1] += blockPos[4];
            blockPos[2] += blockPos[5];

            FloatBuffer vertBuf = BufferUtils.createFloatBuffer(12);
            FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

            float shift = 0.98f;

            if (blockPos[3] != 0) { // xFace intersection
                if (blockPos[3] > 0)
                    shift = 0.02f;

                vertBuf.put(blockPos[0] + shift);
                vertBuf.put((float)blockPos[1]);
                vertBuf.put((float)blockPos[2]);

                vertBuf.put(blockPos[0] + shift);
                vertBuf.put((float)blockPos[1]);
                vertBuf.put(blockPos[2] + 1.0f);

                vertBuf.put(blockPos[0] + shift);
                vertBuf.put(blockPos[1] + 1.0f);
                vertBuf.put(blockPos[2] + 1.0f);

                vertBuf.put(blockPos[0] + shift);
                vertBuf.put((float)blockPos[1] + 1.0f);
                vertBuf.put((float)blockPos[2]);
            }
            else if (blockPos[4] != 0) { // yFace intersection
                if (blockPos[4] > 0)
                    shift = 0.02f;

                vertBuf.put((float)blockPos[0]);
                vertBuf.put(blockPos[1] + shift);
                vertBuf.put((float)blockPos[2]);

                vertBuf.put(blockPos[0] + 1.0f);
                vertBuf.put(blockPos[1] + shift);
                vertBuf.put((float)blockPos[2]);

                vertBuf.put(blockPos[0] + 1.0f);
                vertBuf.put(blockPos[1] + shift);
                vertBuf.put(blockPos[2] + 1.0f);

                vertBuf.put((float)blockPos[0]);
                vertBuf.put(blockPos[1] + shift);
                vertBuf.put(blockPos[2] + 1.0f);
            }
            else {  // zFace intersection
                if (blockPos[5] > 0)
                    shift = 0.02f;

                vertBuf.put((float)blockPos[0]);
                vertBuf.put((float)blockPos[1]);
                vertBuf.put(blockPos[2] + shift);

                vertBuf.put(blockPos[0] + 1.0f);
                vertBuf.put((float)blockPos[1]);
                vertBuf.put(blockPos[2] + shift);

                vertBuf.put(blockPos[0] + 1.0f);
                vertBuf.put(blockPos[1] + 1.0f);
                vertBuf.put(blockPos[2] + shift);

                vertBuf.put((float)blockPos[0]);
                vertBuf.put(blockPos[1] + 1.0f);
                vertBuf.put(blockPos[2] + shift);
            }

            vertBuf.flip();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_DYNAMIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            perspectiveMatrix.store(matrixBuffer);
            matrixBuffer.flip();

            GL20.glUseProgram(pId);

            GL20.glUniformMatrix4(uId, false, matrixBuffer);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

            GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, 4);

            GL20.glDisableVertexAttribArray(0);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL20.glUseProgram(0);
        }
        catch (Exception e) {
            //System.out.println(e.getMessage());
        }
    }
}
