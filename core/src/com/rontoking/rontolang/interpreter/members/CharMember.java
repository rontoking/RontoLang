package com.rontoking.rontolang.interpreter.members;

import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class CharMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("isUpper") || child.data.equals("isUp"))
                return new Reference(Character.isUpperCase(((Character) parent.value)));
            else if (child.data.equals("isLower") || child.data.equals("isLow"))
                return new Reference(Character.isLowerCase(((Character) parent.value)));
            else if (child.data.equals("toUpper") || child.data.equals("toUp") || child.data.equals("getUpper") || child.data.equals("getUp") || child.data.equals("up"))
                return new Reference(Character.toUpperCase(((Character) parent.value)));
            else if (child.data.equals("toLower") || child.data.equals("toLow") || child.data.equals("getLower") || child.data.equals("getLow") || child.data.equals("low"))
                return new Reference(Character.toLowerCase(((Character) parent.value)));
            else if (child.data.equals("isNum") || child.data.equals("isDigit"))
                return new Reference(Character.isDigit(((Character) parent.value)));
            else if (child.data.equals("isWhitespace") || child.data.equals("isSpace"))
                return new Reference(Character.isWhitespace(((Character) parent.value)));
            else if (child.data.equals("isLetter"))
                return new Reference(Character.isLetter(((Character) parent.value)));
        }
        return null;
    }
}
