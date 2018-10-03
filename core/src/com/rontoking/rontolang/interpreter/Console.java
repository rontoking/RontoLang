package com.rontoking.rontolang.interpreter;

public class Console {
    public Block properties;

    public Console(Interpreter interpreter){
        properties = new Block(null);
        interpreter.addProperty(properties, "visible", "bool", false);
        interpreter.addProperty(properties, "input", "str", "");
        interpreter.addProperty(properties, "output", "str", "");
        interpreter.addProperty(properties, "entered", "str", ""); // True if user just pressed enter
    }

    public boolean isVisible(){
        return (Boolean) properties.get("visible").getRef().value;
    }

    public void setEntered(boolean entered){
        properties.get("entered").getRef().value = entered;
    }

    public void print(String text, Interpreter interpreter){
        interpreter.consoleOutput.text = interpreter.consoleOutput.text+ text;
    }

    public void println(String text, Interpreter interpreter){
        interpreter.consoleOutput.text = interpreter.consoleOutput.text + text + "\n";
    }

    public void update(Interpreter interpreter, boolean changeOwnProperties){
        if(changeOwnProperties) {
            properties.get("input").getRef().value = interpreter.consoleInput.getText();
            properties.get("output").getRef().value = interpreter.consoleOutput.text;
        }else{
            interpreter.consoleInput.setText(properties.get("input").getRef().value.toString());
            interpreter.consoleOutput.text = properties.get("output").getRef().value.toString();
        }
    }
}
