package com.rontoking.rontolang.interpreter.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.interpreter.Variable;
import com.rontoking.rontolang.program.Function;

public class RontoRect extends RontoObject{

    public RontoRect(Object x, Object y, Object width, Object height, Interpreter interpreter){
        super();
        interpreter.addProperty(properties, "x", "double", x);
        interpreter.addProperty(properties, "y", "double", y);
        interpreter.addProperty(properties, "width", "double", width);
        interpreter.addProperty(properties, "height", "double", height);
    }

    @Override
    protected Reference noArgFunc(String funcName, Interpreter interpreter){
        if (funcName.equals("center")) {
            return new Reference(new RontoPoint(getRect().getCenter(new Vector2()), interpreter));
        }
        else if (funcName.equals("area")) {
            return new Reference(getRect().area());
        }
        else if (funcName.equals("ratio")) {
            return new Reference(getRect().getAspectRatio());
        }
        else if (funcName.equals("perimeter")) {
            return new Reference(getRect().perimeter());
        }
        return null;
    }

    public Rectangle getRect(){
        return new Rectangle((float)Variable.getNum(properties.get("x").getRef().value), (float)Variable.getNum(properties.get("y").getRef().value), (float)Variable.getNum(properties.get("width").getRef().value), (float)Variable.getNum(properties.get("height").getRef().value));
    }
}
