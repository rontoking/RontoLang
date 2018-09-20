package com.rontoking.rontolang.interpreter.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.interpreter.Variable;

// TODO: Make properties for animation: isVerticalSheet, frameNum, frameDur, currentFrame and update all sprite animations with deltaTime every frame.

public class RontoSprite extends RontoObject{
    public RontoSprite(Texture texture, Interpreter interpreter){
        this(texture, 0, 0, new RontoColor(Color.WHITE, interpreter), texture.getWidth(), texture.getHeight(), new RontoPoint(texture.getWidth() / 2, texture.getHeight() / 2, interpreter), new RontoPoint(1, 1, interpreter), new RontoRect(0, 0, texture.getWidth(), texture.getHeight(), interpreter), 0, false, false, interpreter);
    }

    public RontoSprite(Texture texture, double x, double y, RontoColor color, double width, double height, RontoPoint origin, RontoPoint scale, RontoRect src, double angle, boolean flipX, boolean flipY, Interpreter interpreter){
        super();
        interpreter.addProperty(properties, "img", "img", texture);
        interpreter.addProperty(properties, "x", "double", x);
        interpreter.addProperty(properties, "y", "double", y);
        interpreter.addProperty(properties, "color", "color", color);
        interpreter.addProperty(properties, "width", "double", width);
        interpreter.addProperty(properties, "height", "double", height);
        interpreter.addProperty(properties, "origin", "point", origin);
        interpreter.addProperty(properties, "scale", "point", scale);
        interpreter.addProperty(properties, "src", "rect", src);
        interpreter.addProperty(properties, "angle", "double", angle);
        interpreter.addProperty(properties, "flipX", "bool", flipX);
        interpreter.addProperty(properties, "flipY", "bool", flipY);
    }

    @Override
    protected Reference noArgFunc(String funcName, Interpreter interpreter){
        if (funcName.equals("draw")) {
            draw(interpreter);
            return new Reference(null);
        }
        return null;
    }

    public void draw(Interpreter interpreter){
        interpreter.setToSpriteState();
        interpreter.spriteBatch.setColor(((RontoColor)properties.get("color").getRef().value).getColor());
        interpreter.spriteBatch.draw((Texture) properties.get("img").getRef().value, (float)Variable.getNum(properties.get("x").getRef()), (float)Variable.getNum(properties.get("y").getRef()),
                ((RontoPoint)properties.get("origin").getRef().value).floatX(), ((RontoPoint)properties.get("origin").getRef().value).floatY(),
                (float)Variable.getNum(properties.get("width").getRef()), (float)Variable.getNum(properties.get("height").getRef()),
                ((RontoPoint)properties.get("scale").getRef().value).floatX(), ((RontoPoint)properties.get("scale").getRef().value).floatY(),
                (float)Variable.getNum(properties.get("angle").getRef()),
                (int)Variable.getNum(((RontoRect)properties.get("src").getRef().value).properties.get("x").getRef()), (int)Variable.getNum(((RontoRect)properties.get("src").getRef().value).properties.get("y").getRef()), (int)Variable.getNum(((RontoRect)properties.get("src").getRef().value).properties.get("width").getRef()), (int)Variable.getNum(((RontoRect)properties.get("src").getRef().value).properties.get("height").getRef()),
                (Boolean) properties.get("flipX").getRef().value, (Boolean) properties.get("flipY").getRef().value);
    }
}
