package com.rontoking.rontolang.interpreter.objects.networking;

import com.esotericsoftware.kryonet.Connection;

public class RontoPacket {
    public enum Type{
        Connected, Received, Disconnected
    }

    public Type type;
    public Connection sender;
    public Object data;

    public RontoPacket(Type type, Connection sender){
        this(type, sender, null);
    }

    public RontoPacket(Type type, Connection sender, Object data){
        this.type = type;
        this.sender = sender;
        this.data = data;
    }
}
