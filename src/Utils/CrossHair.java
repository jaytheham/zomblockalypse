package Utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Jay
 * Date: 7/06/13
 * Time: 9:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrossHair {

    private int vbo;
    private FloatBuffer matBuf;

    public CrossHair() {
        vbo = GL15.glGenBuffers();


        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(12);

        int i = 0;

        vertBuf.put(-0.01f);
        vertBuf.put(0.0f);
        vertBuf.put(0.6f);

        vertBuf.put(0.01f);
        vertBuf.put(0.0f);
        vertBuf.put(0.6f);


        vertBuf.put(0.0f);
        vertBuf.put(0.01f);
        vertBuf.put(0.6f);

        vertBuf.put(0.0f);
        vertBuf.put(-0.01f);
        vertBuf.put(0.6f);

        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render(int programId, int uniformMatrixId) {

        FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matBuf.position(0);
        matrix.store(matBuf);
        matBuf.flip();
        GL20.glUseProgram(programId);

        GL20.glUniformMatrix4(uniformMatrixId, false, matBuf);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_LINE, 0, 4);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }
}
