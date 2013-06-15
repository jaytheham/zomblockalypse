import Utils.Constants;
import Utils.Settings;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

public class SphericalChaseCamera extends SphericalCamera {
    private Player playerTarget;
    private Vector3f postionVector;
    private double yawAboutTarget;
    private float distanceFromTarget;

    public SphericalChaseCamera(Player p) {
        super();

        playerTarget = p;
        postionVector = new Vector3f(0.0f, -1.0f, -1.0f);
        distanceFromTarget = Constants.CAMERA_MIN_ZOOM;
        yawAboutTarget = 0.0;

        position.x = (postionVector.x * distanceFromTarget) + -playerTarget.getX();
        position.y = (postionVector.y * distanceFromTarget) + -playerTarget.getY();
        position.z = (postionVector.z * distanceFromTarget) + -playerTarget.getZ();

        pitch = (float)Math.toDegrees((double)Vector3f.angle(postionVector, new Vector3f(0,0,-1)));
        postionVector.x = -(float)Math.sin(yawAboutTarget);
        postionVector.z = -(float)Math.cos(yawAboutTarget);
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

        if (Mouse.isButtonDown(2)) {
            yawAboutTarget += Settings.cameraRotateSensitivity * xChange;
            postionVector.x = -(float)Math.sin(Math.toRadians(yawAboutTarget));
            postionVector.z = -(float)Math.cos(Math.toRadians(yawAboutTarget));

            if (yawAboutTarget > 360.0f)
                yawAboutTarget -= 360.0f;
            else if (yawAboutTarget < 0.0f)
                yawAboutTarget += 360.0f;

            yaw = (float)-yawAboutTarget;


        }

        playerTarget.setMoveDirection(
                new Vector3f(
                        (float)Math.sin(Math.toRadians(360.0 - yawAboutTarget)),
                        0.0f,
                        (float)Math.cos(Math.toRadians(yawAboutTarget + 180.0))),
                new Vector3f(
                        (float)Math.cos(Math.toRadians(yawAboutTarget)),
                        0.0f,
                        (float)Math.sin(Math.toRadians(360.0 - yawAboutTarget))));


        position.x = (postionVector.x * distanceFromTarget) - playerTarget.getX();
        position.y = (postionVector.y * distanceFromTarget) - playerTarget.getY();
        position.z = (postionVector.z * distanceFromTarget) - playerTarget.getZ();
    }

    @Override
    public Vector3f getRayToMousePosition() {
        float theX = Mouse.getX() / (float)Display.getWidth();
        theX -= 0.5f;
        float angleChange = (Constants.FIELD_OF_VIEW * 1.5f)* theX;
        if (angleChange < 0.0f)
            angleChange += 360.0f;

        Vector3f forwardsV = getForwardsVector();
        Vector3f upV = getUpVector();
        Vector3f rayV = new Vector3f();

        double angle = Math.toRadians(-angleChange);

        float ux = upV.x*forwardsV.x;
        float uy = upV.x*forwardsV.y;
        float uz = upV.x*forwardsV.z;
        float vx = upV.y*forwardsV.x;
        float vy = upV.y*forwardsV.y;
        float vz = upV.y*forwardsV.z;
        float wx = upV.z*forwardsV.x;
        float wy = upV.z*forwardsV.y;
        float wz = upV.z*forwardsV.z;
        float sa = (float)Math.sin(angle);
        float ca = (float)Math.cos(angle);
        rayV.x = upV.x*(ux+vy+wz)+(forwardsV.x*(upV.y*upV.y+upV.z*upV.z)-upV.x*(vy+wz))*ca+(-wy+vz)*sa;
        rayV.y = upV.y*(ux+vy+wz)+(forwardsV.y*(upV.x*upV.x+upV.z*upV.z)-upV.y*(ux+wz))*ca+(wx-uz)*sa;
        rayV.z = upV.z*(ux+vy+wz)+(forwardsV.z*(upV.x*upV.x+upV.y*upV.y)-upV.z*(ux+vy))*ca+(-vx+uy)*sa;

        float theY = Mouse.getY() / (float)Display.getHeight();
        theY -= 0.5f;
         angleChange = (Constants.FIELD_OF_VIEW * 1.5f)* theY;
        if (angleChange < 0.0f)
            angleChange += 360.0f;

        forwardsV = rayV;
        upV = getRightVector();

        angle = Math.toRadians(angleChange);

        ux = upV.x*forwardsV.x;
        uy = upV.x*forwardsV.y;
        uz = upV.x*forwardsV.z;
        vx = upV.y*forwardsV.x;
        vy = upV.y*forwardsV.y;
        vz = upV.y*forwardsV.z;
        wx = upV.z*forwardsV.x;
        wy = upV.z*forwardsV.y;
        wz = upV.z*forwardsV.z;
        sa = (float)Math.sin(angle);
        ca = (float)Math.cos(angle);
        rayV.x = upV.x*(ux+vy+wz)+(forwardsV.x*(upV.y*upV.y+upV.z*upV.z)-upV.x*(vy+wz))*ca+(-wy+vz)*sa;
        rayV.y = upV.y*(ux+vy+wz)+(forwardsV.y*(upV.x*upV.x+upV.z*upV.z)-upV.y*(ux+wz))*ca+(wx-uz)*sa;
        rayV.z = upV.z*(ux+vy+wz)+(forwardsV.z*(upV.x*upV.x+upV.y*upV.y)-upV.z*(ux+vy))*ca+(-vx+uy)*sa;

        return rayV;
    }
}
