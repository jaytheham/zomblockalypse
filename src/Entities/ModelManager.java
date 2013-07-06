package Entities;

import Utilities.Model;
import org.lwjgl.opengl.GL15;

import java.util.HashMap;

/**
 * ModelManager holds all loaded models and makes sure only a single
 * copy of any model is loaded.
 */
public class ModelManager {

    private HashMap<String, Model> models;

    public ModelManager() {
        models = new HashMap<String, Model>();
    }

    public void loadModel(String path) {
        Model m;
        if (models.containsKey(path)) {
            m = models.get(path);
        }
        else {
            m = new Model(path);
            models.put(path, m);
        }

        m.numUsers += 1;
    }

    public Model getModel(String path) {
        return models.get(path);
    }

    /**
     * Remove a model from the loaded set.
     * Does any necessary cleanup - deleting vertex buffers ...
     * @param path The path of the model to remove.
     */
    public void removeModel(String path) {
        if (models.containsKey(path)) {
            models.get(path).numUsers -= 1;
            if (models.get(path).numUsers < 1) {
                GL15.glDeleteBuffers(models.get(path).vbo);
                models.remove(path);
            }
        }
    }
}
