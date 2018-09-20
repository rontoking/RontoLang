package com.rontoking.rontolang.parser;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.program.Instruction;

public class Expression {
    private static String[][] EXPRESSION_PRIORITY = {
            {"=", "+=", "-=", "*=", "/="},
            {":"},
            {"=>", "->"},
            {"&&", "||", "^^"},
            {"==", "!=", "<=", ">=", "<", ">"},
            {"+", "-"},
            {"*", "/", "%"},
            {"^"},
            {"!"},
            {"."},
    };

    public String type, data;
    public Array<Expression> arguments;

    public Expression(String type){
        this.type = type;
        this.arguments = new Array<Expression>();
        this.data = "";
    }

    public Expression(String type, Array<Expression> arguments){
        this.type = type;
        this.arguments = arguments;
        this.data = "";
    }

    public Expression(String type, String data){
        this.type = type;
        this.arguments = new Array<Expression>();
        this.data = data;
    }

    public Expression(String type, Array<Expression> arguments, String data){
        this.type = type;
        this.arguments = arguments;
        this.data = data;
    }

    public static Expression parseExpr(String block, boolean isLocal){
        Marker marker = new Marker(block);
        marker.getAllTokens(isLocal);
        ErrorHandler.checkBrackets(marker.memory);
        addPriorityToTokens(marker.memory);
        Expression expression = parseExprParentheses(marker.memory);
        replacePointers(expression);
        return expression;
    }

    private static void replacePointers(Expression expression){
        if(expression.type.equals("*") && expression.arguments.get(0).type.equals("Block") && expression.arguments.get(0).arguments.size == 0 && expression.arguments.get(0).data.equals("")){
            expression.arguments.removeIndex(0);
            expression.type = "pointer";
        }
        for(int i = 0; i < expression.arguments.size; i++)
            replacePointers(expression.arguments.get(i));
    }

    private static void addPriorityToTokens(Array<String> tokens){
        for(int opIndex = 0; opIndex < EXPRESSION_PRIORITY.length; opIndex++) {
            int targetLevel = 1;
            while (containOperator(tokens, EXPRESSION_PRIORITY[opIndex])) {
                foundAnyOperator(tokens, targetLevel, opIndex);
                targetLevel++;
            }
        }
        for(int i = 0; i < tokens.size; i++)
            if(tokens.get(i).length() > 3 && tokens.get(i).substring(0, 3).equals("^^^"))
                tokens.set(i, tokens.get(i).substring(3));
    }

    private static boolean containOperator(Array<String> tokens, String[] operators){
        for(int i = 0; i < tokens.size; i++)
            if(contains(operators, tokens.get(i)))
                return true;
        return false;
    }

    private static boolean foundAnyOperator(Array<String> tokens, int targetLevel, int opIndex){
        if(opIndex == 0){
            if(addedPriority(tokens, targetLevel, opIndex, 0, 1)){
                while (addedPriority(tokens, targetLevel, opIndex, 0, 1)){

                }
                return true;
            }
            return false;
        }
        if(addedPriority(tokens, targetLevel, opIndex, tokens.size - 1, -1)){
            while (addedPriority(tokens, targetLevel, opIndex, tokens.size - 1, -1));
            return true;
        }
        return false;
    }

    private static boolean addedPriority(Array<String> tokens, int targetLevel, int opIndex, int index, int delta){
        int currentLevel = 0;
        while(index >= 0 && index < tokens.size){
            if(isParenStart(tokens.get(index)))
                currentLevel++;
            else if(isParenEnd(tokens.get(index)))
                currentLevel--;
            if(Math.abs(currentLevel) == targetLevel && contains(EXPRESSION_PRIORITY[opIndex], tokens.get(index))){ // Is the operator both of the correct priority and level.
                addPriority(tokens, index, opIndex);
                return true;
            }
            index += delta;
        }
        return false;
    }

    private static void addPriority(Array<String> tokens, int index, int opIndex){
        if(opIndex == 8){
            addParenOnSide(tokens, index, 1);
            tokens.set(index, "^^^!");
        }else {
            addParenOnSide(tokens, index, 1);
            int pos = addParenOnSide(tokens, index, -1); // Position to move the operator.
            index += 2; // Because two paren were added behind the operator.
            String operator = tokens.get(index);
            tokens.removeIndex(index);
            tokens.insert(pos, "^^^" + operator);
        }
    }

