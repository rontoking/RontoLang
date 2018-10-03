package com.rontoking.rontolang.interpreter.members;

import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class StringMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("sub") ||funcName.equals("substr") || funcName.equals("substring")) {
                if (child.arguments.size == 2) {
                    return new Reference((parent.value.toString()).substring((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock))));
                } else if (child.arguments.size == 3) {
                    return new Reference((parent.value.toString()).substring((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock)), (int) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock))));
                }
            } else if (funcName.equals("charAt")) {
                return new Reference((parent.value.toString()).charAt((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock))));
            } else if (funcName.equals("replace")) {
                return new Reference((parent.value.toString()).replace(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value.toString()));
            } else if (funcName.equals("split")) {
                if (child.arguments.size == 1)
                    return new Reference((parent.value.toString()).split(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
                else
                    return new Reference((parent.value.toString()).split(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), (int) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock))));
            } else if (funcName.equals("indexOf")) {
                return new Reference((parent.value.toString()).indexOf(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
            } else if (funcName.equals("lastIndexOf")) {
                return new Reference((parent.value.toString()).lastIndexOf(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
            } else if (funcName.equals("contains")) {
                return new Reference((parent.value.toString()).contains(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
            } else if (funcName.equals("startsWith")) {
                String start = Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString();
                return new Reference((parent.value.toString()).length() >= start.length() && (parent.value.toString()).substring(0, start.length()).equals(start));
            }else if (funcName.equals("endsWith")) {
                String end = Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString();
                return new Reference((parent.value.toString()).length() >= end.length() && (parent.value.toString()).substring((parent.value.toString()).length() - end.length()).equals(end));
            }
        } else if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("size") || child.data.equals("count") || child.data.equals("num") || child.data.equals("length") || child.data.equals("n") || child.data.equals("chars"))
                return new Reference((parent.value.toString()).length());
            else if (child.data.equals("trim"))
                return new Reference((parent.value.toString()).trim());
            else if (child.data.equals("toUpper") || child.data.equals("toUp") || child.data.equals("getUpper") || child.data.equals("getUp"))
                return new Reference((parent.value.toString()).toUpperCase());
            else if (child.data.equals("toLower") || child.data.equals("toLow") || child.data.equals("getLower") || child.data.equals("getLow"))
                return new Reference((parent.value.toString()).toLowerCase());
            else if (child.data.equals("list") || child.data.equals("array"))
                return new Reference(Variable.toCharArray((parent.value.toString())));
        }
        return null;
    }
}
