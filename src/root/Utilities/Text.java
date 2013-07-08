package root.Utilities;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class Text {

    private static int textureId;
    private static int vbo;
    private static final float TEX_CHAR_WIDTH = 0.03515625f;

    public static void init(String filepath) {
        vbo = GL15.glGenBuffers();
        textureId = GL11.glGenTextures();
        GL13.glActiveTexture(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        root.Utilities.TextureLoader.loadPNG(filepath, GL11.GL_RGBA8);
    }

    /**
     * Draw text 'text' at position x% from the left of the screen, y% from the bottom
     * @param text The text string to draw.
     * @param x The percent from the left of the screen to start the text.
     * @param y The percent from the bottom of the screen to start the text.
     */
    public static void draw(String text, float x, float y) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(text.length() * 6 * 4);

        x = (x - 50.0f) / 50.0f;
        y = (y - 50.0f) / 50.0f;
        float charSize = 30.0f / Display.getHeight();
        float charWidth = charSize / 2;

        char c = ' ';
        float u;
        float v;
        for (int i = 0; i < text.length(); i ++) {

            c = text.charAt(i);

            switch (c) {
                case 'A':
                    u = 0.0f;
                    v = 0.0f;
                    break;
                case 'B':
                    u = 1 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'C':
                    u = 2 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'D':
                    u = 3 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'E':
                    u = 4 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'F':
                    u = 5 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'G':
                    u = 6 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'H':
                    u = 7 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'I':
                    u = 8 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'J':
                    u = 9 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'K':
                    u = 10 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'L':
                    u = 11 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'M':
                    u = 12 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'N':
                    u = 13 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'O':
                    u = 14 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'P':
                    u = 15 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'Q':
                    u = 16 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'R':
                    u = 17 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'S':
                    u = 18 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'T':
                    u = 19 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'U':
                    u = 20 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'V':
                    u = 21 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'W':
                    u = 22 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'X':
                    u = 23 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'Y':
                    u = 24 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case 'Z':
                    u = 25 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case ' ':
                    u = 26 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case '-':
                    u = 27 * TEX_CHAR_WIDTH;
                    v = 0.0f;
                    break;
                case '0':
                    u = 0.0f;
                    v = 0.5f;
                    break;
                case '1':
                    u = 1 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '2':
                    u = 2 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '3':
                    u = 3 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '4':
                    u = 4 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '5':
                    u = 5 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '6':
                    u = 6 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '7':
                    u = 7 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '8':
                    u = 8 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '9':
                    u = 9 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case ':':
                    u = 10 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                case '.':
                    u = 11 * TEX_CHAR_WIDTH;
                    v = 0.5f;
                    break;
                default:
                    u = 0.0f;
                    v = 0.5f;
                    break;
            }

            buf.put(x + i * charWidth);
            buf.put(y + charSize);
            buf.put(u);
            buf.put(v);

            buf.put(x + i * charWidth);
            buf.put(y);
            buf.put(u);
            buf.put(v + 0.25f);

            buf.put(x + i * charWidth + charWidth);
            buf.put(y);
            buf.put(u + TEX_CHAR_WIDTH);
            buf.put(v + 0.25f);


            buf.put(x + i * charWidth);
            buf.put(y + charSize);
            buf.put(u);
            buf.put(v);

            buf.put(x + i * charWidth + charWidth);
            buf.put(y);
            buf.put(u + TEX_CHAR_WIDTH);
            buf.put(v + 0.25f);

            buf.put(x + i * charWidth + charWidth);
            buf.put(y + charSize);
            buf.put(u + TEX_CHAR_WIDTH);
            buf.put(v);
        }


        buf.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STREAM_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        render(text.length());
    }

    private static void render(int numChars) {

        GL20.glUseProgram(ShaderProgram.getHudProgramId());

        GL13.glActiveTexture(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL20.glUniform1i(ShaderProgram.getHudUTextureId(), 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 16, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 16, 8);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numChars * 6);

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