    private static int addParenOnSide(Array<String> tokens, int startIndex, int sideDelta){
        int functionElementLevel = 0; // For situations like func()[0].
        if(sideDelta == 1)
            tokens.insert(startIndex + 1, "(");
        else {
            tokens.insert(startIndex, ")");
            startIndex++; // Update the operator's index.
        }
        String target = "(";
        if(sideDelta == 1)
            target = ")";
        int currentLevel = 1;
        while(startIndex >= 0 && startIndex < tokens.size){
            if((sideDelta == 1 && isParenStart(tokens.get(startIndex))) || (sideDelta == -1 && isParenEnd(tokens.get(startIndex)))) // when delta 1 we stop at )
                currentLevel--;
            else if((sideDelta == 1 && isParenEnd(tokens.get(startIndex))) || (sideDelta == -1 && isParenStart(tokens.get(startIndex)))){
                currentLevel++;
                if(currentLevel == 1){ // end
                    tokens.insert(startIndex, target);
                    if(sideDelta == 1)
                        return startIndex + 1;
                    return startIndex;
                }
            }
            startIndex += sideDelta;
        }
        if(startIndex == -1) {
            tokens.insert(0, target);
            return 0;
        }
        tokens.add(target);
        return tokens.size - 1;
    }

    private static Expression parseExprParentheses(Array<String> tokens){ // Creates an expression tree using any parenthesis tokens.
        Expression expression = new Expression("Code", "");
        int position = 0; // Where on the expression tree we are on.
        for(int i = 0; i < tokens.size; i++){
            if(isParenStart(tokens.get(i))) { // An expression in the form of (arguments and name) <= we don't know its type yet. The priority parser will figure it out.
                addArgumentToExprAtPos(expression, position, 0, "");
                if(tokens.get(i).equals("{")){
                    position++;
                    getExprAtPos(expression, position, 0).type = "List";
                }else
                    position++;
            }
            else if(isParenEnd(tokens.get(i))) {
                if(tokens.get(i).equals(")")  && i < tokens.size - 1 && tokens.get(i + 1).equals("[")){ // List element of a function or raw array. like func()[0]. Places the Element on the position where the function was and makes the function its first child.
                    Expression func = getExprAtPos(expression, position, 0);
                    position--; // Goes to the position of the function's parent.
                    getExprAtPos(expression, position, 0).arguments.removeIndex(getExprAtPos(expression, position, 0).arguments.size - 1); // removes the function.
                    addArgumentToExprAtPos(expression, position, 0, ""); // Adds the Element expression.
                    position++; // Goes to the Element expression.
                    getExprAtPos(expression, position, 0).type = "Element"; // Sets its type to Element.
                    getExprAtPos(expression, position, 0).arguments.add(func); // Adds the function as a child.

                    i++; // Skips the '('.
                }
                else if(tokens.get(i).equals("]") && i < tokens.size - 1 && tokens.get(i + 1).equals("[")) { // Chained array element indexes.
                    i++;
                }else
                    position--;
            }
            else if((isVarName(tokens.get(i)) || contains(EXPRESSION_PRIORITY, tokens.get(i))) && i < tokens.size - 1 && isParenStart(tokens.get(i + 1))) { // An expression in the form of name(arguments)
                if(tokens.get(i + 1).equals("[")) { // Element
                    addArgumentToExprAtPos(expression, position, 0, tokens.get(i));
                    position++;
                    getExprAtPos(expression, position, 0).type = "Element";
                }else if(tokens.get(i + 1).equals("{")) { // List
                    addArgumentToExprAtPos(expression, position, 0, tokens.get(i));
                    position++;
                    getExprAtPos(expression, position, 0).type = "List";
                }
                else {
                    addArgumentToExprAtPos(expression, position, 0, "");
                    position++;
                    getExprAtPos(expression, position, 0).type = tokens.get(i);
                }
                i++; // To skip the parenthesis.
            }else{ // Direct arguments of an expression.
                addArgumentToExprAtPos(expression, position, 0, tokens.get(i));
            }
        }
        return expression;
    }

    private static void addArgumentToExprAtPos(Expression parent, int position, int level, String data){
        if(level == position)
            parent.arguments.add(new Expression("Block", data));
        else
            addArgumentToExprAtPos(parent.arguments.get(parent.arguments.size - 1), position, level + 1, data);
    }

