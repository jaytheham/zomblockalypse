package root.Entities;

import java.io.Serializable;

/**
 * Stores information about a model, not the model data.
 */
public class CmpModel extends Component implements Serializable{

    public String modelPath;

    public CmpModel(String path) {
        type = ComponentType.MODEL;
        modelPath = path;
    }

}
