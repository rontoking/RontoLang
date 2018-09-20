package com.rontoking.rontolang.interpreter.objects.networking;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

public class RontoServer extends RontoSocket{
    public Server server;

    public RontoServer(){
        super();
    }

    public void host(int port){
        host(port, 32000, 32000);
    }

    public void host(int port, int writeSize, int objectSize){
        server = new Server(writeSize, objectSize);
        registerClasses(server.getKryo());
        server.addListener(listener());
        try {
            server.bind(port, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();
    }
}
