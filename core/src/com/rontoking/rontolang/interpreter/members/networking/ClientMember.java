package com.rontoking.rontolang.interpreter.members.networking;

import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.interpreter.objects.networking.RontoClient;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class ClientMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("start") || funcName.equals("join") || funcName.equals("connect")) {
                if(child.arguments.size == 3)
                    return new Reference(((RontoClient)parent.value).join(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), (int)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value), interpreter));
                else
                    return new Reference(((RontoClient)parent.value).join(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), (int)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value), (int)Variable.getNum(Executor.execute(child.arguments.get(3), interpreter, ownerClass, instanceBlock).value), (int)Variable.getNum(Executor.execute(child.arguments.get(4), interpreter, ownerClass, instanceBlock).value), interpreter));
            }else if (funcName.equals("send")) {
                ((RontoClient)parent.value).client.sendTCP(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
                return null;
            }
        }
        return SocketMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
    }
}
