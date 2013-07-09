package root.Entities;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import root.EntityManager;
import root.Game;
import root.RayCaster;
import root.Utilities.Constants;

import java.io.Serializable;

public class ProcessMoveZombie implements Process, Serializable {

    public void run(GameEntity e) {
        if (searchForTarget(e, EntityManager.getPlayer().getPosition())) {
            Vector3f.add(e.getPosition(),
                    movement(e,
                            (CmpSelfMovement) e.getComponent(ComponentType.MOVEMENT),
                            EntityManager.getPlayer().getPosition()),
                    ((CmpSelfMovement) e.getComponent(ComponentType.MOVEMENT)).nextPosition);
        }
    }

    private boolean searchForTarget(GameEntity e, Vector3f playerPos) {

        boolean attack = false;

        Vector3f direction = new Vector3f();
        CmpSelfMovement m = (CmpSelfMovement)e.getComponent(ComponentType.MOVEMENT);
        Vector3f.sub(playerPos, e.getPosition(), direction);
        Vector3f facingDirection = new Vector3f((float)Math.sin(Math.toRadians(m.rotation.y)),
                0.0f,
                -(float)Math.cos(Math.toRadians(m.rotation.y)));

        float distance = direction.length();

        // If within attack viewing angle and visible distance
        if (Math.toDegrees(Vector3f.angle(direction, facingDirection)) <= 45
                && direction.length() <= 64) {


            direction.normalise();
            // Need to add eye height to position
            //
            int[] result = RayCaster.getIntercept(e.getPosition(), direction, distance);

            if (result == null) {
                attack = true;
            }
        }
        // If within interest viewing angle and visible distance
        // Or within half a metre
        else if (Math.toDegrees(Vector3f.angle(direction, facingDirection)) <= 90
                && direction.length() <= 64
                || distance < Constants.ONE_METRE_IN_UNITS / 2) {

            int[] result = RayCaster.getIntercept(e.getPosition(), direction, distance);
            if (result == null) {
                Vector3f d = new Vector3f();
                Vector3f.sub(playerPos, e.getPosition(), d);
                doRotation(e, playerPos, m, d, 0.02f);
            }
        }
        return attack;
    }

    /**
     * Calculate how far and in which direction to try move this frame.
     * @return A vector containing the desired next position
     */
    private Vector3f movement(GameEntity e, CmpSelfMovement m, Vector3f playerPos) {

        Vector3f direction = new Vector3f();
        Vector3f.sub(playerPos, e.getPosition(), direction);

        doRotation(e, playerPos, m, direction, 0.1f);

        direction.normalise();
        direction.scale(m.maxUnitsPerSec * m.acceleration * (Game.timeDelta / 1000.0f));

        return direction;
    }

    private void doRotation(GameEntity e, Vector3f playerPos, CmpSelfMovement m, Vector3f directionOfTarget, float scale) {

        Vector2f facingDirection = new Vector2f((float)Math.sin(Math.toRadians(m.rotation.y)),
                -(float)Math.cos(Math.toRadians(m.rotation.y)));
        Vector2f targetDirection = new Vector2f(directionOfTarget.x, directionOfTarget.z);

        float angleToTarget = (float)Math.toDegrees(Vector2f.angle(facingDirection, targetDirection));
        if (angleToTarget > 0.5f) {
            facingDirection.x += e.getPosition().x;
            facingDirection.y += e.getPosition().z;
            if ((facingDirection.x - e.getPosition().x)*(playerPos.z - e.getPosition().z) -
                    (facingDirection.y - e.getPosition().z)*(playerPos.x - e.getPosition().x) < 0) {
                angleToTarget = -angleToTarget;
            }

            angleToTarget *= scale;
        }

        m.rotation.y += angleToTarget;

        if (m.rotation.y > 360.0f)
            m.rotation.y -= 360.0f;
        else if (m.rotation.y < 0.0f)
            m.rotation.y += 360.0f;
    }
}
