package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.rontoking.rontolang.rontoui.Assets;
import com.rontoking.rontolang.rontoui.Node;
import com.rontoking.rontolang.rontoui.RontoUI;

public class Slider extends Node{
    public Texture sliderTexture;
    public float lineWidth, lineHeight, sliderWidth, sliderHeight, sliderPosition, renderedSliderPosition, animationSpeed;
    public Color lineColor;
    public boolean autoLimitSliderPosition;
    public Runnable onSliderPositionChanged;

    public Slider(Texture sliderTexture, float sliderWidth, float sliderHeight, float sliderPosition, float lineWidth, float lineHeight, Color lineColor, float animationSpeed){
        super();
        this.sliderTexture = sliderTexture;
        this.sliderWidth = sliderWidth;
        this.sliderHeight = sliderHeight;
        this.sliderPosition = sliderPosition;
        this.lineWidth = lineWidth;
        this.lineHeight = lineHeight;
        this.lineColor = lineColor;
        this.autoLimitSliderPosition = true;
        this.animationSpeed = animationSpeed;
        this.onSliderPositionChanged = null;
        limitSliderPosition();
    }

    public Slider(){
        this(0, 0);
    }

    public Slider(float sliderPosition, float animationSpeed){
        this(Assets.slider, 15, 15, sliderPosition, 100, 5, new Color(Color.BLACK), animationSpeed);
    }

    @Override
    public float getPrefWidth(){
        return marginLeft + lineWidth + sliderWidth + marginRight;
    }

    @Override
    public float getPrefHeight(){
        return marginUp + lineHeight + sliderHeight + marginDown;
    }

    @Override
    public void update(float x, float y){
        super.update(x, y);
        if(isFocused && Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            sliderPosition = (RontoUI.getMouseX() - x - marginLeft - sliderWidth / 2) / lineWidth;
        }
        if(autoLimitSliderPosition)
            limitSliderPosition();
        if(animationSpeed <= 0)
            renderedSliderPosition = sliderPosition;
        if(sliderPosition != renderedSliderPosition) {
            boolean goingForward = renderedSliderPosition < sliderPosition;
            if(goingForward) {
                renderedSliderPosition += animationSpeed * Gdx.graphics.getDeltaTime();
                if(renderedSliderPosition > sliderPosition)
                    renderedSliderPosition = sliderPosition;
            }
            else {
                renderedSliderPosition -= animationSpeed * Gdx.graphics.getDeltaTime();
                if(renderedSliderPosition < sliderPosition)
                    renderedSliderPosition = sliderPosition;
            }
            if(onSliderPositionChanged != null)
                onSliderPositionChanged.run();
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        Color c = new Color(spriteBatch.getColor());
        spriteBatch.setColor(lineColor);
        spriteBatch.draw(Assets.pixel, x + marginLeft + sliderWidth / 2, y + marginDown + sliderHeight / 2 - lineHeight / 2, lineWidth, lineHeight);
        spriteBatch.setColor(c);
        spriteBatch.draw(sliderTexture, getSliderRect(x, y).x,  getSliderRect(x, y).y,  getSliderRect(x, y).width,  getSliderRect(x, y).height);
    }

    public Rectangle getSliderRect(float x, float y){
        return new Rectangle(x + marginLeft + lineWidth * renderedSliderPosition, y + marginDown, sliderWidth, sliderHeight);
    }

    public void limitSliderPosition(){
        if (sliderPosition < 0)
            sliderPosition = 0;
        else if (sliderPosition > 1)
            sliderPosition = 1;
    }
}
