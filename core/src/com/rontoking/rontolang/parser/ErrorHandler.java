package com.rontoking.rontolang.parser;

import com.badlogic.gdx.utils.Array;

public class ErrorHandler {
    public static void checkBrackets(Array<String> tokens){
        int paren = 0;
        for(int i = 0; i < tokens.size; i++) {
            if (Expression.isParenStart(tokens.get(i)))
                paren++;
            else if (Expression.isParenEnd(tokens.get(i)))
                paren--;
        }
        if(paren > 0)
            try {
                throw new Exception("There are " + paren + " too many closing brackets");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        else if(paren < 0)
            try {
                throw new Exception("There are " + (-paren) + " too many opening brackets");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
    }

    public static void throwStringError(){
        try {
            throw new Exception("There is an unclosed string literal.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwPropertyError(String className){
        try {
            throw new Exception("Syntax error for class property. Class name: " + className);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwParamError(String className, String funcName){
        try {
            throw new Exception("Syntax error for function parameter. Class name: " + className + ", Function name: " + funcName);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
