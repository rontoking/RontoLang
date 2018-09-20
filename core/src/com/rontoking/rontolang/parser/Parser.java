package com.rontoking.rontolang.parser;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.interpreter.Variable;
import com.rontoking.rontolang.program.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class Parser {
    public static Program parseProgram(String block, String mainClassName, String mainFuncName){
        if(block.trim().equals("")){
            return new Program();
        }
        Program program = new Program();
        block = removeComments(block);
        block = getProgramMainImplications(block, mainClassName, mainFuncName);
        Marker marker = new Marker(block);
        while(!marker.isAtCodeEnd()) {
            marker.getAllAttributes(); // class NAME, optionally followed by extends PARENT
            String className = marker.memory.get(1);
            String parentClass = null;
            if(marker.memory.size == 4){
                parentClass = marker.memory.get(3);
            }
            String classBlock = marker.getBlock();
            program.classes.add(parseClass(className, parentClass, classBlock));
        }
        return program;
    }

    private static String removeComments(String block){
        Marker marker = new Marker(block);
        while (marker.getComment())
            marker.removeSelection();
        return marker.code;
    }

    private static String getProgramMainImplications(String block, String mainClassName, String mainFuncName) {
        block = block.trim();
        Marker marker = new Marker(block);
        marker.getAllTokens(false);
        if (marker.memory.get(0).equals("class"))
            return block; // Nothing is implied.
        for (int i = 0; i < marker.memory.size - 2; i++) {
            if ((marker.memory.get(i).equals("static") && marker.memory.get(i + 1).equals("main") && Expression.isParenStart(marker.memory.get(i + 2))) || (marker.memory.get(i).equals("static") && marker.memory.get(i + 1).equals("update") && Expression.isParenStart(marker.memory.get(i + 2))))
                return "class " + mainClassName + "{" + block; // Main class is implied.
        }
        return "class " + mainClassName + "{static " + mainFuncName + "(){" + block; // Main class and main function are both implied.
    }

    private static Class parseClass(String name, String parent, String block){
        Class c = new Class(name, parent);
        Marker marker = new Marker(block);
        while(!marker.isAtCodeEnd()) {
            marker.getAllAttributes(); // GetVariable the function's or property's attributes.
            if(marker.isAtBlockStart()){ // Function
                String paramBlock = marker.getBlock(); // The parameters.
                String funcBlock = marker.getBlock(); // The expressions.
                if(marker.inMemory("static"))
                    c.classFunctions.add(parseFunc(name, marker.memory, paramBlock, funcBlock));
                else
                    c.objectFunctions.add(parseFunc(name, marker.memory, paramBlock, funcBlock));
                marker.setup();
            }else{ // Property
                if(marker.mark() == ';'){ // Declaration
                    marker.end++;
                    if(marker.inMemory("static"))
                        c.classProperties.add(parseProp(marker.memory, ""));
                    else
                        c.objectProperties.add(parseProp(marker.memory, ""));
                }else if(marker.mark() == '='){ // Assignment
                    marker.end++;
                    if(marker.inMemory("static"))
                        c.classProperties.add(parseProp(marker.memory, marker.reachEndOfLine()));
                    else
                        c.objectProperties.add(parseProp(marker.memory, marker.reachEndOfLine()));
                }else{ // Syntax error
                    ErrorHandler.throwPropertyError(name);
                }
                marker.setup();
            }
        }
        return c;
    }

    private static Property parseProp(Array<String> attr, String value){
        Property prop = new Property(attr.get(attr.size - 1));
        parseAttr(prop, attr);
        if(value.equals(""))
            prop.value = new Instruction(Instruction.Type.Raw_Value, Variable.getDefaultValue(prop.type));
        else
            prop.value = Expression.parseExpr(value, false).getInstruction().arguments.get(1);
        return prop;
    }

    private static Function parseFunc(String className, Array<String> attr, String params, String block){
        Function func = new Function(attr.get(attr.size - 1));
        parseAttr(func, attr);
        func.parameters = parseParams(className, attr.get(attr.size - 1), params);
        if(!block.trim().equals("")) {
            Expression expression = Expression.parseExpr(block, true);
            for(int i = 0; i < expression.arguments.size; i++)
                func.code.add(expression.arguments.get(i).getInstruction());
        }
        return func;
    }

    private static void parseAttr(Function func, Array<String> attr){
        for(int i = 0; i < attr.size - 1; i++){
            if(attr.get(i).equals("public"))
                func.access = Function.Access.Public;
            else if(attr.get(i).equals("private"))
                func.access = Function.Access.Private;
            else if(!attr.get(i).equals("static"))
                func.type = attr.get(i);
        }
    }

    private static void parseAttr(Property prop, Array<String> attr) {
        for (int i = 0; i < attr.size - 1; i++) {
            if (attr.get(i).equals("public"))
                prop.access = Function.Access.Public;
            else if (attr.get(i).equals("private"))
                prop.access = Function.Access.Private;
            else if (!attr.get(i).equals("static"))
                prop.type = attr.get(i);
        }
    }

    private static Array<Parameter> parseParams(String className, String funcName, String block){
        Array<Parameter> params = new Array<Parameter>();
        String[] parts = block.split(",");
        for(int i = 0; i < parts.length; i++){
            String[] param = getParamParts(parts[i].trim());
            if(param.length == 1) { // Parameter type is non-mandatory. It is used for developers who prefer strict typing, as it can give an error.
                if (!param[0].trim().equals(""))
                    params.add(new Parameter("", false, param[0].trim()));
            }
            else if(param.length == 2){
                if(param[0].trim().equals("&"))
                    params.add(new Parameter("", true, param[1].trim()));
                else
                    params.add(new Parameter(param[0].trim(), false, param[1].trim()));
            }else if(param.length == 3 && param[1].trim().equals("&")){
                params.add(new Parameter(param[0].trim(), true, param[2].trim()));
            }else{
                ErrorHandler.throwParamError(className, funcName);
            }
        }
        return params;
    }

    private static String[] getParamParts(String param){
        if(param.split("&").length > 1)
            return new String[]{param.split("&")[0], "&", param.split("&")[1]};
        return param.split(" ");
    }
}
