package root.Entities;

import org.lwjgl.util.vector.Vector3f;

import java.io.Serializable;

public class CmpPointLight extends Component implements Serializable {

    public Vector3f color;
    public float intensity;

    /**
     * Constructor.
     * @param c Color (Values in range 0-1)
     * @param i Intensity of light
     */
    public CmpPointLight(Vector3f c, float i) {
        type = ComponentType.POINT_LIGHT;
        color = c;
        intensity = i;
    }

    /**
     * Constructor.
     * @param r Red value of light color (In range 0-1)
     * @param g Green value of light color (In range 0-1)
     * @param b Blue value of light color (In range 0-1)
     * @param i Intensity of light
     */
    public CmpPointLight(float r, float g, float b, float i) {
        this(new Vector3f(r, g, b), i);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }
}
