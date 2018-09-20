package com.rontoking.rontolang.interpreter.members.networking;

import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class ConnectionMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if(child.type == Instruction.Type.GetVariable){
            if(child.data.equals("close") || child.data.equals("disc") || child.data.equals("disconnect")){
                ((Connection)parent.value).close();
                return null;
            }else if(child.data.equals("id") || child.data.equals("num") || child.data.equals("index")){
                return new Reference(((Connection)parent.value).getID());
            }
        }
        return null;
    }
}
