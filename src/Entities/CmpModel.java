package Entities;

import java.io.Serializable;

/**
 * Stores information about a model, not the model data.
 */
public class CmpModel extends Component implements Serializable{

    public static int type = ComponentType.MODEL;
    public String modelPath;

    public CmpModel(String path) {
        modelPath = path;
    }

    public int getType() {
        return type;
    }

}
