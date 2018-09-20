package com.rontoking.rontolang.rontoui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Node {
    protected Array<Node> children;
    public float manualWidth, manualHeight;
    public Color color, borderColor;
    protected SizeMode sizeMode;
    public boolean isFocused;
    protected float marginUp, marginDown, marginLeft, marginRight;
    protected float borderSize;
    public Runnable onUpdate;

    public enum SizeMode{
        AUTOMATIC, MANUAL
    }

    public enum Alignment{
        CENTER, LEFT
    }

    public Node(){
        marginUp = 5;
        marginDown = 5;
        marginLeft = 5;
        marginRight = 5;
        color = new Color(Color.WHITE);
        borderColor = new Color(Color.BLACK);
        children = new Array<Node>();
        sizeMode = SizeMode.AUTOMATIC;
        borderSize = 0;
    }

    public void render(SpriteBatch spriteBatch, float x, float y){
        Color c = new Color(spriteBatch.getColor());
        if(borderSize > 0){
            spriteBatch.setColor(borderColor);
            Assets.node.draw(spriteBatch, x - borderSize, y - borderSize , getWidth() + borderSize * 2f, getHeight() + borderSize * 2f);
        }
        spriteBatch.setColor(color);
        Assets.node.draw(spriteBatch, x, y , getWidth(), getHeight());
        spriteBatch.setColor(c);

        for(int i = 0; i < children.size; i++)
            children.get(i).render(spriteBatch, getChildX(i, x), getChildY(i, y));
    }

    public void render(SpriteBatch spriteBatch){
        render(spriteBatch, 0, Gdx.graphics.getHeight() - getHeight());
    }

    public float getPrefWidth(){
        return marginLeft + marginRight;
    }

    public float getPrefHeight(){
        return marginDown + marginUp;
    }

    public float getWidth(){
        if(sizeMode == SizeMode.AUTOMATIC)
            return getPrefWidth();
        return manualWidth;
    }

    public float getHeight(){
        if(sizeMode == SizeMode.AUTOMATIC)
            return getPrefHeight();
        return manualHeight;
    }

    protected float getTop(float y, boolean insideMargin){
        if(insideMargin)
            return y + getHeight() - marginUp;
        return y + getHeight();
    }

    public void update(float x, float y){
        if(onUpdate != null)
            onUpdate.run();
        if(Gdx.input.justTouched()){
            isFocused = (RightClickMenu.position == null || !RontoUI.isMouseInRect(RightClickMenu.menu.getRect(RightClickMenu.position.x, RightClickMenu.position.y))) && RontoUI.isMouseInRect(getRect(x, y));
        }
        for(int i = 0; i < children.size; i++)
            children.get(i).update(getChildX(i, x), getChildY(i, y));
    }

    public void update(){
        update(0, Gdx.graphics.getHeight() - getHeight());
    }

    public void setMargins(float left, float right, float up, float down){
        marginLeft = left;
        marginRight = right;
        marginUp = up;
        marginDown = down;
    }

    public void setMargins(float margin){
        marginLeft = margin;
        marginRight = margin;
        marginUp = margin;
        marginDown = margin;
    }

    public void setHMargins(float left, float right){
        marginLeft = left;
        marginRight = right;
    }

    public void setVMargins(float up, float down){
        marginUp= up;
        marginDown= down;
    }

    public void setSizeMode(SizeMode sizeMode){
        this.sizeMode = sizeMode;
        manualWidth = getPrefWidth();
        manualHeight = getPrefHeight();
    }

    public SizeMode getSizeMode() {
         return sizeMode;
    }

    public float getChildX(int index, float x){
        return marginLeft + x;
    }

    public float getChildY(int index, float y){
        return marginDown + y;
    }

    protected Rectangle getRect(float x, float y){
        return new Rectangle(x, y, getWidth(), getHeight());
    }
}
