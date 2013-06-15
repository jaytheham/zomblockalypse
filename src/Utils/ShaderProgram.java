package Utils;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderProgram {

    private static int hudProgramId;
    private static int hudUTextureId;

    public ShaderProgram() {
        setupHUDShader("Shaders/HudVertex.glsl", "Shaders/HudFragment.glsl");
    }

    private void setupHUDShader(String vertShader, String fragShader) {
        int vsId = ShaderUtils.loadShader(vertShader, GL20.GL_VERTEX_SHADER);
        int fsId = ShaderUtils.loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);

        hudProgramId = GL20.glCreateProgram();
        GL20.glAttachShader(hudProgramId, vsId);
        GL20.glAttachShader(hudProgramId, fsId);

        GL20.glBindAttribLocation(hudProgramId, 0, "in_Position");
        GL20.glBindAttribLocation(hudProgramId, 1, "in_TexCoord");

        GL20.glLinkProgram(hudProgramId);

        int status = GL20.glGetShaderi(hudProgramId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shaders failed to link!");
            System.exit(-1);
        }

        hudUTextureId = GL20.glGetUniformLocation(hudProgramId, "uTexture");

        GL20.glDetachShader(hudProgramId, vsId);
        GL20.glDetachShader(hudProgramId, fsId);

    }

    public static int getHudProgramId() {
        return hudProgramId;
    }

    public static int getHudUTextureId() {
        return hudUTextureId;
    }
}
