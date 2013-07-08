package root.Entities;

import java.io.Serializable;

public abstract class Component implements Serializable {

    public int type;
    public Process process;

    public int getType() {
        return type;
    }

    public Process getProcess(){
        return process;
    }
}
