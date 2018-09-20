package com.rontoking.rontolang.interpreter.objects.networking;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.interpreter.Variable;
import com.rontoking.rontolang.program.Function;

public class RontoSocket {
    public Array<RontoPacket> queue;

    public RontoSocket(){
        queue = new Array<RontoPacket>();
    }

    protected void registerClasses(Kryo kryo){
        kryo.register(byte.class);
        kryo.register(boolean.class);
        kryo.register(int.class);
        kryo.register(float.class);
        kryo.register(double.class);
        kryo.register(char.class);
        kryo.register(String.class);
    }

    public boolean hasNext(Interpreter interpreter){
        if(queue.size > 0){
            interpreter.packet = queue.get(0);
            queue.removeIndex(0);
            return true;
        }
        return false;
    }

    public Listener listener(){
        return new Listener(){
            @Override
            public void connected(Connection connection) {
                queue.add(new RontoPacket(RontoPacket.Type.Connected, connection));
            }

            @Override
            public void received(Connection connection, Object object) {
                if(!(object instanceof FrameworkMessage.KeepAlive))
                    queue.add(new RontoPacket(RontoPacket.Type.Received, connection, object));
            }

            @Override
            public void disconnected(Connection connection) {
                queue.add(new RontoPacket(RontoPacket.Type.Disconnected, connection));
            }
        };
    }
}
