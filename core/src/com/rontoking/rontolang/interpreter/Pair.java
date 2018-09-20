package com.rontoking.rontolang.interpreter;

import com.rontoking.rontolang.interpreter.Reference;

public class Pair {
    public Reference a, b;

    public Pair(Reference a, Reference b){
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return a + " : " + b;
    }
}
