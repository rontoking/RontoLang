package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.audio.Sound;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class SoundMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if(child.type == Instruction.Type.GetVariable){
            if(child.data.equals("play"))
                return new Reference(((Sound)parent.value).play());
            else if(child.data.equals("stop")) {
                ((Sound) parent.value).stop();
                return null;
            }
            else if(child.data.equals("loop"))
                return new Reference(((Sound)parent.value).loop());
        }
        else if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("play")) {
                if(child.arguments.size == 2)
                    return new Reference(((Sound)parent.value).play((float) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value)));
                return new Reference(((Sound)parent.value).play((float)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (float)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value), (float)Variable.getNum(Executor.execute(child.arguments.get(3), interpreter, ownerClass, instanceBlock).value)));
            }else if(funcName.equals("stop")){
                ((Sound)parent.value).stop((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value));
                return null;
            }else if (funcName.equals("loop")) {
                if(child.arguments.size == 2)
                    return new Reference(((Sound)parent.value).loop((float) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value)));
                return new Reference(((Sound)parent.value).loop((float)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (float)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value), (float)Variable.getNum(Executor.execute(child.arguments.get(3), interpreter, ownerClass, instanceBlock).value)));
            }else if (funcName.equals("setLooping")) {
                ((Sound)parent.value).setLooping((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value);
                return null;
            }else if (funcName.equals("setVolume")) {
                ((Sound)parent.value).setVolume((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (float) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value));
                return null;
            }else if (funcName.equals("setPitch")) {
                ((Sound)parent.value).setPitch((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (float) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value));
                return null;
            }else if (funcName.equals("setPan")) {
                ((Sound)parent.value).setPan((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), (float) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value), (float) Variable.getNum(Executor.execute(child.arguments.get(3), interpreter, ownerClass, instanceBlock).value));
                return null;
            }
        }
        return null;
    }
}
