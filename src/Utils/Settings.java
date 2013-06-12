package Utils;

import org.lwjgl.input.Keyboard;

/**
 * Created with IntelliJ IDEA.
 * User: Jay
 * Date: 7/06/13
 * Time: 8:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class Settings {

    public static int keyboardLeft = Keyboard.KEY_A;
    public static int keyboardRight = Keyboard.KEY_D;

    public static int keyboardForwards = Keyboard.KEY_W;
    public static int keyboardBackwards = Keyboard.KEY_S;

    public static int keyboardUp = Keyboard.KEY_1;
    public static int keyboardDown = Keyboard.KEY_2;

    public static int fpsCameraLeft = Keyboard.KEY_F;
    public static int fpsCameraRight = Keyboard.KEY_H;

    public static int fpsCameraForwards = Keyboard.KEY_T;
    public static int fpsCameraBackwards = Keyboard.KEY_G;

    public static float mouseScrollSensitivity = 0.003f;
    public static float cameraRotateSensitivity = 400.0f;
}
