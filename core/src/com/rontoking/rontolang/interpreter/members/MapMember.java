package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.interpreter.Variable;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class MapMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("size") || child.data.equals("count") || child.data.equals("num") || child.data.equals("length") || child.data.equals("n"))
                return new Reference(((ObjectMap) parent.value).size);
            else if (child.data.equals("keys"))
                return Variable.getList(((ObjectMap) parent.value).keys().toArray());
            else if (child.data.equals("values"))
                return Variable.getList(((ObjectMap) parent.value).values().toArray());
        }
        return null;
    }
}