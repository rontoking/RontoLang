package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;
import com.rontoking.rontolang.rontoui.RontoUI;

public class MapMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("size") || child.data.equals("count") || child.data.equals("num") || child.data.equals("length") || child.data.equals("n"))
                return new Reference(((ObjectMap) parent.value).size);
            else if (child.data.equals("keys"))
                return Variable.getList(((ObjectMap) parent.value).keys().toArray());
            else if (child.data.equals("values"))
                return Variable.getList(((ObjectMap) parent.value).values().toArray());
            else if (child.data.equals("reverse")){
                ObjectMap map = (ObjectMap) parent.value;
                Array keys = map.keys().toArray();
                for(int i = 0; i < keys.size; i++){
                    Object newKey = map.get(keys.get(i));
                    Object newValue = keys.get(i);
                    map.remove(keys.get(i));
                    map.put(newKey, newValue);
                }
                return null;
            }
        }
        return null;
    }
}
