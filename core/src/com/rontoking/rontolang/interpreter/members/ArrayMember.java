package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class ArrayMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("size") || child.data.equals("count") || child.data.equals("num") || child.data.equals("length") || child.data.equals("len") || child.data.equals("n"))
                return new Reference(((Array) parent.value).size);
            else if (child.data.equals("shuffle")) {
                ((Array) parent.value).shuffle();
                return null;
            } else if (child.data.equals("reverse")) {
                ((Array) parent.value).reverse();
                return null;
            } else if (child.data.equals("random") || child.data.equals("rand")) {
                return ((Array<Reference>) parent.value).random();
            }
        } else if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("add")) {
                ((Array) parent.value).add(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock));
                return null;
            } else if (funcName.equals("remove")) {
                ((Array) parent.value).removeIndex((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock)));
                return null;
            } else if (funcName.equals("join")) {
                return new Reference(Variable.join(((Array) parent.value), Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
            } else if (funcName.equals("insert")) {
                ((Array) parent.value).insert((int) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock)), Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock));
                return null;
            }else if (funcName.equals("startsWith")) {
                Array<Reference> start = (Array<Reference>)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value;
                if(((Array) parent.value).size >= start.size){
                    Array<Reference> p = (Array<Reference>)parent.value;
                    for(int i = 0; i < start.size; i++){
                        if(!Variable.areEqual(p.get(i), start.get(i)))
                            return new Reference(false);
                    }
                    return new Reference(true);
                }
                return new Reference(false);
            }else if (funcName.equals("endsWith")) {
                Array<Reference> end = (Array<Reference>)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value;
                if(((Array) parent.value).size >= end.size){
                    Array<Reference> p = (Array<Reference>)parent.value;
                    for(int i = 0; i < end.size; i++){
                        if(!Variable.areEqual(p.get(p.size - end.size + i), end.get(i)))
                            return new Reference(false);
                    }
                    return new Reference(true);
                }
                return new Reference(false);
            }
        }
        return null;
    }
}
