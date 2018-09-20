package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.rontoui.Node;

public class Image extends Node{
    public Texture texture;

    public Image(){
        this("");
    }

    public Image(String textureInternalPath){
        super();
        if(textureInternalPath.equals(""))
            texture = null;
        else
            texture = new Texture(Gdx.files.internal(textureInternalPath));
    }

    public Image(Texture texture){
        super();
        this.texture = texture;
    }

    public Image(Texture texture, float width, float height){
        super();
        this.texture = texture;
        sizeMode = SizeMode.MANUAL;
        manualWidth = width;
        manualHeight = height;
    }

    public Image(String textureInternalPath, float width, float height){
        if(textureInternalPath.equals(""))
            texture = null;
        else
            texture = new Texture(Gdx.files.internal(textureInternalPath));
        sizeMode = SizeMode.MANUAL;
        manualWidth = width;
        manualHeight = height;
    }

    @Override
    public float getPrefWidth(){
        return marginLeft + texture.getWidth() + marginRight;
    }

    @Override
    public float getPrefHeight(){
        return marginUp + texture.getHeight() + marginDown;
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        if(texture != null) {
            spriteBatch.draw(texture, x + marginLeft, y + marginDown, getWidth() - marginLeft - marginRight, getHeight() - marginDown - marginUp);
        }
    }
}
