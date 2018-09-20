package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class Event {
    public Instruction cond; // Condition event checks for.
    public boolean isRepeated; // Is it a whenever or a when?
    public Array<Instruction> block; // Executed if condition is true.

    private Class ownerClass;
    private Block instanceBlock;

    public Event(Instruction cond, boolean isRepeated, Array<Instruction> block, Class ownerClass, Block instanceBlock){
        this.cond = cond;
        this.isRepeated = isRepeated;
        this.block = block;

        this.ownerClass = ownerClass;
        this.instanceBlock = instanceBlock;
    }

    public boolean check(Interpreter interpreter){ // Returns whether it should be removed or not.
        if((Boolean)Executor.execute(cond, interpreter, ownerClass, instanceBlock).value){
            Executor.executeBlock(block, interpreter, ownerClass, null, null, instanceBlock);
            return !isRepeated;
        }
        return false;
    }
}
