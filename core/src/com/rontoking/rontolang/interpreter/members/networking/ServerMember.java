package com.rontoking.rontolang.interpreter.members.networking;

import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.interpreter.objects.networking.RontoServer;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class ServerMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("start") || funcName.equals("host")) {
                if(child.arguments.size == 2)
                    ((RontoServer)parent.value).host((int)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value));
                else
                    ((RontoServer)parent.value).host((int)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (int)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value), (int)Variable.getNum(Executor.execute(child.arguments.get(3), interpreter, ownerClass, instanceBlock).value));
                interpreter.socketState = Interpreter.SocketState.Server;
                return null;
            }else if (funcName.equals("send")) {
                if(child.arguments.size == 2){
                    ((RontoServer)parent.value).server.sendToAllTCP(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
                    return null;
                }else{
                    ((RontoServer)parent.value).server.sendToTCP(((Connection)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value).getID(), Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value);
                    return null;
                }
            }
        }else if(child.type == Instruction.Type.GetVariable){
            if(child.data.equals("connections") || child.data.equals("list") || child.data.equals("clients")){
                return Variable.getList(((RontoServer)parent.value).server.getConnections());
            }
        }
        return SocketMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
    }
}
