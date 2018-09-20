package com.rontoking.rontolang.interpreter.serializable;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.Serializable;

public class SerializableMap extends ObjectMap implements Serializable {
    public SerializableMap(ObjectMap map){
        super(map);
    }
}
