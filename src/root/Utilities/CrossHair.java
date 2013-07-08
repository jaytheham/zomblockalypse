package root.Utilities;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class CrossHair {

    private int vbo;

    public CrossHair() {
        vbo = GL15.glGenBuffers();


        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(8);

        vertBuf.put(-0.01f);
        vertBuf.put(0.0f);

        vertBuf.put(0.01f);
        vertBuf.put(0.0f);


        vertBuf.put(0.0f);
        vertBuf.put(0.015f);

        vertBuf.put(0.0f);
        vertBuf.put(-0.015f);

        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render(int programId) {

        GL20.glUseProgram(programId);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_LINES, 0, 4);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }
}
