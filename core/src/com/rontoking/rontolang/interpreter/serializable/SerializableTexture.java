package com.rontoking.rontolang.interpreter.serializable;

import com.badlogic.gdx.graphics.Texture;

import java.io.Serializable;

public class SerializableTexture extends Texture implements Serializable{
    public SerializableTexture(Texture texture){
        super(texture.getTextureData());
    }
}
