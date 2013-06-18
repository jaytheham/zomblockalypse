import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class PointLight extends GameObject{

    private Vector4f color;

    public PointLight() {
        super();

    }

    public PointLight (Vector3f p, Vector4f c) {
        super(p);
        color = new Vector4f(c);
    }

    public PointLight (float x, float y, float z, float r, float g, float b, float i) {
        super(x, y, z);
        color = new Vector4f(r, g, b, i);
    }

    public Vector4f getColor() {
        return new Vector4f(color);
    }
}
