package com.rontoking.rontolang.program;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.interpreter.ErrorHandler;
import com.rontoking.rontolang.interpreter.Interpreter;

public class Class {
    public String name;
    public String parentClass; // Via 'extends'.
    public Array<Function> classFunctions, objectFunctions;
    public Array<Property> classProperties, objectProperties;

    public Class(String name, String parent){
        this.name = name;
        parentClass = parent;
        classFunctions = new Array<Function>();
        objectFunctions = new Array<Function>();
        classProperties = new Array<Property>();
        objectProperties = new Array<Property>();
    }

    public Function getFunc(String name, boolean isStatic, String[] paramTypes, boolean throwError, Interpreter interpreter){
        Array<Function> array;
        if(isStatic)
            array = classFunctions;
        else
            array = objectFunctions;
        for(int i = 0; i < array.size; i++){
            if(array.get(i).name.equals(name) && array.get(i).parameters.size == paramTypes.length){ // It might be the target function.
                boolean isTarget = true;
                for(int p = 0; p < paramTypes.length; p++){ // Checking if parameter types match.
                    Parameter par = array.get(i).parameters.get(p);
                    if(!par.type.equals("") && !par.type.equals(paramTypes[p])) {
                        p = paramTypes.length; // It's not the target function.
                        isTarget = false;
                    }
                }
                if(isTarget)
                    return array.get(i);
            }
        }
        if(parentClass != null){
            return interpreter.getClass(parentClass).getFunc(name, isStatic, paramTypes, throwError, interpreter);
        }
        if(throwError)
            ErrorHandler.throwFuncError(this.name, name, paramTypes);
        return null;
    }

}
