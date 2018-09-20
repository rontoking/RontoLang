package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class ErrorHandler {
    public enum Errors{
        MISSING_PROPERTY_DEPENDENCY
    }

    public static void throwInstructionError(Instruction.Type type){
        try {
            throw new Exception("No execution code implemented for instruction: " + type.name());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwPreferencesError(){
        try {
            throw new Exception("Cannot use preferences until they have been opened using 'prefs.open([string])'.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwVarTypeError(String type){
        try {
            throw new Exception("Invalid variable type: " + type);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwSerializeError(String type){
        try {
            throw new Exception("Type cannot be serialized: " + type);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwDeserializeError(){
        try {
            throw new Exception("Cannot deserialize object.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwPropertyError(){
        try {
            throw new Exception("Property dependencies are unresolvable.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwVarError(String name){
        try {
            throw new Exception("Variable doesn't exist: " + name);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwCaseError(){
        try {
            throw new Exception("Case statement cannot be outside of Switch statement.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwOperationError(Instruction.Type operation, String type1, String type2){
        try {
            throw new Exception("Incorrect type of values used for the '" + operation + "' operation: '" + type1 + "' and '" + type2 + "'.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwTypeValueError(String name, String type, Object value){
        try {
            throw new Exception("Variable " + name + "'s value of " + value + " does not match its type of " + type);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwMouseError(String name){
        try {
            throw new Exception("There is no mouse button called: " + name);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwBreakError(){
        try {
            throw new Exception("Cannot use the break statement directly inside of a function.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwElementError(){
        try {
            throw new Exception("Cannot get element of value");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwFuncError(String className, String funcName, String[] paramTypes){
        try {
            throw new Exception("There is no function '" + funcName + "' in the class: " + className + " with parameters of types: " + arrToStr(paramTypes));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwInstanceError(String className){
        try {
            throw new Exception("There is no class with the name '" + className + "' for the creation of an instance.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwVoidError(String name, String funcName, int paramIndex){
        try {
            throw new Exception("Cannot use '" + name + "' as a function argument. Located at function call for '" + funcName + "' at parameter index: " + paramIndex + ".");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwMemberError(String parentType, String child){
        try {
            throw new Exception("Value of type '" + parentType + "' does not have a member called '" + child + "'.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwSetError(String name){
        try {
            throw new Exception("Cannot set value of instruction of type: " + name + ". Please use a variable name instead.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwAccessError(String name){
        try {
            throw new Exception("Variable '" + name + "' is private and cannot be accessed from the outside.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void throwMemberError(boolean isParent){
        try {
            if(isParent)
                throw new Exception("Incorrect usage of member operator parent.");
            else
                throw new Exception("Incorrect usage of member operator child.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static String arrToStr(String[] arr){
        StringBuilder sb = new StringBuilder("(");
        for(int i = 0; i < arr.length; i++){
            if(i != 0)
                sb.append(", ");
            sb.append(arr[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