    private static Expression getExprAtPos(Expression parent, int position, int level){
        if(level == position)
            return parent;
        else
            return getExprAtPos(parent.arguments.get(parent.arguments.size - 1), position, level + 1);
    }

    private static boolean contains(String[] arr, String str){
        for(int i = 0; i < arr.length; i++)
            if(arr[i].equals(str))
                return true;
        return false;
    }

    private static boolean contains(String[][] arr, String str){
        for(int i = 0; i < arr.length; i++)
            if(contains(arr[i], str))
                return true;
        return false;
    }

    public static boolean isParenStart(String s){ // Whether the current position is the start of a block.
        return s.equals("(") || s.equals("[") || s.equals("{");
    }

    public static boolean isParenEnd(String s){ // Whether the current position is the end of a block.
        return s.equals(")") || s.equals("]") || s.equals("}");
    }

    private static boolean isVarName(String s){
        return s.length() > 0 && (Character.isLetter(s.charAt(0)) || s.charAt(0) == '_');
    }

    public String toStr(){
        StringBuilder sb = new StringBuilder();
        if(data.equals("")) {
            sb.append(type).append("(");
        }
        else
            sb.append(data);
        for(int i = 0; i < arguments.size; i++) {
            if (i != 0)
                sb.append(", ");
            sb.append(arguments.get(i).toStr());
        }
        if(data.equals(""))
            return sb.toString() + ")";
        return sb.toString();
    }

