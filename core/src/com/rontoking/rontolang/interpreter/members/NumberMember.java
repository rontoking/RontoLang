package com.rontoking.rontolang.interpreter.members;

import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class NumberMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("limit") || funcName.equals("lim")) {
                double val = Variable.getNum(parent.value);
                val = Math.max(val, Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value));
                val = Math.min(val, Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value));
                if(parent.value instanceof Integer)
                    parent.value = (int)val;
                else if(parent.value instanceof Float)
                    parent.value = (float)val;
                else
                    parent.value = val;
                return parent;
            }
        }else if(child.type == Instruction.Type.GetVariable){
            if(child.data.equals("round")){
                parent.value = (int)Math.round(Variable.getNum(parent.value));
                return parent;
            }else if(child.data.equals("ceil")){
                parent.value = (int)Math.ceil(Variable.getNum(parent.value));
                return parent;
            }else if(child.data.equals("floor")){
                parent.value = (int)Math.floor(Variable.getNum(parent.value));
                return parent;
            }
        }
        return null;
    }
}
