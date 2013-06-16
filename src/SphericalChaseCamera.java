import Utils.Constants;
import Utils.Settings;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

public class SphericalChaseCamera extends SphericalCamera {
    private Player playerTarget;
    private Vector3f postionVector;
    private double yawAboutTarget;
    private float distanceFromTarget;

    public SphericalChaseCamera(Player p) {
        super();

        playerTarget = p;
        postionVector = new Vector3f(0.0f, -1.0f, -1.0f);
        distanceFromTarget = Constants.CAMERA_MIN_ZOOM;
        yawAboutTarget = 0.0;

        position.x = (postionVector.x * distanceFromTarget) + -playerTarget.getX();
        position.y = (postionVector.y * distanceFromTarget) + -playerTarget.getY();
        position.z = (postionVector.z * distanceFromTarget) + -playerTarget.getZ();

        pitch = (float)Math.toDegrees((double)Vector3f.angle(postionVector, new Vector3f(0,0,-1)));
        postionVector.x = -(float)Math.sin(yawAboutTarget);
        postionVector.z = -(float)Math.cos(yawAboutTarget);
    }

    @Override
    public void moveCamera(float xChange, float yChange, int timeDelta) {

        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            distanceFromTarget -= (dWheel * Settings.mouseScrollSensitivity);
            if (distanceFromTarget > Constants.CAMERA_MAX_ZOOM)
                distanceFromTarget = Constants.CAMERA_MAX_ZOOM;
            else if (distanceFromTarget < Constants.CAMERA_MIN_ZOOM)
                distanceFromTarget = Constants.CAMERA_MIN_ZOOM;
        }

        if (Mouse.isButtonDown(2)) {
            yawAboutTarget += Settings.cameraRotateSensitivity * xChange;
            postionVector.x = -(float)Math.sin(Math.toRadians(yawAboutTarget));
            postionVector.z = -(float)Math.cos(Math.toRadians(yawAboutTarget));

            if (yawAboutTarget > 360.0f)
                yawAboutTarget -= 360.0f;
            else if (yawAboutTarget < 0.0f)
                yawAboutTarget += 360.0f;

            yaw = (float)-yawAboutTarget;


        }

        playerTarget.setMoveDirection(
                new Vector3f(
                        (float)Math.sin(Math.toRadians(360.0 - yawAboutTarget)),
                        0.0f,
                        (float)Math.cos(Math.toRadians(yawAboutTarget + 180.0))),
                new Vector3f(
                        (float)Math.cos(Math.toRadians(yawAboutTarget)),
                        0.0f,
                        (float)Math.sin(Math.toRadians(360.0 - yawAboutTarget))));


        position.x = (postionVector.x * distanceFromTarget) - playerTarget.getX();
        position.y = (postionVector.y * distanceFromTarget) - playerTarget.getY();
        position.z = (postionVector.z * distanceFromTarget) - playerTarget.getZ();
    }

    @Override
    public Vector3f getRayToMousePosition() {
        float x = (2.0f * Mouse.getX()) / Display.getWidth() - 1.0f;
        float y = (2.0f * Mouse.getY()) / Display.getHeight() - 1.0f;

        Vector4f rayViewPort = new Vector4f(x, y, -1.0f, 1.0f);

        Matrix4f workingMatrix = new Matrix4f(Game.projectionMatrix);
        workingMatrix.invert();
        Matrix4f.transform(workingMatrix, rayViewPort, rayViewPort);

        rayViewPort.z = -1.0f;
        rayViewPort.w = 0.0f;

        workingMatrix = getMatrix();
        workingMatrix.invert();

        Matrix4f.transform(workingMatrix, rayViewPort, rayViewPort);

        Vector3f rayWorld = new Vector3f(rayViewPort.x, rayViewPort.y, rayViewPort.z);

        return rayWorld;
    }

    @Override
    public void renderTargetBlock(int pId, int uId, Matrix4f perspectiveMatrix) {

        try {
            int[] blockPos = RayCaster.raycast(getPosition(), getRayToMousePosition(), Constants.MAX_PICK_DISTANCE);

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
