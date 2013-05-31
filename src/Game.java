import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.Sys;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

import java.io.*;

public class Game {

    private int transformMatrixId;
    private int vsId = 0;
    private int fsId = 0;
    private int pId = 0;
    int mouseLastX = 0;
    int mouseLastY = 0;

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
            Display.setDisplayMode(new DisplayMode(800, 600));
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
        //GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        setupShaders();

        Camera cam = new Camera();
        CameraFPS camF = new CameraFPS();

        Player playerOne = new Player();
        playerOne.setPosition(camF.getPosition());
        ChunkManager chnkLord = new ChunkManager(playerOne);

        while(!Display.isCloseRequested()) {
            Display.sync(60);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);

            //camMovement(cam);
            camMoveFPS(camF);

            //tie player to camera
            playerOne.setPosition(camF.getPosition());
            chnkLord.update();

            Matrix4f.mul(projectionMatrix, camF.getMatrix(), camXprjMatrix);
            chnkLord.render(pId, transformMatrixId, camXprjMatrix);

            Display.update();
        }

        ChunkSaver.close();
        Display.destroy();
    }


    private void setupShaders() {
        vsId = this.loadShader("src/vertex.glsl", GL20.GL_VERTEX_SHADER);
        fsId = this.loadShader("src/fragment.glsl", GL20.GL_FRAGMENT_SHADER);

        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);

        GL20.glBindAttribLocation(pId, 0, "in_Position");
        GL20.glBindAttribLocation(pId, 1, "in_Color");

        GL20.glLinkProgram(pId);

        int status = GL20.glGetShaderi(pId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shaders failed to link!");
            System.exit(-1);
        }

        transformMatrixId = GL20.glGetUniformLocation(pId, "transformMatrix");

        GL20.glUseProgram(pId);

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

        GL20.glUseProgram(0);

        GL20.glDetachShader(pId, vsId);
        GL20.glDetachShader(pId, fsId);
    }

    /**
     * Load a shader file and compile it.
     */
    private int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }

        shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        int status = GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS);

        if (status == GL11.GL_FALSE) {
            System.out.println("ERROR: Shader " + filename + " failed to compile!");
            System.exit(-1);
        }

        return shaderID;
    }

    private void movement(TriTest tri) {
        if (Keyboard.isKeyDown(Keyboard.KEY_A))
            tri.changePos(-0.05f, 0.0f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_D))
            tri.changePos(0.05f, 0.0f);

        if (Keyboard.isKeyDown(Keyboard.KEY_W))
            tri.changePos(0.0f, 0.05f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_S))
            tri.changePos(0.0f, -0.05f);
    }

    private void camMoveFPS(CameraFPS cam) {
        if (Keyboard.isKeyDown(Keyboard.KEY_F))
            cam.moveLeft(0.5f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_H))
            cam.moveRight(0.5f);

        if (Keyboard.isKeyDown(Keyboard.KEY_T))
            cam.moveForwards(0.5f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_G))
            cam.moveBackwards(0.5f);

        if (Mouse.isButtonDown(0)) {
            cam.moveTarget((Mouse.getX() - mouseLastX)/(float)Display.getWidth(),
                    (Mouse.getY() - mouseLastY)/(float)Display.getHeight());
        }
        mouseLastX = Mouse.getX();
        mouseLastY = Mouse.getY();
    }

    private void camMovement(Camera cam) {
        if (Keyboard.isKeyDown(Keyboard.KEY_F))
            cam.offsetPosition(-0.01f, 0.0f, 0.0f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_H))
            cam.offsetPosition(0.01f, 0.0f, 0.0f);

        if (Keyboard.isKeyDown(Keyboard.KEY_R))
            cam.offsetPosition(0.0f, 0.01f, 0.0f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_Y))
            cam.offsetPosition(0.0f, -0.01f, 0.0f);

        if (Keyboard.isKeyDown(Keyboard.KEY_T))
            cam.offsetPosition(0.0f, 0.0f, -0.01f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_G))
            cam.offsetPosition(0.0f, 0.0f, 0.01f);


        if (Keyboard.isKeyDown(Keyboard.KEY_J))
            cam.offsetTarget(-0.01f, 0.0f, 0.0f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_L))
            cam.offsetTarget(0.01f, 0.0f, 0.0f);

        if (Keyboard.isKeyDown(Keyboard.KEY_I))
            cam.offsetTarget(0.0f, 0.0f, 0.01f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_K))
            cam.offsetTarget(0.0f, 0.0f, -0.01f);

        if (Keyboard.isKeyDown(Keyboard.KEY_U))
            cam.offsetTarget(0.0f, 0.01f, 0.0f);
        else if (Keyboard.isKeyDown(Keyboard.KEY_O))
            cam.offsetTarget(0.0f, -0.01f, 0.0f);

    }

    /**
     * Get the time in milliseconds
     *
     * @return The system time in milliseconds
     */
    public long getTimeMillis() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    /**
     * Return the co-tangent of an angle.
     * @param angle The angle in Radians.
     * @return
     */
    private float coTangent(float angle) {
        return (1.0f / (float)Math.tan((double)angle));
    }

    private float degreesToRadians(float degrees) {
        return (float)(degrees * (Math.PI / 180.0d));
    }
}
