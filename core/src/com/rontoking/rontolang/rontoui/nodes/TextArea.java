package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.rontoui.Font;
import com.rontoking.rontolang.rontoui.Node;
import com.rontoking.rontolang.rontoui.RontoUI;

public class TextArea extends Node{
    public String text;
    public Font font;
    public float yScrollOffset;
    public boolean manuallyScrolling;

    public TextArea(String text){
        this.text = text;
        this.font = new Font(Color.WHITE);
        this.color = new Color(Color.BLACK);
        this.yScrollOffset = 0;
        this.manuallyScrolling = false;
    }

    @Override
    public float getPrefWidth(){
        return marginLeft + RontoUI.getTextWidth(text, font.font) + marginRight;
    }

    @Override
    public float getPrefHeight(){
        return marginUp + RontoUI.getTextHeight(text, font.font, getWidth() - marginLeft - marginRight) + marginDown;
    }

    @Override
    public void update(float x, float y) {
        super.update(x, y);
        if (text.length() > 2000) {
            text = text.substring(text.length() - 1500);
        }
        if (RontoUI.SCROLL_AMOUNT != 0) {
            manuallyScrolling = true;
            yScrollOffset += RontoUI.SCROLL_AMOUNT * 10;
            if(yScrollOffset < 0)
                yScrollOffset = 0;
        }
        if(RontoUI.getTextHeight(text, font.font, getWidth() - marginLeft - marginRight) - yScrollOffset < manualHeight - marginUp - marginDown)
            manuallyScrolling = false;
        if(!manuallyScrolling) {
            if (RontoUI.getTextHeight(text, font.font, getWidth() - marginLeft - marginRight) > manualHeight - marginUp - marginDown) {
                yScrollOffset = RontoUI.getTextHeight(text, font.font, getWidth() - marginLeft - marginRight) - (manualHeight - marginUp - marginDown);
            } else {
                yScrollOffset = 0;
            }
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        font.font.draw(spriteBatch, text, x + marginLeft, y + getHeight() - marginUp + yScrollOffset, getWidth() - marginLeft - marginRight, -1, true);
    }
}
