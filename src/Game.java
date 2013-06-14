import Utils.ShaderUtils;
import Utils.CrossHair;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.Sys;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Utils.Constants;

public class Game {

    private int transformMatrixId;
    private int pId = 0;
    int mouseLastX = 0;
    int mouseLastY = 0;

    double lastFPS;
    int fps;

    private long lastFrameTime = 0;
    private int timeDelta;

    //View
    Matrix4f projectionMatrix;
    Matrix4f camXprjMatrix = new Matrix4f();


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
            Display.setTitle("Food, Water, --Shotgun");
        }
        catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_FRONT);
        // Wireframe
        //GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        setupDefaultShaders("Shaders/DefaultVertex.glsl", "Shaders/DefaultFragment.glsl");
        setupPerspectiveMatrix();

        Player playerOne = new Player();

        SphericalCamera cameraFPS = new SphericalCamera(new Vector3f(0,1,0));
        SphericalCamera cameraChase = new SphericalChaseCamera(playerOne);
        SphericalCamera camera = cameraChase;

        CrossHair aimingRecticle = new CrossHair();
        aimingRecticle.setupShader("Shaders/HudVertex.glsl", "Shaders/HudFragment.glsl");

        ChunkManager chunkBaron = ChunkManager.getInstance(playerOne);
        Collider collider = new Collider();

        try {
            Thread.sleep(500); // let loading happen before player starts colliding
        }
        catch (Exception e) {
        }

        lastFPS = Sys.getTime();

        while(!Display.isCloseRequested()) {
            Display.sync(60);
            updateFPS();
            timeDelta = getDelta();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);


            camera.moveCamera((Mouse.getX() - mouseLastX)/(float)Display.getWidth(),
                    (Mouse.getY() - mouseLastY)/(float)Display.getHeight(), timeDelta);
            mouseLastX = Mouse.getX();
            mouseLastY = Mouse.getY();

            playerOne.move(timeDelta);

            while (Mouse.next()) {
                if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
                    try {
                        int[] hitBlock = RayCaster.raycast(camera.getPosition(), camera.getForwardsVector(), Constants.MAX_PICK_DISTANCE);
                        if (hitBlock != null) {
                            hitBlock[0] += hitBlock[3];
                            hitBlock[1] += hitBlock[4];
                            hitBlock[2] += hitBlock[5];
                            chunkBaron.setBlock(hitBlock, 1);
                        }
                    }
                    catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            while (Keyboard.next()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_Y && Keyboard.getEventKeyState()) {
                    try {
                        int[] hitBlock = RayCaster.raycast(camera.getPosition(), camera.getForwardsVector(), Constants.MAX_PICK_DISTANCE);
                        if (hitBlock != null)
                            chunkBaron.deleteBlock(hitBlock);
                    }
                    catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
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

            collider.collide(playerOne);

            chunkBaron.update();

            Matrix4f.mul(projectionMatrix, camera.getMatrix(), camXprjMatrix);
            chunkBaron.render(camXprjMatrix);
            playerOne.render(pId, transformMatrixId, camXprjMatrix);
            camera.renderTargetBlock(pId, transformMatrixId, camXprjMatrix);
            aimingRecticle.render();

            Display.update();
        }

        // Should clear buffers and textures off the GPU here
        ChunkSaver.close();
        ChunkLoader.close();
        Display.destroy();
    }


    private void setupDefaultShaders(String vertShader, String fragShader) {
        int vsId = ShaderUtils.loadShader(vertShader, GL20.GL_VERTEX_SHADER);
        int fsId = ShaderUtils.loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);

        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);

        GL20.glBindAttribLocation(pId, 0, "in_Position");

        GL20.glLinkProgram(pId);

        int status = GL20.glGetShaderi(pId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shaders failed to link!");
            System.exit(-1);
        }

        transformMatrixId = GL20.glGetUniformLocation(pId, "transformMatrix");

        GL20.glDetachShader(pId, vsId);
        GL20.glDetachShader(pId, fsId);
    }



    /**
     * Get the time in milliseconds
     *
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
        if (Sys.getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    private void setupPerspectiveMatrix() {
        projectionMatrix = new Matrix4f();

        float fieldOfView = 60.0f;
        float aspectRatio = Display.getWidth() / (float)Display.getHeight();
        float nearPlane = 0.5f;
        float farPlane = 300.0f;
        float yScale = this.coTangent(this.degreesToRadians((fieldOfView / 2.0f)));

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
    private float coTangent(float angle) {
        return (1.0f / (float)Math.tan((double)angle));
    }

    private float degreesToRadians(float degrees) {
        return (float)(degrees * (Math.PI / 180.0d));
    }
}
