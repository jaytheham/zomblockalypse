package Utilities;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TextureLoader {

        public static void loadPNG(String filepath, int format) {

            ByteBuffer buf = null;
            int tWidth = 0;

            int tHeight = 0;
            try {
                // Open the PNG file as an InputStream
                InputStream in = new FileInputStream(filepath);
                // Link the PNG decoder to this stream
                PNGDecoder decoder = new PNGDecoder(in);

                // Get the width and height of the texture
                tWidth = decoder.getWidth();
                tHeight = decoder.getHeight();


                // Decode the PNG file in a ByteBuffer
                buf = ByteBuffer.allocateDirect(
                        4 * decoder.getWidth() * decoder.getHeight());
                decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.BGRA);
                buf.flip();

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, tWidth, tHeight, 0,
                    GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);
        }
}
