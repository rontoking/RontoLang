package com.rontoking.rontolang.interpreter.members.networking;

import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.interpreter.objects.networking.RontoPacket;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class PacketMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if(child.type == Instruction.Type.GetVariable){
            if(child.data.equals("conn") || child.data.equals("isConn") || child.data.equals("isConnection")){
                return new Reference(((RontoPacket)parent.value).type == RontoPacket.Type.Connected);
            }else if(child.data.equals("disc") || child.data.equals("isDisc") || child.data.equals("isDisconnection")){
                return new Reference(((RontoPacket)parent.value).type == RontoPacket.Type.Disconnected);
            }else if(child.data.equals("norm") || child.data.equals("isNorm") || child.data.equals("isNormalMessage")){
                return new Reference(((RontoPacket)parent.value).type == RontoPacket.Type.Received);
            }else if(child.data.equals("data") || child.data.equals("obj") || child.data.equals("object") || child.data.equals("value")){
                return new Reference(((RontoPacket)parent.value).data);
            }else if(child.data.equals("sender")){
                return new Reference(((RontoPacket)parent.value).sender);
            }
        }
        return null;
    }
}
