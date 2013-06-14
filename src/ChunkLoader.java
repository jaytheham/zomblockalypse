import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkLoader {
    private final Runnable load = new Runnable() {
        @Override
        public void run() {
            loadChunk();
        }
    };
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final Chunk c;

    public ChunkLoader(Chunk c) {
        this.c = c;
    }

    public void load() {
        executor.submit(load);
    }

    public static void close() {
        executor.shutdown();
    }

    private void loadChunk() {
        String fileName = "save/" +
                c.getPosition()[0] + "_" +
                c.getPosition()[1] + "_" +
                c.getPosition()[2] + ".cnk";
        try {
            FileInputStream fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            for (int i = 0; i < c.getNumBlocks(); i++) {
                c.setBlock(i, dis.readInt());
            }
            c.hasBeenLoaded();
        }
        catch (IOException e) {
            //System.out.println("Failed to load file: " + c.getPositionf()[0] + " " +
            //c.getPositionf()[1] + " " + c.getPositionf()[2]);
            //for (int i = 0; i < c.getNumBlocks(); i++) {
            //    c.setBlock(i, 1);
            //}
            c.hasBeenLoaded();
        }
    }
}
