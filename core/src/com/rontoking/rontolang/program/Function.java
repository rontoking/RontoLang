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

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("func(").append(access.name().toLowerCase()).append(", ").append(type).append(", ").append(name).append(", (");
        for(int i = 0; i < parameters.size; i++){
            if(i != 0)
                stringBuilder.append(", ");
            stringBuilder.append(parameters.get(i).toString());
        }
        stringBuilder.append("), {");
        for(int i = 0; i < code.size; i++){
            if(i != 0)
                stringBuilder.append(", ");
            stringBuilder.append(code.get(i).toString());
        }
        stringBuilder.append("})");
        return stringBuilder.toString();
    }
}
