package Utilities;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class ShaderUtils {

    /**
     * Load a shader file and compile it.
     */
    public static int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }

        shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        int status = GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS);

        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shader " + filename + " failed to compile!");
            System.exit(-1);
        }

        return shaderID;
    }
}
