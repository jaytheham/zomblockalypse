package root.Utilities;

import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.FileReader;

public class ModelLoader {
    public static void loadModel(String path, Model model) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));

            String line = br.readLine();
            int numVertices = 0;

            while (!line.startsWith("v ")) {
                br.mark(5000);
                line = br.readLine();
            }

            while (line.startsWith("v ")) {
                line = br.readLine();
                numVertices++;
            }

            model.vertices = BufferUtils.createFloatBuffer(numVertices * 3);
            br.reset();
            String[] verts;
            for (int i = 0; i < numVertices; i++) {
                verts = br.readLine().split(" ");
                model.vertices.put(Float.parseFloat(verts[1]));
                model.vertices.put(Float.parseFloat(verts[2]));
                model.vertices.put(Float.parseFloat(verts[3]));
            }
            model.vertices.flip();

            int numTriangles = 0;
            while (!line.startsWith("f ")) {
                br.mark(5000);
                line = br.readLine();
            }

            while (line != null && line.startsWith("f ")) {
                line = br.readLine();
                numTriangles++;
            }
            br.reset();
            model.indices = BufferUtils.createIntBuffer(numTriangles * 3);
            for (int i = 0; i < numTriangles; i++) {
                verts = br.readLine().split(" ");
                model.indices.put(Integer.parseInt(verts[1]) -1);
                model.indices.put(Integer.parseInt(verts[2]) -1);
                model.indices.put(Integer.parseInt(verts[3]) -1);
            }
            model.indices.flip();

        } catch (Exception e) {
            System.out.println("Error opening file: " + path);
        }
    }
}
