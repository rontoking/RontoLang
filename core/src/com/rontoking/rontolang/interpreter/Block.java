package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.program.Function;

public class Block {
    public ObjectMap<String, Variable> variables;
    public Function function;

    public Block(){
        // For KryoNet.
    }

    public Block(Function function){
        this.function = function;
        variables = new ObjectMap<String, Variable>();
    }

    public Variable get(String name){
        return variables.get(name);
    }

    public void set(String name, Variable variable){
        variables.put(name, variable);
    }
}
