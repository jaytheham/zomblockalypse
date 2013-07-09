package root;

import root.Utilities.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.Sys;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Game {

    private int mouseLastX = 0;
    private int mouseLastY = 0;

    private double previousFrameTime;
    private int fps;
    private int lastFps = 0;
    private long lastFrameTime = 0;
    public static int timeDelta;
    public static SphericalCamera camera;

    //View
    public static Matrix4f projectionMatrix;
    private Matrix4f camXprjMatrix = new Matrix4f();


    public static void main(String[] args){
        new Game();
    }

    public Game(){

        PixelFormat pixelFormat = new PixelFormat();
        ContextAttribs contextAttribs = new ContextAttribs(3,2)
                .withForwardCompatible(true)
                .withProfileCore(true);

        try {
            Display.setDisplayMode(new DisplayMode(1280, 720));
            Display.create(pixelFormat, contextAttribs);
            Display.setTitle("Food, Water, -Shotgun");
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.out.println("Failed to create Display");
            System.exit(0);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_FRONT);
        // Wireframe
        //GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        ShaderProgram shaders = new ShaderProgram();
        setupPerspectiveMatrix();

        Vector3f savedPlayerPos = loadSave();
        SphericalCamera cameraFPS = new SphericalCamera(new Vector3f(0,1,0), savedPlayerPos);
        SphericalCamera cameraChase = new SphericalChaseCamera(savedPlayerPos);
        camera = cameraChase;

        ChunkManager chunkBaron = ChunkManager.getInstance(savedPlayerPos);
        EntityManager entityBaron = EntityManager.getInstance();

        Text.init("res/Font1.png");

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Errmagerd, failed to sleep");
        }

        previousFrameTime = Sys.getTime();
        while(!Display.isCloseRequested()) {

            Display.sync(60);
            timeDelta = getDelta();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);


            camera.moveCamera((Mouse.getX() - mouseLastX)/(float)Display.getWidth(),
                    (Mouse.getY() - mouseLastY)/(float)Display.getHeight(), timeDelta);
            mouseLastX = Mouse.getX();
            mouseLastY = Mouse.getY();


            while (Mouse.next()) {
                if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {

                    //int[] hitBlock = root.RayCaster.getIntercept(camera.getPosition(), camera.getForwardsVector(), Constants.MAX_PICK_DISTANCE);
                    int[] hitBlock = RayCaster.getIntercept(camera.getPosition(), camera.getRayToMousePosition(), Constants.MAX_PICK_DISTANCE);

                    if (hitBlock != null) {
                        hitBlock[0] += hitBlock[3];
                        hitBlock[1] += hitBlock[4];
                        hitBlock[2] += hitBlock[5];
                        chunkBaron.setBlock(hitBlock, 1);
                    }
                }
            }
            while (Keyboard.next()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_Y && Keyboard.getEventKeyState()) {
                    int[] hitBlock = RayCaster.getIntercept(camera.getPosition(), camera.getRayToMousePosition(), Constants.MAX_PICK_DISTANCE);
                    if (hitBlock != null)
                        chunkBaron.deleteBlock(hitBlock);
                }
                else if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState()) {
                    if (camera instanceof SphericalChaseCamera) {
                        Vector3f camv = new Vector3f(camera.getPosition());
                        camv.negate();
                        cameraFPS.setPosition(camv);
                        camera = cameraFPS;
                    }
                    else {
                        camera = cameraChase;
                    }
                }
                else if (Keyboard.getEventKey() == Keyboard.KEY_F1 && Keyboard.getEventKeyState()) {
                    chunkBaron.saveAllChunks();
                }
            }

            entityBaron.update();
            chunkBaron.update();

            Matrix4f.mul(projectionMatrix, camera.getMatrix(), camXprjMatrix);
            chunkBaron.render(camXprjMatrix, camera.getPosition(), camera.getForwardsVector());
            camera.renderTargetBlock(ShaderProgram.getDefaultProgramId(), ShaderProgram.getDefaultTransformMatrixId(), camXprjMatrix);

            updateFPS();
            Text.draw(String.format("%5.1f %5.1f %5.1f",
                    camera.playerPosition.x,
                    camera.playerPosition.y,
                    camera.playerPosition.z), 1, 90);
            Display.update();
        }

        // Should clear buffers and textures off the GPU here
        save(entityBaron.closeDatabase());
        ChunkLoader.close();
        ChunkSaver.close();
        Display.destroy();
    }

    private Vector3f loadSave() {
        Vector3f playerPosition = new Vector3f();
        try {
            BufferedReader br = new BufferedReader(new FileReader("res/Save.txt"));
            String[] data = br.readLine().split(" ");

            playerPosition.x = Float.parseFloat(data[1]);
            playerPosition.y = Float.parseFloat(data[2]);
            playerPosition.z = Float.parseFloat(data[3]);
            br.close();

        } catch (Exception e) {
            System.out.println("Error loading save file");
        }
        return playerPosition;
    }

    private void save(Vector3f playerPosition) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("res/Save.txt"));
            bw.write("0 ");
            bw.write(Float.toString(playerPosition.x));
            bw.write(" ");
            bw.write(Float.toString(playerPosition.y));
            bw.write(" ");
            bw.write(Float.toString(playerPosition.z));
            bw.flush();
            bw.close();

        } catch (Exception e) {
            System.out.println("Error loading save file");
        }
    }

    /**
     * Get the time in milliseconds
     * @return The system time in milliseconds
     */
    public long getTimeMillis() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public int getDelta() {
        long time = getTimeMillis();
        int delta = (int)(time - lastFrameTime);
        lastFrameTime = time;

        // Prevent freakish delta times due to pause etc
        if (delta > 40)
            delta = 40;
        return delta;
    }

    private void updateFPS() {
        Text.draw("FPS: " + lastFps, 1, 95);
        if (Sys.getTime() - previousFrameTime > 1000) {
            lastFps = fps;
            fps = 0;
            previousFrameTime += 1000;
        }
        fps++;
    }

    private void setupPerspectiveMatrix() {
        projectionMatrix = new Matrix4f();

        float fieldOfView = Constants.FIELD_OF_VIEW;
        float aspectRatio = Display.getWidth() / (float)Display.getHeight();
        float nearPlane = 0.5f;
        float farPlane = 300.0f;
        float yScale = coTangent(Math.toRadians((fieldOfView / 2.0f)));

        projectionMatrix.m00 = yScale / aspectRatio;
        projectionMatrix.m11 = yScale;
        projectionMatrix.m22 = -((farPlane + nearPlane) / (farPlane - nearPlane));
        projectionMatrix.m23 = -1.0f;
        projectionMatrix.m32 = -((2 * nearPlane * farPlane) / (farPlane - nearPlane));
        projectionMatrix.m33 = 0.0f;
    }

    /**
     * Return the co-tangent of an angle.
     * @param angle The angle in Radians.
     * @return the coTangent of the angle.
     */
    private float coTangent(double angle) {
        return (1.0f / (float)Math.tan(angle));
    }
}
