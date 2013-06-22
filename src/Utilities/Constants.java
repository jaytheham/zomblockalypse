package Utilities;

import org.lwjgl.util.vector.Vector3f;

public class Constants {
    public static final float MAX_PICK_DISTANCE = 78.0f;
    public static final float FIELD_OF_VIEW = 60.0f;

    public static final int MAX_NUM_LIGHTS = 8;

    public static final Vector3f GRAVITY_DIRECTION = new Vector3f(0.0f, -1.0f, 0.0f);
    public static final float GRAVITY_UNITS_PER_SECOND = 6.0f;

    public static final float CAMERA_MIN_ZOOM = 15.0f;
    public static final float CAMERA_MAX_ZOOM = 30.0f;

    public static final float ONE_METRE_IN_UNITS = 4.0f;

    public static final String BLOCK_TEXTURES_FILE_PATH = "res/block_textures.png";
}
