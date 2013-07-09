package root.Entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import root.Game;

import java.io.Serializable;

public class ProcessMovePlayer implements Process, Serializable {

    public void run(GameEntity e) {
        CmpSelfMovement movement = (CmpSelfMovement)e.getComponent(ComponentType.MOVEMENT);

        Game.camera.updatePlayerPosition(e.getPosition());

        movement.velocity.set(0.0f, 0.01f, 0.0f);
        movement.moveDirection.set(0.0f, 0.0f, 0.0f);

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            movement.moveDirection = Game.camera.getForwardsVector();
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            Game.camera.getForwardsVector().negate(movement.moveDirection);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            Vector3f.add(Game.camera.getRightVector(), movement.moveDirection, movement.moveDirection);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            Vector3f left = new Vector3f();
            Game.camera.getRightVector().negate(left);
            Vector3f.add(left, movement.moveDirection, movement.moveDirection);
        }

        movement.velocity.normalise();
        movement.moveDirection.y = 0;
        //movement.moveDirection.normalise();

        movement.moveDirection.scale(movement.maxUnitsPerSec * (Game.timeDelta / 1000.0f));

        movement.nextPosition.x = e.getPosition().x;
        movement.nextPosition.y = e.getPosition().y;
        movement.nextPosition.z = e.getPosition().z;

        movement.nextPosition.x += movement.moveDirection.x;
        movement.nextPosition.z += movement.moveDirection.z;
        //e.setPosition(new Vector3f(movement.nextPosition.x + movement.moveDirection.x,
        //        movement.nextPosition.y,
        //        movement.nextPosition.z + movement.moveDirection.z));

    }
}
