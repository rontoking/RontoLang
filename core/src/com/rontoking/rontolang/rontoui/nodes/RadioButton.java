package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.rontoui.Assets;
import com.rontoking.rontolang.rontoui.Node;
import com.rontoking.rontolang.rontoui.RontoUI;

public class RadioButton extends Node{
    private Texture texture;
    private boolean isSelected;
    public int ID;

    public RadioButton(boolean isSelected, int ID, float size){
        super();
        this.texture = Assets.radioButton;
        this.isSelected = isSelected;
        RontoUI.addRadioButton(this);
        if(isSelected)
            RontoUI.deSelectRadioButtons(this);
        this.ID = ID;
        setMargins(0);
        sizeMode = SizeMode.MANUAL;
        manualWidth = manualHeight = size;
    }

    public RadioButton(boolean isSelected, int ID){
        super();
        this.texture = Assets.radioButton;
        this.isSelected = isSelected;
        RontoUI.addRadioButton(this);
        if(isSelected)
            RontoUI.deSelectRadioButtons(this);
        this.ID = ID;
        setMargins(0);
    }

    public RadioButton(){
        this(true, 0);
    }

    public void select(){
        isSelected = true;
        RontoUI.deSelectRadioButtons(this);
    }

    public void deSelect(){
        isSelected = false;
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
        if(RontoUI.JUST_LEFT_CLICKED && RontoUI.isMouseInRect(getRect(x, y))) {
            if(!isSelected){
                select();
            }
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        if(isSelected)
            spriteBatch.draw(texture, x + marginLeft, y + marginDown, getWidth() - marginLeft - marginRight, getHeight() - marginDown - marginUp, 0, 0, texture.getWidth(), texture.getHeight() / 2, false, false);
        else
            spriteBatch.draw(texture, x + marginLeft, y + marginDown, getWidth() - marginLeft - marginRight, getHeight() - marginDown - marginUp, 0, texture.getHeight() / 2, texture.getWidth(), texture.getHeight() / 2, false, false);
    }
}
