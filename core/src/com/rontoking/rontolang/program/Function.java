package com.rontoking.rontolang.program;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.program.Instruction;

public class Function {
    public enum Access{
        Public, Private
    }

    public String name;
    public String type;
    public Access access;
    public Array<Parameter> parameters;
    public Array<Instruction> code;

    public Function(String name){
        this.name = name;
        this.type = "";
        this.access = Access.Public;
        this.parameters = new Array<Parameter>();
        this.code = new Array<Instruction>();
    }
}
