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
}