    public Instruction getInstruction(){
        Array<Instruction> args = new Array<Instruction>();
        if(Instruction.isInstruction(type)){
            for(int i = 0; i < arguments.size; i++)
                args.add(arguments.get(i).getInstruction());
            return new Instruction(Instruction.getType(type), args);
        } else if (".".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Member, args);
        } else if ("+".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Sum, args);
        } else if (":".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Pair, args);
        } else if ("->".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Implies, args);
        } else if ("=>".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Implies, args);
        } else if ("-".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Difference, args);
        } else if ("/".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Quotient, args);
        } else if ("*".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Product, args);
        } else if ("%".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Remainder, args);
        } else if ("^".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Power, args);
        } else if ("=".equals(type)) {
            Instruction var = arguments.get(0).getInstruction();
            if (var.type == Instruction.Type.List) {
                args.add(var.arguments.get(0));
                args.add(var.arguments.get(1));
            } else {
                args.add(new Instruction(Instruction.Type.Raw_Value, ""));
                args.add(var);
            }
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.SetVariable, args);
        } else if ("+=".equals(type)) {
            args.add(new Instruction(Instruction.Type.GetVariable, ""));
            args.add(arguments.get(0).getInstruction());
            args.add(new Instruction(Instruction.Type.Sum));
            args.get(2).arguments.add(arguments.get(0).getInstruction());
            args.get(2).arguments.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.SetVariable, args);
        } else if ("-=".equals(type)) {
            args.add(new Instruction(Instruction.Type.GetVariable, ""));
            args.add(arguments.get(0).getInstruction());
            args.add(new Instruction(Instruction.Type.Difference));
            args.get(2).arguments.add(arguments.get(0).getInstruction());
            args.get(2).arguments.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.SetVariable, args);
        } else if ("*=".equals(type)) {
            args.add(new Instruction(Instruction.Type.GetVariable, ""));
            args.add(arguments.get(0).getInstruction());
            args.add(new Instruction(Instruction.Type.Product));
            args.get(2).arguments.add(arguments.get(0).getInstruction());
            args.get(2).arguments.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.SetVariable, args);
        } else if ("/=".equals(type)) {
            args.add(new Instruction(Instruction.Type.GetVariable, ""));
            args.add(arguments.get(0).getInstruction());
            args.add(new Instruction(Instruction.Type.Quotient));
            args.get(2).arguments.add(arguments.get(0).getInstruction());
            args.get(2).arguments.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.SetVariable, args);
        } else if ("==".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Equal, args);
        } else if ("!=".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Not_Equal, args);
        } else if ("<=".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Lesser_Or_Equal, args);
        } else if (">=".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Greater_Or_Equal, args);
        } else if ("<".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Lesser, args);
        } else if (">".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Greater, args);
        } else if ("&&".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.And, args);
        } else if ("||".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Or, args);
        } else if ("^^".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Xor, args);
        } else if ("!".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            return new Instruction(Instruction.Type.Not, args);
        } else if ("if".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.If, args);
        } else if ("runLater".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.RunLater, args);
        } else if ("switch".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Switch, args);
        } else if ("case".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Case, args);
        } else if ("for".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.For, args);
        } else if ("foreach".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Foreach, args);
        } else if ("when".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.When, args);
        } else if ("whenever".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Whenever, args);
        } else if ("thread".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Thread, args);
        } else if ("while".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.While, args);
        } else if ("repeat".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Repeat, args);
        } else if ("else".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            return new Instruction(Instruction.Type.Else, args);
        } else if ("else if".equals(type)) {
            args.add(arguments.get(0).getInstruction());
            args.add(arguments.get(1).getInstruction());
            return new Instruction(Instruction.Type.Else_If, args);
        } else if ("Element".equals(type)) {
            if (!data.equals(""))
                args.add(new Instruction(Instruction.Type.GetVariable, data));
            for (int i = 0; i < arguments.size; i++)
                args.add(arguments.get(i).getInstruction());
            return new Instruction(Instruction.Type.Element, args);
        } else if ("List".equals(type)) {
            for (int i = 0; i < arguments.size; i++)
                args.add(arguments.get(i).getInstruction());
            return new Instruction(Instruction.Type.List, args);
        } else if ("Block".equals(type)) {
            if (arguments.size == 0) {
                if (data.length() == 0)
                    return new Instruction(Instruction.Type.Empty);
                if (data.charAt(0) == '"' || data.charAt(0) == '\'')
                    return new Instruction(Instruction.Type.Raw_Value, parsedString(data));
                if (data.equals("true") || data.equals("false")) // Bool
                    return new Instruction(Instruction.Type.Raw_Value, Boolean.parseBoolean(data));
                if (data.charAt(0) == '_' || Character.isLetter(data.charAt(0))) // Variable
                    return new Instruction(Instruction.Type.GetVariable, data);
                return new Instruction(Instruction.Type.Raw_Value, parsedNum(data)); // Number
            }
            if (arguments.size == 1) {
                return arguments.get(0).getInstruction();
            }
            for (int i = 0; i < arguments.size; i++) {
                args.add(arguments.get(i).getInstruction());
            }
            if(args.size > 0 && args.get(0).type == Instruction.Type.Pair)
                return new Instruction(Instruction.Type.Map, args); // If it uses the pair instruction (x : y), it's a map.
            return new Instruction(Instruction.Type.List, args); // If it's nothing else, it's an array.
        } else {
            args.add(new Instruction(Instruction.Type.GetVariable, type));
            for (int i = 0; i < arguments.size; i++)
                args.add(arguments.get(i).getInstruction());
            return new Instruction(Instruction.Type.Function, args);
        }
    }

    private static Object parsedString(String str){ // Removes all escape characters (the first of every sequence of backslashes).
        boolean isInSequence = false;
        StringBuilder sb = new StringBuilder(str);
        for(int i = str.length() - 1; i >= 0; i--){
            if(sb.charAt(i) == '\\' && !isInSequence){
                if(sb.charAt(i + 1) == 'n'){
                    isInSequence = false;
                    sb.setCharAt(i + 1, '\n');
                }else if(sb.charAt(i + 1) == 'r'){
                    isInSequence = false;
                    sb.setCharAt(i + 1, '\r');
                }else if(sb.charAt(i + 1) == 't'){
                    isInSequence = false;
                    sb.setCharAt(i + 1, '\t');
                }else if(sb.charAt(i + 1) == 'b'){
                    isInSequence = false;
                    sb.setCharAt(i + 1, '\b');
                }
                isInSequence = true;
                sb.deleteCharAt(i);
            }else if(sb.charAt(i) != '\\') {
                isInSequence = false;
            }else if(isInSequence){
                isInSequence = false;
            }
        }
        if(sb.toString().length() == 3)
            return sb.toString().charAt(1);
        return sb.toString().substring(1, sb.length() - 1);
    }

    private static Object parsedNum(String num){
        try {
            int x = Integer.parseInt(num);
            return x;
        }catch (Exception e){
            try {
                float x = Float.parseFloat(num);
                return x;
            }catch (Exception ex){
                return Double.parseDouble(num);
            }
        }
    }
}
