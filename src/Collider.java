import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector2f;

public class Collider {

    public Collider() {

    }

    public void collide(Player p) {
        ChunkManager c = ChunkManager.getInstance(null);

        Vector3f pos = p.getPosition();
        Vector3f nPos = new Vector3f(p.getNextPosition());
        Vector3f bnds = new Vector3f(p.getBoundingBoxSize());
        Vector2f reduce = new Vector2f();

        // if next position not valid
        while (c.getBlock(nPos) != 0 && c.getBlock(pos) == 0) { //Each loop should get the original nPos to stop sticking at block boundaries
         // Find side of intersection
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
            //Vector2f blockXSideB = new Vector2f((int)Math.floor(nPos.x) + x, (int)Math.floor(nPos.z) + 1.0f);
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

         // Reduce next based on intersected side
         // repeat
        }
        // else accept move
        p.setPosition(nPos);
    }
/*
    private boolean validMove(Vector3f nextPos) {


        if (c.getBlock(nextPos) != 0) {
            return true;
        }
        else {
            return false;
        }

    }
*/

    public boolean linesIntersect(Vector2f p, Vector2f r, Vector2f q, Vector2f s) {

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
