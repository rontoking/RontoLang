package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.rontoui.Font;
import com.rontoking.rontolang.rontoui.Node;
import com.rontoking.rontolang.rontoui.RontoUI;

public class Label extends Node {
    protected String text;
    public Font font;
    public Alignment alignment;
    public int charDist;

    public Label(String text){
        this(text, new Font(Color.BLACK));
    }

    public Label(String text, Font font){
        super();
        this.text = text;
        this.font = font;
        this.alignment = Alignment.CENTER;
        charDist = 1;
        updateTextTexture();
    }

    @Override
    public float getPrefWidth(){
        return marginLeft + RontoUI.getTextWidth(text, font.font) + marginRight;
    }

    @Override
    public float getPrefHeight(){
        return marginUp + RontoUI.getTextHeight(text, font.font) + marginDown;
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        //if(textTexture.getWidth() > 0) {
        //    spriteBatch.draw(textTexture, getTextX(x), getTextY(y - RontoUI.TEXT_TO_TEXTURE_PIXMAP_HEIGHT_BONUS / 2), 0, 0, (int) (getWidth() - marginRight - marginLeft), textTexture.getHeight());
        //}
        font.font.draw(spriteBatch, renderedText(), getTextX(x), getTextY(y) + RontoUI.getTextHeight(renderedText(), font.font));
    }

    protected void updateTextTexture(){

    }

    public String renderedText(){
        return text;
    }

    public void appendText(String txt){
        text += txt;
        updateTextTexture();
    }

    private float getTextX(float x){
        if(alignment == Alignment.CENTER)
            return x + getWidth() / 2 - RontoUI.getTextWidth(renderedText(), font.font) / 2;
        else
            return x + marginLeft;
    }

    private float getTextY(float y){
        if(alignment == Alignment.CENTER)
            return y + getHeight() / 2 - RontoUI.getTextHeight(renderedText(), font.font) / 2;
        else
            return y + marginDown;
    }

    public void setText(String text){
        this.text = text;
        updateTextTexture();
    }

    public String getText(){
        return text;
    }
}
