package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;
import com.rontoking.rontolang.rontoui.RontoUI;

public class FontMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("width") || funcName.equals("w")) {
                return new Reference(RontoUI.getTextWidth(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), (BitmapFont)parent.value));
            }else if (funcName.equals("height") || funcName.equals("h")) {
                return new Reference(RontoUI.getTextHeight(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), (BitmapFont)parent.value));
            }
        }
        return null;
    }
}
