package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.rontoui.Assets;
import com.rontoking.rontolang.rontoui.Font;
import com.rontoking.rontolang.rontoui.Node;
import com.rontoking.rontolang.rontoui.RontoUI;

public class ProgressBar extends Node{
    public float progress, renderedProgress;
    public Color barColor, progressColor;
    public float animationSpeed;
    public boolean autoLimitProgress, displayText;
    public Runnable onProgressChanged;
    public Font font;

    public ProgressBar(){
        this(0);
    }

    public ProgressBar(float progress){
        this(progress, -1);
    }

    public ProgressBar(float progress, float animationSpeed){
        super();
        this.progress = progress;
        this.renderedProgress = progress;
        this.animationSpeed = animationSpeed;
        this.barColor = Color.BLACK;
        this.progressColor = Color.GREEN;
        this.autoLimitProgress = true;
        this.onProgressChanged = null;
        this.font = new Font(Color.WHITE, 24, Color.BLACK, 2);
        this.displayText = true;
        limitProgress();
    }

    @Override
    public float getPrefWidth(){
        return marginLeft + 200 + marginRight;
    }

    @Override
    public float getPrefHeight(){
        return marginUp + 50 + marginDown;
    }

    @Override
    public void update(float x, float y){
        super.update(x, y);
        if(autoLimitProgress)
            limitProgress();
        if(animationSpeed <= 0)
            renderedProgress = progress;
        if(progress != renderedProgress) {
            boolean goingForward = renderedProgress < progress;
            if(goingForward) {
                renderedProgress += animationSpeed * Gdx.graphics.getDeltaTime();
                if(renderedProgress > progress)
                    renderedProgress = progress;
            }
            else {
                renderedProgress -= animationSpeed * Gdx.graphics.getDeltaTime();
                if(renderedProgress < progress)
                    renderedProgress = progress;
            }
            if(onProgressChanged != null)
                onProgressChanged.run();
        }
    }

    public void limitProgress(){
        if (progress < 0)
            progress = 0;
        else if (progress > 1)
            progress = 1;
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        Color c = new Color(spriteBatch.getColor());
        spriteBatch.setColor(barColor);
        spriteBatch.draw(Assets.pixel, x + marginLeft, y + marginDown, getWidth() - marginLeft - marginRight, getHeight() - marginDown - marginUp);
        spriteBatch.setColor(progressColor);
        spriteBatch.draw(Assets.pixel, x + marginLeft, y + marginDown, (getWidth() - marginLeft - marginRight)*renderedProgress, getHeight() - marginDown - marginUp);
        spriteBatch.setColor(c);
        if(displayText)
            font.font.draw(spriteBatch, getText(), x + marginLeft + (getWidth() - marginLeft - marginRight) / 2 - RontoUI.getTextWidth(getText(), font.font) / 2, y + marginDown + (getHeight() - marginDown - marginUp) / 2 + RontoUI.getTextHeight(getText(), font.font) / 2);
    }

    public String getText(){
        return (int)(renderedProgress * 100) + " %";
    }
}
