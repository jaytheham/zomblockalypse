package root.Entities;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import root.ChunkManager;

import java.io.Serializable;

public class ProcessCollide implements Process, Serializable {

    public void run(GameEntity e) {
        ChunkManager chunkManager = ChunkManager.getInstance(null);

        Vector3f pos = e.getPosition();
        CmpMovement m = (CmpMovement)e.getComponent(ComponentType.MOVEMENT);
        CmpCollision c = (CmpCollision)e.getComponent(ComponentType.COLLISION);
        Vector3f nPos = new Vector3f(m.nextPosition);
        Vector3f bnds = new Vector3f(c.boundingBoxSize);
        Vector2f reduce = new Vector2f();

        nPos.y -= 0.4f; // Gravity

        boolean collided = false;

        //Each loop should get the original nPos to stop sticking at block boundaries
        // while nPos == block
        outerLoop : while (chunkManager.getBlock(nPos) != 0 && chunkManager.getBlock(pos) == 0) {

            // faulty:
            if (!collided) {
                nPos.y = pos.y;
            }
            if (chunkManager.getBlock(nPos) == 0)
                break;

            // If step > 0, for s in 1->step
            for (int i = 1; i <= c.step; i++) {
                // if test position (nPos + i) == OK
                if (chunkManager.getBlock(nPos.x, nPos.y + i, nPos.z) == 0) {
                    // return nPos + i
                    nPos.y = (float)(Math.floor((double)nPos.y) + i + 0.01f);
                    break outerLoop;
                }
            }
            // If climb > 0, for chunkManager in step -> climb
            for (int i = c.step; i <= c.climb; i++) {
                // if test position (nPos + i) == OK
                // Perform climbing
                // return
                continue;
            }
            // nPos slide and retry
            nPos = rejectCollidingVectorSegment(pos, nPos);
            collided = true;
        }

        // accept move
        e.setPosition(nPos);

        //if (collided)
        //    p.collided();
    }

    private Vector3f rejectCollidingVectorSegment(Vector3f pos, Vector3f nPos) {

        float x = 0;
        float z = 0;
        if (pos.x < nPos.x)
            x = 0.0f; // On left of block
        else
            x = 1.0f; // On right of block

        if (pos.z < nPos.z)
            z = 0.0f; // Behind block
        else
            z = 1.0f; // In front of block

        Vector2f blockXSideA = new Vector2f((int)Math.floor(nPos.x) + x, (int)Math.floor(nPos.z));
        Vector2f blockXSideB = new Vector2f(0.0f, 1.0f);

        if (linesIntersect(blockXSideA, blockXSideB, new Vector2f(pos.x, pos.z),
                new Vector2f(nPos.x-pos.x,nPos.z-pos.z))) {
            // Hit x face
            nPos.x = (float)Math.floor(nPos.x) + x;
            if (x == 0.0f)
                nPos.x-=0.01f;
            else
                nPos.x += 0.01f;
        }
        else {
            // must have hit z face
            nPos.z = (float)Math.floor(nPos.z) + z;
            if (z == 0.0f)
                nPos.z -=0.01f;
            else
                nPos.z += 0.01f;
        }
        return nPos;
    }

    private boolean linesIntersect(Vector2f p, Vector2f r, Vector2f q, Vector2f s) {

        float RcS = (r.x*s.y) - (r.y*s.x);

        if (RcS == 0.0f)
            return false; //Lines are parallel

        Vector2f a = new Vector2f();

        Vector2f.sub(q, p, a);
        float QPcS = (a.x*s.y) - (a.y*s.x);

        float t = QPcS/RcS;

        // If this is 0 lines are collinear
        float QPcR = (a.x*r.y) - (a.y*r.x);

        float u = QPcR/RcS;

        return (t >= 0.0f) && (t <=1.0f) && (u >= 0.0f) && (u <= 1.0f);
    }
}
