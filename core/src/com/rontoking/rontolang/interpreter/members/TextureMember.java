package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;
import com.rontoking.rontolang.rontoui.RontoUI;

public class TextureMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("w") || child.data.equals("width"))
                return new Reference(((Texture) parent.value).getWidth());
            else if (child.data.equals("h") || child.data.equals("height"))
                return new Reference(((Texture) parent.value).getHeight());
        }else if (child.type == Instruction.Type.Draw) {
            interpreter.setToSpriteState();
            interpreter.spriteBatch.draw((Texture)parent.value, (float)Variable.getNum(Executor.execute(child.arguments.get(0), interpreter, ownerClass, instanceBlock).value), (float)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value));
            return null;
        }
        return null;
    }
}
