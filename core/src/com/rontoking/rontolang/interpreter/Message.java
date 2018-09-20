package com.rontoking.rontolang.interpreter;

public class Message {
    public enum Type{
        Return
    }

    public Type type;
    public Reference ref;

    public Message(Type type, Reference ref){
        this.type = type;
        this.ref = ref;
    }
}
