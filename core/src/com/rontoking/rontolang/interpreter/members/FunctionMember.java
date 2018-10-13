package com.rontoking.rontolang.interpreter.members;

import com.rontoking.rontolang.interpreter.Block;
import com.rontoking.rontolang.interpreter.Executor;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Function;
import com.rontoking.rontolang.program.Instruction;

public class FunctionMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("run")) {
                Function func = (Function)parent.value;
                Reference[] params = new Reference[child.arguments.size - 1];
                for(int i = 0; i < params.length; i++){
                    params[i] = Executor.execute(child.arguments.get(1 + i), interpreter, ownerClass, instanceBlock);
                }
                return Executor.executeBlock(func.code, interpreter, ownerClass, func, params, instanceBlock);
            }
        }
        return null;
    }
}
