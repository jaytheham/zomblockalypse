package Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Jay
 * Date: 1/06/13
 * Time: 7:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Counter {

    private int count;

    public Counter(int intialValue) {
        count = intialValue;
    }

    public void decrement() {
        if (count > 0)
            count--;
    }
}
