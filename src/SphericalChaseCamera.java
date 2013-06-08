import Utils.Constants;
import Utils.Settings;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class SphericalChaseCamera extends SphericalCamera {
    private Player playerTarget;
    private Vector3f postionVector;
    private float distanceFromTarget;

    public SphericalChaseCamera(Player p) {
        super();

        playerTarget = p;
        postionVector = new Vector3f(0.0f, -1.0f, -1.0f);
        distanceFromTarget = Constants.CAMERA_MIN_ZOOM;

        position.x = (postionVector.x * distanceFromTarget) + -playerTarget.getX();
        position.y = (postionVector.y * distanceFromTarget) + -playerTarget.getY();
        position.z = (postionVector.z * distanceFromTarget) + -playerTarget.getZ();

        pitch = (float)Math.toDegrees((double)Vector3f.angle(postionVector, new Vector3f(0,0,-1)));

    }

    @Override
    public void moveCamera(float xChange, float yChange, int timeDelta) {

        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            distanceFromTarget -= (dWheel * Settings.mouseScrollSensitivity);
            if (distanceFromTarget > Constants.CAMERA_MAX_ZOOM)
                distanceFromTarget = Constants.CAMERA_MAX_ZOOM;
            else if (distanceFromTarget < Constants.CAMERA_MIN_ZOOM)
                distanceFromTarget = Constants.CAMERA_MIN_ZOOM;
        }
        //yaw += rotationSpeed * xChange;



        position.x = (postionVector.x * distanceFromTarget) + -playerTarget.getX();
        position.y = (postionVector.y * distanceFromTarget) + -playerTarget.getY();
        position.z = (postionVector.z * distanceFromTarget) + -playerTarget.getZ();
    }
}
