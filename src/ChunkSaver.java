import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkSaver {
    private final Runnable save = new Runnable() {
        @Override
        public void run() {
            saveChunk();
        }
    };
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final Chunk c;

    public ChunkSaver(Chunk c) {
        this.c = c;
    }

    public void save() {
        executor.submit(save);
    }

    public static void close() {
        executor.shutdown();
    }

    private void saveChunk() {
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
            System.out.println("Failed to save file: " + c.getPosition()[0] + " " +
                    c.getPosition()[1] + " " + c.getPosition()[2]);
        }
    }
}
