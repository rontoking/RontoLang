package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Reference { // Used for making primitive types mutable.
    public Object value;

    public Reference(){
        // For KryoNet.
    }

    public Reference(Object value){
        this.value = value;
    }

    @Override
    public String toString(){
        return value.toString();
    }
}
