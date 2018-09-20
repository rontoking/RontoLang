package com.rontoking.rontolang.parser;

import com.badlogic.gdx.utils.Array;

public class Marker {
    public int start, end;
    public String selection;
    public String code;
    public Array<String> memory;

    public Marker(String code){
        this.code = code;
        this.memory = new Array<String>();
        reset();
    }

    public String getVarName(){ // Can have letters, numbers, and _. Can't start with a number.
        setup();
        if(Character.isDigit(mark()))
            selection = "";
        else{
            while (end < code.length() && (Character.isLetter(mark()) || Character.isDigit(mark()) || mark() == '_'))
                end++;
            selection = code.substring(start, end); // Stops at a space or special character (+, -, ?, (, }, ., ...)
        }
        return selection;
    }

    public String getBlock() {
        setup();
        int level = 1;
        boolean insideString = false; // Used for not counting parentheses inside of a string.
        char strBound = ' ';
        while (level > 0) {
            end++;
            if (isAtCodeEnd()) // End of the code implies end of the block.
                level = 0;
            else {
                if (isStringStartOrEnd(insideString, strBound)) {
                    insideString = !insideString;
                    strBound = mark();
                }
                if (!insideString) {
                    if (isAtBlockStart())
                        level++;
                    else if (isAtBlockEnd())
                        level--;
                }
            }
        }
        selection = code.substring(start + 1, end); // Stops when the block end is reached, ex: { ... }
        end++;
        return selection;
    }

    public String reachEndOfLine() {
        setup();
        boolean insideString = false; // Used for not counting parentheses inside of a string.
        char strBound = ' ';
        while (!isAtCodeEnd() && (mark() != ';' || insideString)) {
            if (isStringStartOrEnd(insideString, strBound)) {
                insideString = !insideString;
                strBound = mark();
            }
            end++;
        }
        selection = code.substring(start, end); // Stops when the semicolon is reached.
        end++; // Skips the semicolon.
        return selection;
    }

    public boolean isAtBlockStart(){ // Whether the current position is the start of a block.
        return mark() == '(' || mark() == '[' || mark() == '{';
    }

    public boolean isAtBlockEnd(){ // Whether the current position is the end of a block.
        return mark() == ')' || mark() == ']' || mark() == '}';
    }

    public void reset(){
        this.start = 0;
        this.end = 0;
        this.selection = "";
        this.memory.clear();
    }

    public boolean isAtCodeEnd(){
        return end >= code.length();
    }

    public void getAllAttributes(){ // Adds every var name in a row to its memory (usually used for things like 'public static bool myVar')
        memory.clear();
        setup();
        while (mark() != '=' && mark() != ';' && mark() != '(' && mark() != '{') {
            addAttribute();
        }
    }

    private void addAttribute(){
        while (mark() != '=' && mark() != ';' && mark() != '(' && mark() != '{' && !Character.isWhitespace(mark()))
            end++;
        memory.add(code.substring(start, end));
        while (Character.isWhitespace(mark()))
            end++;
        setup();
    }

    public boolean inMemory(String str){
        for(int i = 0; i < memory.size; i++)
            if(memory.get(i).equals(str))
                return true;
        return false;
    }

    public boolean getComment(){
        setup();
        boolean insideString = false;
        char strBound = ' ';
        while(!isAtCodeEnd()){
            if(insideString){
                if (isStringStartOrEnd(insideString, strBound)) {
                    strBound = mark();
                    insideString = false;
                }
            }else{
                if (isStringStartOrEnd(insideString, strBound)) { // The beginning of a string.
                    insideString = true;
                    strBound = mark();
                }else if(isMarked("//")){ // One line comment, like this one :D
                    start = end;
                    while(!isAtCodeEnd() && mark() != '\n')
                        end++;
                    selection = code.substring(start, end);
                    return true;
                }else if(isMarked("/*")){ // Multiline comment.
                    while(!isAtCodeEnd() && !isMarked("*/"))
                        end++;
                    end += 2;
                    selection = code.substring(start, end);
                    return true;
                }
            }
            end++;
        }
        if(insideString)
            ErrorHandler.throwStringError();
        return false;
    }

