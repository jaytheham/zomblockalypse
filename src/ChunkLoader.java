import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
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
                (int)c.getPosition()[0] + "_" +
                (int)c.getPosition()[1] + "_" +
                (int)c.getPosition()[2] + ".cnk";
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bos);

            int[] blocks = c.getBlocks();

            for (int i : blocks) {
                dos.writeInt(i);
            }
            dos.close();
            bos.close();
            fos.close();

            c.wasSaved();
        }
        catch (IOException e) {
            System.out.println("Failed to save file!");
        }
    }
}
