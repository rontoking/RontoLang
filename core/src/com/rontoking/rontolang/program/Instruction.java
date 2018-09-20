package com.rontoking.rontolang.program;

import com.badlogic.gdx.utils.Array;

public class Instruction {
    public enum Type{
        Raw_Value, Element, List, Map,
        Print, Println,
        SetVariable, GetVariable,
        Function,
        Sum, Difference, Product, Quotient, Remainder, Power, Member,
        If, Else, Else_If, While, Repeat, Switch, Case, For, Foreach, When, Whenever, Thread,
        Equal, Not_Equal, Not, Greater, Lesser, Greater_Or_Equal, Lesser_Or_Equal, And, Or, Xor,
        Comment,
        Exec, Wait, TypeOf,
        Pair, Implies, Concat,
        RunLater,
        Random, Abs, Atan, Atan2, Sin, Cos, Tan, Sinh, Cosh, Tanh, Round, Ceil, Floor, Asin, Acos, Max, Min, Sqrt, ToDeg, ToRad,
        Pointer, Copy, Super,
        Return, Empty,
        Img, File, Font, Sound, Music, String, Str, Int, Float, Double,
        Internal, Local, External, Absolute, Classpath,
        Color, Point, Sprite, Rect, Camera2d,
        Serialize, Deserialize, Parse,
        KeyDown, KeyPressed, MousePressed, MouseClicked, MouseDown, MouseReleased, MouseDouble,
        MoveMouse, Browse, Google,

        Draw, Fill, Dst, Noise, Path,
        Circle, Ellipse, Line, Polygon, ShapeColor
    }

    public Type type;
    public Array<Instruction> arguments;
    public Object data;

    public Instruction(Type type){
        this.type = type;
        this.arguments = new Array<Instruction>();
    }


    public Instruction(Type type , Array<Instruction> arguments){
        this.type = type;
        this.arguments = arguments;
    }

    public Instruction(Type type, Object data){
        this.type = type;
        this.data = data;
        this.arguments = new Array<Instruction>();
    }

    public static String codeToStr(Array<Instruction> code){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < code.size; i++){
            stringBuilder.append(code.get(i).toStr());
            stringBuilder.append(";");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String toStr(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(type.name());
        stringBuilder.append("(");
        for(int i = 0; i < arguments.size; i++) {
            if (i != 0)
                stringBuilder.append(", ");
            stringBuilder.append(arguments.get(i).toStr());
        }
        if(data != null)
            stringBuilder.append(data);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public static boolean isInstruction(String str){
        for(int i = 0; i < Type.values().length; i++)
            if(str.equals(Character.toLowerCase(Type.values()[i].name().charAt(0)) + Type.values()[i].name().substring(1)))
                return true;
        return false;
    }

    public static Type getType(String str){
        for(int i = 0; i < Type.values().length; i++)
            if(str.equals(Character.toLowerCase(Type.values()[i].name().charAt(0)) + Type.values()[i].name().substring(1)))
                return Type.values()[i];
        return null;
    }
}