    public void getAllTokens(boolean isLocal) { // Used on an instruction to separate it into its parts, which could also be expressions, or variable names or arrays or raw values.
        memory.clear();
        setup();
        boolean insideString = false;
        char strBound = ' ';
        boolean insideNumber = false;
        if (isLocal)
            memory.add("("); // The beginning of the first statement.
        while (!isAtCodeEnd()) {
            if (insideString) {
                if (isStringStartOrEnd(insideString, strBound)) {
                    insideString = false;
                    strBound = mark();
                    memory.add(code.substring(start, end) + "\"");
                }
            } else if (insideNumber) {
                if (!Character.isDigit(mark()) && (mark() != '.' || code.substring(start, end).contains("."))) {
                    insideNumber = false;
                    memory.add(code.substring(start, end));
                    end--; // This is so that the next loop doesn't skip the character right after the final digit of the number.
                }
            } else if (!Character.isWhitespace(mark())) {
                if (isAtVarNameStart()) { // A variable or instruction or function name, or true or false.
                    String varName = getVarName();
                    if (varName.equals("if") && memory.size >= 2 && memory.get(memory.size - 2).equals("else")) { // For making else if statements.
                        memory.set(memory.size - 2, "else if");
                    }
                    else {
                        memory.add(varName);
                        if (varName.equals("if") || varName.equals("else") || varName.equals("while") || varName.equals("repeat") || varName.equals("switch") || varName.equals("case") || varName.equals("for") || varName.equals("foreach") || varName.equals("when") || varName.equals("whenever") || varName.equals("thread") || varName.equals("runLater")) // For instructions with { } blocks.
                            memory.add("(");
                    }
                    end--; // This is so that the next loop doesn't skip the character right after the word.
                } else if (isStringStartOrEnd(insideString, strBound)) { // The beginning of a string.
                    insideString = true;
                    strBound = mark();
                    start = end;
                } else if (end + 1 < code.length() && ((mark() == '<' && mark(1) == '=') || (mark() == '>' && mark(1) == '=') || (mark() == '=' && mark(1) == '=') || (mark() == '!' && mark(1) == '=') || (mark() == '&' && mark(1) == '&') || (mark() == '|' && mark(1) == '|') || (mark() == '/' && mark(1) == '=') || (mark() == '*' && mark(1) == '=') || (mark() == '-' && mark(1) == '=') || (mark() == '+' && mark(1) == '=') || (mark() == '+' && mark(1) == '+') || (mark() == '-' && mark(1) == '-') || (mark() == '^' && mark(1) == '^') || (mark() == '=' && mark(1) == '>') || (mark() == '-' && mark(1) == '>'))) { // Symbol combinations.
                    if(mark() == '+' && mark(1) == '+'){
                        memory.add("+=");
                        memory.addAll("1");
                    }else if(mark() == '-' && mark(1) == '-'){
                        memory.add("-=");
                        memory.addAll("1");
                    }else {
                        memory.add(Character.toString(mark()) + Character.toString(mark(1)));
                    }
                    end++;
                } else if (Character.isDigit(mark()) || (mark() == '-' && end + 1 < code.length() && Character.isDigit(mark(1)))) { // The beginning of a number.
                    insideNumber = true;
                    start = end;
                } else {
                    switch (mark()) {
                        case '(':
                            memory.add("(");
                            memory.add("(");
                            break;
                        case ')':
                            memory.add(")");
                            memory.add(")");
                            break;
                        case '{':
                            memory.add("{");
                            if(code.substring(end + 1).length() > 1 && code.substring(end + 1).trim().charAt(0) != '}')
                                memory.add("(");
                            break;
                        case '}':
                            memory.add("}"); // Close the { }
                            memory.add(")"); // Close the if()/while()/...
                            replaceSemicolon();
                            break;
                        case ';':
                            replaceSemicolon();
                            break;
                        case ',':
                            memory.add(")");
                            memory.add("(");
                            break;
                        default:
                            memory.add(mark() + "");
                            break;
                    }

                }
            }
            end++;
        }
        if(insideNumber)
            memory.add(code.substring(start, end));
    }

    private void replaceSemicolon(){
        if (end + 1 < code.length() && code.substring(end + 1).trim().length() > 0 && code.substring(end + 1).trim().charAt(0) != '}') { // To separate the statements.
            memory.add(")");
            memory.add("(");
        } else { // The last statement.
            memory.add(")");
        }
    }

    public void removeSelection(){
        StringBuilder stringBuilder = new StringBuilder(code);
        stringBuilder.delete(start, end);
        code = stringBuilder.toString();
        reset();
    }

    private boolean isAtVarNameStart(){
        return code.length() > 0 && (Character.isLetter(mark()) || mark() == '_');
    }

    private boolean isStringStartOrEnd(boolean insideString, char strChar){ // A start or end of a string.
        if(!insideString)
            return (mark() == '\'' || mark() == '"') && !isCharEscaped(end);
        else
            return mark() == strChar && (mark() == '\'' || mark() == '"') && !isCharEscaped(end);
    }

    private boolean isCharEscaped(int index){
        if(index == 0 || code.charAt(index - 1) != '\\') // There isn't a backslash so it is escaped.
            return false;
        return !isCharEscaped(index - 1); // It is only escaped if the backslash before it is NOT escaped.
    }

    public void setup(){ // Skips any whitespaces as they are only for ease of reading/writing code. Also resets the selection start back to the end.
        while(!isAtCodeEnd() && Character.isWhitespace(mark()))
            end++;
        start = end;
    }

    public char mark(){
        return code.charAt(end);
    }

    private boolean isMarked(String str){
        return str.length() + end <= code.length() && code.substring(end, str.length() + end).equals(str);
    }

    private char mark(int offset){
        return code.charAt(end + offset);
    }

    public void printMemory(){
        System.out.println("START");
        for(int i = 0; i < memory.size; i++)
            System.out.print(memory.get(i) + "*");
        System.out.println("END");
    }
}
