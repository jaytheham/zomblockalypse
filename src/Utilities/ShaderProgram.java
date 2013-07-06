package Utilities;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderProgram {

    private static int hudProgramId;
    private static int hudUTextureId;
    private static int hudAttribPosition;
    private static int hudAttribTexCoord;

    private static int defaultProgramId;
    private static int defaultUTransformMtxId;

    public ShaderProgram() {
        setupHUDShader("Shaders/HudVertex.glsl", "Shaders/HudFragment.glsl");
        setupDefaultShaders("Shaders/DefaultVertex.glsl", "Shaders/DefaultFragment.glsl");
    }

    private void setupHUDShader(String vertShader, String fragShader) {
        int vsId = ShaderUtils.loadShader(vertShader, GL20.GL_VERTEX_SHADER);
        int fsId = ShaderUtils.loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);

        hudProgramId = GL20.glCreateProgram();
        GL20.glAttachShader(hudProgramId, vsId);
        GL20.glAttachShader(hudProgramId, fsId);

        GL20.glLinkProgram(hudProgramId);

        int status = GL20.glGetShaderi(hudProgramId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shaders failed to link!");
            System.exit(-1);
        }

        hudAttribPosition = GL20.glGetAttribLocation(hudProgramId, "in_Position");
        hudAttribTexCoord = GL20.glGetAttribLocation(hudProgramId, "in_TexCoord");

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

    private void setupDefaultShaders(String vertShader, String fragShader) {
        int vsId = ShaderUtils.loadShader(vertShader, GL20.GL_VERTEX_SHADER);
        int fsId = ShaderUtils.loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);

        defaultProgramId = GL20.glCreateProgram();
        GL20.glAttachShader(defaultProgramId, vsId);
        GL20.glAttachShader(defaultProgramId, fsId);

        GL20.glBindAttribLocation(defaultProgramId, 0, "in_Position");

        GL20.glLinkProgram(defaultProgramId);

        int status = GL20.glGetShaderi(defaultProgramId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shaders failed to link!");
            System.exit(-1);
        }

        defaultUTransformMtxId = GL20.glGetUniformLocation(defaultProgramId, "transformMatrix");

        GL20.glDetachShader(defaultProgramId, vsId);
        GL20.glDetachShader(defaultProgramId, fsId);
    }

    public static int getDefaultProgramId() {
        return defaultProgramId;
    }

    public static int getDefaultTransformMatrixId() {
        return defaultUTransformMtxId;
    }
}
