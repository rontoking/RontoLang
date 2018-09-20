package com.rontoking.rontolang.interpreter.objects;

import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

import java.io.Serializable;

public class RontoObject implements Serializable{
    protected Block properties;

    public RontoObject(){
        properties = new Block(null);
    }

    protected Reference func(Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        return null;
    }

    public Reference member(Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if(properties.get(child.data.toString()) != null)
                return properties.get(child.data.toString()).getRef();
            else {
                Reference ret = noArgFunc(child.data.toString(), interpreter);
                if(ret == null)
                    ErrorHandler.throwMemberError(getType(), child.data.toString());
                else
                    return ret;
            }
        } else if (child.type == Instruction.Type.Function) {
            return func(child, interpreter, ownerClass, instanceBlock);
        }
        return null;
    }

    protected Reference noArgFunc(String funcName, Interpreter interpreter){
        return null;
    }

    public ObjectMap<String, Variable> getProperties(){
        return properties.variables;
    }

    @Override
    public String toString(){
        return "(" + getType() + properties.variables.toString() + ")";
    }

    private String getType(){
        return getClass().getSimpleName().substring(5);
    }
}
