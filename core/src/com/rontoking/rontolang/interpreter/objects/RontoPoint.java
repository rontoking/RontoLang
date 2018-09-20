package com.rontoking.rontolang.interpreter.objects;

import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class RontoPoint extends RontoObject {
    public RontoPoint(Object x, Object y, Interpreter interpreter){
        super();
        interpreter.addProperty(properties, "x", "double", x);
        interpreter.addProperty(properties, "y", "double", y);
    }

    public RontoPoint(Vector2 vector2, Interpreter interpreter){
        super();
        interpreter.addProperty(properties, "x", "double", vector2.x);
        interpreter.addProperty(properties, "y", "double", vector2.y);
    }

    public float floatX(){
        return (float)Variable.getNum(properties.get("x").getRef());
    }

    public float floatY(){
        return (float)Variable.getNum(properties.get("y").getRef());
    }

    public Vector2 getVec(){
        return new Vector2((float)Variable.getNum(properties.get("x").getRef().value), (float)Variable.getNum(properties.get("y").getRef().value));
    }

    @Override
    protected Reference func(Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        String funcName = child.arguments.get(0).data.toString();
        if (funcName.equals("dst")) {
            if (child.arguments.size == 2) {
                return new Reference(getVec().dst(((RontoPoint) Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value).getVec()));
            } else {
                return new Reference(getVec().dst((float)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock)), (float)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock))));
            }
        }
        else if (funcName.equals("rotate") || funcName.equals("rot") || funcName.equals("rotDeg") || funcName.equals("rotateDeg")) {
            return new Reference(new RontoPoint(getVec().rotate((float)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value)), interpreter));
        }
        else if (funcName.equals("rotRad") || funcName.equals("rotateRad")) {
            return new Reference(new RontoPoint(getVec().rotateRad((float)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value)), interpreter));
        }
        else if (funcName.equals("rot90") || funcName.equals("rotate90")) {
            return new Reference(new RontoPoint(getVec().rotate90((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value)), interpreter));
        }
        else if (funcName.equals("lim") || funcName.equals("limit")) {
            return new Reference(new RontoPoint(getVec().limit((float) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value)), interpreter));
        }
        return null;
    }

    @Override
    protected Reference noArgFunc(String funcName, Interpreter interpreter){
        if (funcName.equals("len") || funcName.equals("length")) {
            return new Reference(getVec().len());
        }
        else if (funcName.equals("nor") || funcName.equals("normalized")) {
            return new Reference(new RontoPoint(getVec().nor(), interpreter));
        }
        else if (funcName.equals("angle") || funcName.equals("deg") || funcName.equals("degrees")) {
            return new Reference(getVec().angle());
        }
        else if (funcName.equals("rad") || funcName.equals("radians")) {
            return new Reference(getVec().angleRad());
        }
        else if (funcName.equals("isUnit")) {
            return new Reference(getVec().isUnit());
        }
        return null;
    }
}
