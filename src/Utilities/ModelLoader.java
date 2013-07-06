package Utilities;

import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.FileReader;

public class ModelLoader {
    public static void loadModel(String path, Model model) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = "";
            int numVertices = Integer.decode(br.readLine());
            model.vertices = BufferUtils.createFloatBuffer(numVertices * 3);
            String[] verts;
            for (int i = 0; i < numVertices; i++) {
                verts = br.readLine().split(",");
                model.vertices.put(Float.parseFloat(verts[0]));
                model.vertices.put(Float.parseFloat(verts[1]));
                model.vertices.put(Float.parseFloat(verts[2]));
            }
            model.vertices.flip();

            int numTriangles = Integer.decode(br.readLine());
            model.indices = BufferUtils.createIntBuffer(numTriangles * 3);
            for (int i = 0; i < numTriangles; i++) {
                verts = br.readLine().split(",");
                model.indices.put(Integer.parseInt(verts[0]));
                model.indices.put(Integer.parseInt(verts[1]));
                model.indices.put(Integer.parseInt(verts[2]));
            }
            model.indices.flip();

        } catch (Exception e) {
            System.out.println("Error opening file: " + path);
        }
    }
}
