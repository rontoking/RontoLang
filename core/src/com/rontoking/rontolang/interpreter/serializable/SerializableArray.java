package com.rontoking.rontolang.interpreter.serializable;

import com.badlogic.gdx.utils.Array;

import java.io.Serializable;

public class SerializableArray extends Array implements Serializable {
    public SerializableArray(Array array){
        super(array);
    }
}
