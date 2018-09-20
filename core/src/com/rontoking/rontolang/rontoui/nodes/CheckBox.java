package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.rontoui.Assets;
import com.rontoking.rontolang.rontoui.Node;
import com.rontoking.rontolang.rontoui.RontoUI;

public class CheckBox extends Node{
    private Texture texture;
    private boolean isChecked;

    public CheckBox(boolean isChecked, float size){
        super();
        this.texture = Assets.checkBox;
        this.isChecked = isChecked;
        setMargins(0);
        sizeMode = SizeMode.MANUAL;
        manualWidth = manualHeight = size;
    }

    public CheckBox(boolean isChecked){
        super();
        this.texture = Assets.checkBox;
        this.isChecked = isChecked;
        setMargins(0);
    }

    public CheckBox(){
        this(true);
    }

    @Override
    public float getPrefWidth(){
        return marginLeft + 25 + marginRight;
    }

    @Override
    public float getPrefHeight(){
        return marginUp + 25 + marginDown;
    }

    @Override
    public void update(float x, float y){
        super.update(x, y);
        if(RontoUI.JUST_LEFT_CLICKED && RontoUI.isMouseInRect(getRect(x, y)))
            isChecked = !isChecked;
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        if(isChecked)
            spriteBatch.draw(texture, x + marginLeft, y + marginDown, getWidth() - marginLeft - marginRight, getHeight() - marginDown - marginUp, 0, 0, texture.getWidth(), texture.getHeight() / 2, false, false);
        else
            spriteBatch.draw(texture, x + marginLeft, y + marginDown, getWidth() - marginLeft - marginRight, getHeight() - marginDown - marginUp, 0, texture.getHeight() / 2, texture.getWidth(), texture.getHeight() / 2, false, false);
    }
}
