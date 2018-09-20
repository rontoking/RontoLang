package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.graphics.Color;
import com.rontoking.rontolang.rontoui.Font;
import com.rontoking.rontolang.rontoui.RontoUI;

public class Button extends Label {
    public boolean isPressed;
    public Runnable onPress;
    public Color releasedColor, pressedColor;

    public Button(String text) {
        super(text);
        isPressed = false;
        this.releasedColor = new Color(color);
        setPressedColor();
    }
    public Button(String text, Font font) {
        super(text, font);
        isPressed = false;
        this.releasedColor = new Color(color);
        setPressedColor();
    }

    public Button(String text, Runnable onPress){
        this(text);
        this.onPress = onPress;
    }

    public Button(String text, Font font, Runnable onPress){
        this(text, font);
        this.onPress = onPress;
    }

    private void setPressedColor(){
        pressedColor = new Color(color.r * 0.7f, color.g * 0.7f, color.b * 0.7f, color.a);
    }



    @Override
    public void update(float x, float y){
        super.update(x, y);
        if(!isPressed && RontoUI.JUST_LEFT_CLICKED) {
            isPressed = RontoUI.isMouseInRect(getRect(x, y));
        }else if(isPressed && RontoUI.JUST_LEFT_RELEASED){
            if(onPress != null && RontoUI.isMouseInRect(getRect(x, y))) // Button has been pressed.
                onPress.run();
            isPressed = false;
        }
        if(isPressed)
            color = pressedColor;
        else
            color = releasedColor;
    }
}
