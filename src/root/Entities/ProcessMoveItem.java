package root.Entities;

import org.lwjgl.util.vector.Vector3f;
import root.ChunkManager;
import root.Game;

import java.io.Serializable;

public class ProcessMoveItem implements Process, Serializable {

    public void run(GameEntity e) {
        root.ChunkManager cm = ChunkManager.getInstance(null);

        CmpMovement move = (CmpMovement)e.getComponent(ComponentType.MOVEMENT);

        move.nextPosition.y -= (Game.timeDelta / 1000.0) * 3.6;

        if (cm.getBlock(move.nextPosition.x,move.nextPosition.y,move.nextPosition.z) == 0) {
            e.setPosition(new Vector3f(move.nextPosition));
        }
        else {
            move.nextPosition.y = e.getPosition().y;
        }
    }
}
