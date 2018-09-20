package com.rontoking.rontolang.interpreter.members.networking;

import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Executor;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.interpreter.objects.networking.RontoClient;
import com.rontoking.rontolang.interpreter.objects.networking.RontoServer;
import com.rontoking.rontolang.interpreter.objects.networking.RontoSocket;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class SocketMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if(child.type == Instruction.Type.GetVariable){
            if(child.data.equals("queue") || child.data.equals("packets")){
                return new Reference(((RontoSocket)parent.value).queue.size);
            }else if(child.data.equals("isConnected") || child.data.equals("isConn")){
                if(interpreter.socketState == Interpreter.SocketState.Server)
                    return new Reference(parent.value instanceof RontoServer);
                else if(interpreter.socketState == Interpreter.SocketState.Client)
                    return new Reference(parent.value instanceof RontoClient && ((RontoClient)parent.value).client.isConnected());
                return new Reference(false);
            }else if(child.data.equals("next") || child.data.equals("hasNext") || child.data.equals("read")){
                return new Reference(((RontoSocket)parent.value).hasNext(interpreter));
            }else if(child.data.equals("stop") || child.data.equals("disconnect") || child.data.equals("disc") || child.data.equals("shutdown")){
                if(interpreter.socketState == Interpreter.SocketState.Server)
                    ((RontoServer) parent.value).server.stop();
                else if(interpreter.socketState == Interpreter.SocketState.Client)
                    ((RontoClient) parent.value).client.stop();
                ((RontoSocket)parent.value).queue.clear();
                interpreter.socketState = Interpreter.SocketState.None;
                return null;
            }
        }else if(child.type == Instruction.Type.Function){
            if(child.data.equals("send")){
                if(child.arguments.size == 2){
                    if(interpreter.socketState == Interpreter.SocketState.Server)
                        ((RontoServer)parent.value).server.sendToAllTCP(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
                    else if(interpreter.socketState == Interpreter.SocketState.Client)
                        ((RontoClient)parent.value).client.sendTCP(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
                    return null;
                }else{
                    ((RontoServer)parent.value).server.sendToTCP(((Connection)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value).getID(), Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value);
                    return null;
                }
            }
        }
        return null;
    }
}
