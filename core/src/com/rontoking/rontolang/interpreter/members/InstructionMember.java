package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class InstructionMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("run")) {
                return Executor.executeBlock((Array<Instruction>)parent.value, interpreter, ownerClass, null, null, instanceBlock);
            }
        }
        return null;
    }
}
