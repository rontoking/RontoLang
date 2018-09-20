package com.rontoking.rontolang.program;

import com.badlogic.gdx.utils.Array;

public class Program {
    public Array<Class> classes;

    public Program() {
        classes = new Array<Class>();
    }

    public Class getClass(String name){
        for(int i = 0; i < classes.size; i++)
            if(classes.get(i).name.equals(name))
                return classes.get(i);
        return null;
    }
}
