package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.program.Class;

public class Instance {
    public Class baseClass;
    public Block properties;

    public Instance(Class baseClass, Interpreter interpreter, Class ownerClass){
        this.baseClass = baseClass;
        this.properties = new Block(null);
        for(int i = 0; i < baseClass.objectProperties.size; i++){
            this.properties.set(baseClass.objectProperties.get(i).name, new Variable(baseClass.objectProperties.get(i).type, baseClass.objectProperties.get(i).access,
                    Executor.execute(baseClass.objectProperties.get(i).value, interpreter, ownerClass, properties)));
        }
        if(baseClass.parentClass != null){ // Inheriting the parent's members.
            ObjectMap<String, Variable> rontoObjectProperties = interpreter.getRontoObjectProperties(baseClass.parentClass);
            if(rontoObjectProperties == null) {
                Class parentClass = interpreter.getClass(baseClass.parentClass);
                for (int i = 0; i < parentClass.objectProperties.size; i++) {
                    this.properties.set(parentClass.objectProperties.get(i).name, new Variable(parentClass.objectProperties.get(i).type, parentClass.objectProperties.get(i).access,
                            Executor.execute(parentClass.objectProperties.get(i).value, interpreter, ownerClass, properties)));
                }
            }else{
                this.properties.variables.putAll(rontoObjectProperties);
            }
        }
    }

    @Override
    public String toString(){
        return "(" + baseClass.name + properties.variables.toString() + ")";
    }
}
