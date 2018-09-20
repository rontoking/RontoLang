package com.rontoking.rontolang.interpreter.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Variable;

public class RontoColor extends RontoObject{
    public RontoColor(Color color, Interpreter interpreter){
        this(color.r, color.g, color.b, color.a, interpreter);
    }
    
    public RontoColor(Object r, Object g, Object b, Object a, Interpreter interpreter){
        super();
        interpreter.addProperty(properties, "r", "double", r);
        interpreter.addProperty(properties, "g", "double", g);
        interpreter.addProperty(properties, "b", "double", b);
        interpreter.addProperty(properties, "a", "double", a);
    }

    public Color getColor(){
        return new Color((float) Variable.getNum(properties.get("r").getRef().value), (float)Variable.getNum(properties.get("g").getRef().value), (float)Variable.getNum(properties.get("b").getRef().value), (float)Variable.getNum(properties.get("a").getRef().value));
    }
}
