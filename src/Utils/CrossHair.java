package Utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

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
    private int programId;

    public CrossHair() {
        vbo = GL15.glGenBuffers();


        FloatBuffer vertBuf = BufferUtils.createFloatBuffer(12);

        vertBuf.put(-0.01f);
        vertBuf.put(0.0f);
        vertBuf.put(0.0f);

        vertBuf.put(0.01f);
        vertBuf.put(0.0f);
        vertBuf.put(0.0f);


        vertBuf.put(0.0f);
        vertBuf.put(0.015f);
        vertBuf.put(0.0f);

        vertBuf.put(0.0f);
        vertBuf.put(-0.015f);
        vertBuf.put(0.0f);

        vertBuf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void setupShader(String vertShader, String fragShader) {
        int vsId = ShaderUtils.loadShader(vertShader, GL20.GL_VERTEX_SHADER);
        int fsId = ShaderUtils.loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vsId);
        GL20.glAttachShader(programId, fsId);

        GL20.glBindAttribLocation(programId, 0, "in_Position");

        GL20.glLinkProgram(programId);

        int status = GL20.glGetShaderi(programId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shaders failed to link!");
            System.exit(-1);
        }

        GL20.glDetachShader(programId, vsId);
        GL20.glDetachShader(programId, fsId);

    }

    public void render() {

        GL20.glUseProgram(programId);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_LINES, 0, 4);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }
}
