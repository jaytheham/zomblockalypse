package Entities;

import org.lwjgl.util.vector.Vector3f;

import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class PointLight extends GameEntity implements Serializable {

    private Vector3f color;
    private float intensity;

    public PointLight(int id, Vector3f p, Vector3f c, float i) {
        super(id, p);
        color = c;
        intensity = i;
    }

    public PointLight(int id, Vector3f p, float r, float g, float b, float i) {
        super(id, p);
        color = new Vector3f(r, g, b);
        intensity = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointLight light = (PointLight) o;

        if (this.id != light.id) return false;

        return true;
    }
}
