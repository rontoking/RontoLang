package com.rontoking.rontolang.program;

public class Parameter {
    public String type;
    public boolean isReference;
    public String name;

    public Parameter(String type, boolean isReference, String name){
        this.type = type;
        this.isReference = isReference;
        this.name = name;
    }

    @Override
    public String toString(){
        String s = name;
        if(isReference)
            s = "&" + name;
        if(type.equals(""))
            return s;
        return type + " " + s;
    }
}
