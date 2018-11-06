package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class Timer {
    private double totalSeconds;
    public double secondsLeft; // Condition event checks for.
    public boolean isRepeated; // Is it a whenever or a when?
    public Array<Instruction> block; // Executed if condition is true.

    private Class ownerClass;
    private Block instanceBlock;

    public Timer(double secondsLeft, boolean isRepeated, Array<Instruction> block, Class ownerClass, Block instanceBlock){
        this.secondsLeft = secondsLeft;
        this.totalSeconds = secondsLeft;
        this.isRepeated = isRepeated;
        this.block = block;

        this.ownerClass = ownerClass;
        this.instanceBlock = instanceBlock;
    }

    public boolean check(Interpreter interpreter){ // Returns whether it should be removed or not.
        secondsLeft -= Gdx.graphics.getRawDeltaTime();
        if(secondsLeft <= 0){
            Executor.executeBlock(block, interpreter, ownerClass, null, null, instanceBlock);
            secondsLeft = totalSeconds;
            return !isRepeated;
        }
        return false;
    }
}
