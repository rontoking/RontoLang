package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.audio.Music;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class MusicMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if(child.type == Instruction.Type.GetVariable){
            if(child.data.equals("play"))
                ((Music) parent.value).play();
            if(child.data.equals("stop"))
                ((Music) parent.value).stop();
            if(child.data.equals("pause"))
                ((Music) parent.value).pause();
            if(child.data.equals("isLooping"))
                return new Reference(((Music) parent.value).isLooping());
            if(child.data.equals("getVolume"))
                return new Reference(((Music) parent.value).getVolume());
            if(child.data.equals("getPosition"))
                return new Reference(((Music) parent.value).getPosition());
            return null;
        }
        else if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("setLooping")) {
                ((Music)parent.value).setLooping((Boolean)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
                return null;
            }else if (funcName.equals("setVolume")) {
                ((Music)parent.value).setVolume((float) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value));
                return null;
            }else if (funcName.equals("setPosition")) {
                ((Music)parent.value).setPosition((float) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value));
                return null;
            }else if (funcName.equals("setPan")) {
                ((Music)parent.value).setPan((float) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (float) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value));
                return null;
            }
        }
        return null;
    }
}
