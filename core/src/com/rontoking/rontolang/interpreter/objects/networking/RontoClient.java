package com.rontoking.rontolang.interpreter.objects.networking;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.rontoking.rontolang.interpreter.Interpreter;

import java.io.IOException;

public class RontoClient extends RontoSocket{
    public Client client;

    public RontoClient(){
        super();
    }

    public boolean join(String ip, int port, Interpreter interpreter){
        return join(ip, port, 32000, 32000, interpreter);
    }

    public boolean join(String ip, int port, int writeSize, int objectSize, Interpreter interpreter){
        client = new Client(writeSize, objectSize);
        registerClasses(client.getKryo());
        client.addListener(listener());
        client.start();
        try {
            client.connect(5000, ip, port, port);
            interpreter.socketState = Interpreter.SocketState.Client;
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
