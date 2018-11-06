package com.rontoking.rontolang.interpreter.members;

import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class BoolMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("flip") || child.data.equals("reverse"))
                parent.value = !(Boolean)parent.value;
                return parent;
        }
        return null;
    }
}
