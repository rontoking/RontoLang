package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontolang.interpreter.members.*;
import com.rontoking.rontolang.interpreter.members.networking.*;
import com.rontoking.rontolang.interpreter.objects.*;
import com.rontoking.rontolang.interpreter.objects.networking.RontoClient;
import com.rontoking.rontolang.interpreter.objects.networking.RontoPacket;
import com.rontoking.rontolang.interpreter.objects.networking.RontoServer;
import com.rontoking.rontolang.interpreter.objects.networking.RontoSocket;
import com.rontoking.rontolang.parser.Expression;
import com.rontoking.rontolang.parser.Parser;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Function;
import com.rontoking.rontolang.program.Instruction;
import com.rontoking.rontolang.program.Parameter;
import com.rontoking.rontolang.rontoui.RontoUI;
import javafx.application.Platform;

import java.util.ArrayList;

public class Executor {
    public static Reference executeBlock(Array<Instruction> instructions, Interpreter interpreter, Class ownerClass, Function function, Reference[] params, Block instanceBlock){
        interpreter.addBlock(function);
        Reference ret = null;
        if(function != null) {
            for (int i = 0; i < function.parameters.size; i++) // Adding the parameters to the block.
                interpreter.stackTop().set(function.parameters.get(i).name, new Variable(function.parameters.get(i).type, params[i], function.parameters.get(i).isReference));
        }
        for(int i = 0; i < instructions.size; i++){
            switch (instructions.get(i).type){
                case If:
                    if ((Boolean) execute(instructions.get(i).arguments.get(0), interpreter, ownerClass, instanceBlock).value) {
                        ret = executeBlock(instructions.get(i).arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                        while (i + 1 < instructions.size && (instructions.get(i + 1).type == Instruction.Type.Else_If || instructions.get(i + 1).type == Instruction.Type.Else)) // Skip all else ifs and elses.
                            i++;
                    }
                    interpreter.updateGlobals(false);
                    break;
                case Else_If:
                    if ((Boolean) execute(instructions.get(i).arguments.get(0), interpreter, ownerClass, instanceBlock).value) {
                        ret = executeBlock(instructions.get(i).arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                        while (i + 1 < instructions.size && (instructions.get(i + 1).type == Instruction.Type.Else_If || instructions.get(i + 1).type == Instruction.Type.Else)) // Skip all else ifs and elses.
                            i++;
                    }
                    interpreter.updateGlobals(false);
                    break;
                default:
                    if(instructions.get(i).type == Instruction.Type.Return) {
                        if(function == null) {
                            Message msg = new Message(Message.Type.Return, execute(instructions.get(i), interpreter, ownerClass, instanceBlock));
                            interpreter.updateGlobals(false);
                            interpreter.removeBlock();
                            return new Reference(msg);
                        }else{
                            ret = execute(instructions.get(i), interpreter, ownerClass, instanceBlock);
                            interpreter.updateGlobals(false);
                            interpreter.removeBlock();
                            if(ret == null){
                                if(function.type.equals("void"))
                                    return null;
                                else
                                    ErrorHandler.throwTypeValueError(function.name + "()", function.type, "NULL");
                            }
                            Variable.checkIfTypeAndValueMatch(function.name + "()", function.type, ret.value, interpreter);
                            return ret;
                        }
                    }else if(instructions.get(i).type == Instruction.Type.GetVariable && instructions.get(i).data.equals("break")){
                        if(function != null) {
                            ErrorHandler.throwBreakError();
                        }else{ // Breaks return a null reference.
                            interpreter.removeBlock();
                            return null;
                        }
                    }
                    ret = execute(instructions.get(i), interpreter, ownerClass, instanceBlock);
                    interpreter.updateGlobals(false);
                    break;
            }
            if(ret != null && ret.value instanceof Message){
                if(((Message)ret.value).type == Message.Type.Return) {
                    interpreter.removeBlock();
                    if(function != null) { // Ends the return chain.
                        Reference ref = ((Message) ret.value).ref;
                        if(ref == null){
                            if(function.type.equals("void"))
                                return null;
                            else
                                ErrorHandler.throwTypeValueError(function.name + "()", function.type, "NULL");
                        }
                        Variable.checkIfTypeAndValueMatch(function.name + "()", function.type, ref.value, interpreter);
                        return ref;
                    }
                    return ret; // Continues return chain until the first function is found.
                }
            }
        }
        interpreter.removeBlock();
        return null;
    }

    public static Reference execute(Instruction instruction, Interpreter interpreter, Class ownerClass, Block instanceBlock, boolean isMemberInstruction, boolean mustBePublic){
        return execute(instruction, interpreter, ownerClass, false, instanceBlock, isMemberInstruction, mustBePublic);
    }

    public static Reference execute(Instruction instruction, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        return execute(instruction, interpreter, ownerClass, instanceBlock, false, false);
    }

    public static Reference execute(final Instruction instruction, final Interpreter interpreter, final Class ownerClass, boolean isProperty, final Block instanceBlock, boolean isMemberInstruction, boolean mustBePublic) {
        interpreter.updateGlobals(true);
        switch (instruction.type) {
            case Raw_Value:
                return new Reference(instruction.data);
            case GetVariable:
                String name = instruction.data.toString();
                if (name.equals("construct")) {
                    for (int i = 0; i < interpreter.stackTop().function.parameters.size; i++) {
                        if (instanceBlock.get(interpreter.stackTop().function.parameters.get(i).name) != null) {
                            instanceBlock.set(interpreter.stackTop().function.parameters.get(i).name, interpreter.var(interpreter.stackTop().function.parameters.get(i).name, instanceBlock, isMemberInstruction, mustBePublic));
                        }
                    }
                    return null;
                } else if (name.equals("pressedKeys")) {
                    return new Reference(RontoUI.PRESSED_KEYS);
                } else if (name.equals("mouseX")) {
                    return new Reference(Gdx.input.getX());
                } else if (name.equals("mouseY")) {
                    return new Reference(Gdx.graphics.getHeight() - Gdx.input.getY());
                } else if (name.equals("mousePos")) {
                    return new Reference(new RontoPoint(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), interpreter));
                } else if (name.equals("mouseDeltaX")) {
                    return new Reference(Gdx.input.getDeltaX());
                } else if (name.equals("mouseDeltaY")) {
                    return new Reference(Gdx.input.getDeltaY());
                } else if (name.equals("mouseDeltaPos")) {
                    return new Reference(new RontoPoint(Gdx.input.getDeltaX(), -Gdx.input.getDeltaY(), interpreter));
                } else if (name.equals("mouseScroll")) {
                    return new Reference(RontoUI.SCROLL_AMOUNT);
                } else if (name.equals("defaultFont")) {
                    return new Reference(interpreter.defaultFont);
                } else if (name.equals("server")) {
                    return new Reference(interpreter.server);
                } else if (name.equals("client")) {
                    return new Reference(interpreter.client);
                } else if (name.equals("socket")) {
                    if (interpreter.socketState == Interpreter.SocketState.Client)
                        return new Reference(interpreter.client);
                    else if (interpreter.socketState == Interpreter.SocketState.Server)
                        return new Reference(interpreter.server);
                    else
                        return new Reference(interpreter.socket);
                } else if (name.equals("packet")) {
                    return new Reference(interpreter.packet);
                } else if (name.equals("deltaTime")) {
                    return new Reference(Gdx.graphics.getDeltaTime());
                } else if (name.equals("filledShapes") || name.equals("filledshapes")) {
                    interpreter.setToShapeState(ShapeRenderer.ShapeType.Filled);
                    return null;
                } else if (name.equals("lineShapes") || name.equals("lineshapes")) {
                    interpreter.setToShapeState(ShapeRenderer.ShapeType.Line);
                    return null;
                } else if (name.equals("pointShapes") || name.equals("pointshapes")) {
                    interpreter.setToShapeState(ShapeRenderer.ShapeType.Point);
                    return null;
                }
                Variable var = interpreter.var(name, instanceBlock, isMemberInstruction, mustBePublic);
                if (var == null)
                    var = interpreter.getClassProp(ownerClass.name, name, mustBePublic);
                if (var == null) {
                    if (isProperty)
                        return new Reference(ErrorHandler.Errors.MISSING_PROPERTY_DEPENDENCY);
                    else
                        ErrorHandler.throwVarError(name);
                }
                return var.getRef();
            case SetVariable:
                if (instruction.arguments.get(0).type == Instruction.Type.Raw_Value || instruction.arguments.get(0).type == Instruction.Type.GetVariable || instruction.arguments.get(0).type == Instruction.Type.Element || instruction.arguments.get(0).type == Instruction.Type.Function) { // The type of the variable being set.
                    if (instruction.arguments.get(1).type == Instruction.Type.GetVariable) {
                        name = instruction.arguments.get(1).data.toString();
                        Reference ref = execute(instruction.arguments.get(2), interpreter, ownerClass, instanceBlock);

                        if (instruction.arguments.get(0).type == Instruction.Type.Element) { // Strict list or map type.
                            Variable.checkIfTypeAndValueMatch(name, instruction.arguments.get(0), ref.value, interpreter, ownerClass, instanceBlock);
                            interpreter.stackTop().set(name, new Variable(Variable.typeToStr(instruction.arguments.get(0)), ref, false));
                        } else {
                            String type = instruction.arguments.get(0).data.toString();
                            if (type.equals("")) { // No type specified.
                                Variable prop = null;
                                if (!isMemberInstruction) { // If it's a member instruction, only look at the instance members.
                                    prop = interpreter.getClassProp(ownerClass.name, name, mustBePublic);
                                }
                                if (prop != null) { // Is this class's property.
                                    prop.setRefValue(ref.value);
                                } else { // Is either on the stack or should be added to the stack
                                    var = interpreter.var(name, instanceBlock, isMemberInstruction, mustBePublic);
                                    if (var == null) { // Variable doesn't exist. Create new variable.
                                        if (isMemberInstruction) // Do not create a new one if it's a member instruction.
                                            ErrorHandler.throwVarError(name);
                                        interpreter.stackTop().set(name, new Variable("", ref, false));
                                    } else {
                                        var.setRefValue(ref.value);
                                    }
                                }
                            } else { // Type specified.
                                Variable.checkIfTypeAndValueMatch(name, instruction.arguments.get(0), ref.value, interpreter, ownerClass, instanceBlock);
                                interpreter.stackTop().set(name, new Variable(type, ref, false));
                            }
                        }
                        return ref;
                    } else if (instruction.arguments.get(1).type == Instruction.Type.Member || instruction.arguments.get(1).type == Instruction.Type.Element) {
                        Reference ref = execute(instruction.arguments.get(2), interpreter, ownerClass, instanceBlock);
                        execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value = ref.value;
                        return ref;
                    } else {
                        ErrorHandler.throwSetError(instruction.arguments.get(1).type.name());
                    }
                } else {
                    ErrorHandler.throwVarTypeError(instruction.arguments.get(0).type.name());
                }
            case Print:
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < instruction.arguments.size; i++)
                    sb.append(execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock).value.toString());
                System.out.print(sb.toString());
                interpreter.console.print(sb.toString(), interpreter);
                break;
            case Println:
                sb = new StringBuilder();
                for (int i = 0; i < instruction.arguments.size; i++)
                    sb.append(execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock).value.toString());
                System.out.println(sb.toString());
                interpreter.console.println(sb.toString(), interpreter);
                break;
            case Return:
                if (instruction.arguments.get(0).type == Instruction.Type.Empty) { // For functions with type void.
                    return null;
                }
                return execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock);
            case List:
                if (instruction.arguments.size == 2 && instruction.arguments.get(0).data != null && instruction.arguments.get(0).data.equals("new")) { // It's an instance.
                    Class baseClass = interpreter.getClass(instruction.arguments.get(1).arguments.get(0).data.toString());
                    if (baseClass == null)
                        ErrorHandler.throwInstanceError(instruction.arguments.get(1).arguments.get(0).data.toString());
                    else {
                        Instance instance = new Instance(baseClass, interpreter, ownerClass);
                        Reference ref = new Reference(instance);
                        execute(instruction.arguments.get(1), interpreter, baseClass, instance.properties); // Calling the appropriate instance constructor.
                        return ref;
                    }
                } else { // It's a list.
                    Array<Reference> array = new Array<Reference>();
                    for (int i = 0; i < instruction.arguments.size; i++)
                        array.add(execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock));
                    return new Reference(array);
                }
            case Map:
                ObjectMap<Object, Reference> map = new ObjectMap<Object, Reference>();
                for (int i = 0; i < instruction.arguments.size; i++) {
                    Pair pair = (Pair) execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock).value;
                    map.put(pair.a.value, pair.b);
                }
                return new Reference(map);
            case Pair:
                return new Reference(new Pair(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock), execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case Element:
                Object[] args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                Object par = execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value;
                if (par instanceof Array) {
                    return getElement((Array<Reference>) par, args, 1);
                } else if (par instanceof ObjectMap) {
                    return ((ObjectMap<Object, Reference>) par).get((int) Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
                } else if (par instanceof String) {
                    return new Reference(par.toString().charAt((int) Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock))));
                } else {
                    ErrorHandler.throwElementError();
                }
            case Super:
                int paramNum = instruction.arguments.size;
                if (paramNum == 1 && instruction.arguments.get(0).type == Instruction.Type.Empty) // For function calls with 0 arguments.
                    paramNum = 0;
                Reference[] params = new Reference[paramNum];
                String[] paramTypes = new String[paramNum];
                for (int i = 0; i < paramNum; i++) { // Setting the argument values and determining their types.
                    Reference ref = execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock);
                    params[i] = ref;
                    if (ref == null)
                        ErrorHandler.throwVoidError(instruction.arguments.get(i).type.name(), "super", i);
                    else
                        paramTypes[i] = Variable.typeOf(ref.value);
                }
                Function func = ownerClass.getFunc(ownerClass.parentClass, false, paramTypes, true, interpreter);
                executeBlock(func.code, interpreter, ownerClass, func, params, instanceBlock);
                return null;
            case Function:
                paramNum = instruction.arguments.size - 1;
                if (paramNum == 1 && instruction.arguments.get(1).type == Instruction.Type.Empty) // For function calls with 0 arguments.
                    paramNum = 0;
                params = new Reference[paramNum];
                paramTypes = new String[paramNum];
                for (int i = 0; i < paramNum; i++) { // Setting the argument values and determining their types.
                    Reference ref = execute(instruction.arguments.get(i + 1), interpreter, ownerClass, instanceBlock);
                    params[i] = ref;
                    if (ref == null)
                        ErrorHandler.throwVoidError(instruction.arguments.get(i + 1).type.name(), instruction.arguments.get(0).data.toString(), i);
                    else
                        paramTypes[i] = Variable.typeOf(ref.value);
                }
                func = ownerClass.getFunc(instruction.arguments.get(0).data.toString(), true, paramTypes, instanceBlock == null, interpreter); // Only throws an error if it would have to be static.
                if (func == null) // It must be a non-static function.
                    func = ownerClass.getFunc(instruction.arguments.get(0).data.toString(), false, paramTypes, true, interpreter);
                return executeBlock(func.code, interpreter, ownerClass, func, params, instanceBlock);
            case Member:
                Instruction parent = instruction.arguments.get(0); // Parent can be: GetVariable(Variable or class) or Function(which is a variable). There are some special keywords like 'this' which gives the object.
                Instruction child = instruction.arguments.get(1); // Child can be: GetVariable(variable) or Function(also variable))
                if (parent.type == Instruction.Type.GetVariable) {
                    if (parent.data.equals("super")) {
                        paramNum = child.arguments.size - 1;
                        if (paramNum == 1 && child.arguments.get(1).type == Instruction.Type.Empty) // For function calls with 0 arguments.
                            paramNum = 0;
                        params = new Reference[paramNum];
                        paramTypes = new String[paramNum];
                        for (int i = 0; i < paramNum; i++) { // Setting the argument values and determining their types.
                            Reference ref = execute(child.arguments.get(i + 1), interpreter, ownerClass, instanceBlock);
                            params[i] = ref;
                            if (ref == null)
                                ErrorHandler.throwVoidError(child.arguments.get(i + 1).type.name(), "super", i);
                            else
                                paramTypes[i] = Variable.typeOf(ref.value);
                        }
                        func = ownerClass.getFunc(child.arguments.get(0).data.toString(), false, paramTypes, true, interpreter);
                        return executeBlock(func.code, interpreter, interpreter.getClass(ownerClass.parentClass), func, params, instanceBlock);
                    } else if (parent.data.equals("console")) {
                        return interpreter.console.properties.get(child.data.toString()).getRef();
                    } else if (parent.data.equals("window")) {
                        if(child.data.equals("centerX") || child.data.equals("centerx")){
                            return new Reference(Gdx.graphics.getWidth() / 2);
                        }else if(child.data.equals("centerY") || child.data.equals("centery")){
                            return new Reference(Gdx.graphics.getHeight() / 2);
                        }else if(child.data.equals("center") || child.data.equals("centerPos")){
                            return new Reference(new RontoPoint(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, interpreter));
                        }
                        return interpreter.window.properties.get(child.data.toString()).getRef();
                    } else if (parent.data.equals("prefs")) {
                        if(child.type == Instruction.Type.Function){
                            if(child.arguments.get(0).data.equals("open")){
                                interpreter.preferences = Gdx.app.getPreferences(execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString());
                                return null;
                            }else if(child.arguments.get(0).data.equals("set") || child.arguments.get(0).data.equals("put")){
                                if(interpreter.preferences == null)
                                    ErrorHandler.throwPreferencesError();
                                interpreter.preferences.putString(execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value.toString());
                                interpreter.preferences.flush();
                                return null;
                            }else if(child.arguments.get(0).data.equals("get") || child.arguments.get(0).data.equals("find")){
                                if(interpreter.preferences == null)
                                    ErrorHandler.throwPreferencesError();
                                return new Reference(interpreter.preferences.getString(execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
                            }
                        }
                    } else if (parent.data.equals("this")) {
                        return execute(child, interpreter, ownerClass, instanceBlock, true, false);
                    } else { // The parent is either a class or one of this object's members.
                        Class c = interpreter.getClass(parent.data.toString());
                        if (c == null) { // It's a member.
                            Reference ref = execute(parent, interpreter, ownerClass, instanceBlock);
                            return memberOfNewValue(ref, child, interpreter, ownerClass, instanceBlock);
                        } else { // It's a class.
                            return execute(child, interpreter, c, instanceBlock, false, c != ownerClass);
                        }
                    }
                } else { // It's a raw value or function.
                    return memberOfNewValue(execute(parent, interpreter, ownerClass, instanceBlock), child, interpreter, ownerClass, instanceBlock);
                }
            case Else:
                return executeBlock(instruction.arguments.get(0).arguments, interpreter, ownerClass, null, null, instanceBlock);
            case While:
                while ((Boolean) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value) {
                    Reference ref = executeBlock(instruction.arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                    if (ref != null) {
                        return ref;
                    }
                }
                break;
            case Repeat:
                for (int i = 0; i < Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)); i++) {
                    Reference ref = executeBlock(instruction.arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                    if (ref != null) {
                        return ref;
                    }
                }
                break;
            case Func:
                Function function = new Function(null);
                function.parameters = Parser.parseParams(ownerClass.name, function.name, instruction.arguments.get(0).data.toString());
                function.code = instruction.arguments.get(1).arguments;
                return new Reference(function);
            case Switch:
                Reference switchArg = execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock);
                Array<Instruction> cases = instruction.arguments.get(1).arguments;
                for (int i = 0; i < cases.size; i++) // Checks all cases. If a case has no arguments, it is considered the default case and executed.
                    if (cases.get(i).arguments.get(0).type == Instruction.Type.Empty || Variable.areEqual(switchArg, execute(cases.get(i).arguments.get(0), interpreter, ownerClass, instanceBlock)))
                        return executeBlock(cases.get(i).arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                break;
            case Case:
                ErrorHandler.throwCaseError();
                break;
            case For:
                Instruction block = new Instruction(Instruction.Type.List); // The for(x, y, z){c} is converted into a {x, while(y){c, z}}
                Array<Instruction> forHead = instruction.arguments.get(0).arguments;
                block.arguments.add(forHead.get(0)); // The for initial statement.
                Instruction whileInstruction = new Instruction(Instruction.Type.While);
                whileInstruction.arguments.add(forHead.get(1)); // Sets the while condition to the for condition.
                whileInstruction.arguments.add(new Instruction(Instruction.Type.List));
                for (int i = 0; i < instruction.arguments.get(1).arguments.size; i++) // Adds the for block to the while block.
                    whileInstruction.arguments.get(1).arguments.add(instruction.arguments.get(1).arguments.get(i));
                whileInstruction.arguments.get(1).arguments.add(forHead.get(2)); // Adds the for's repeated statement to the end of the while's block.
                block.arguments.add(whileInstruction);
                return executeBlock(block.arguments, interpreter, ownerClass, null, null, instanceBlock);
            case Foreach:
                Array<Instruction> pair = instruction.arguments.get(0).arguments; // x : y
                block = new Instruction(Instruction.Type.List); // The foreach(x : y){c} is converted into a {int i = 0, while(i < y.size){x = y[i], c, i++}}

                block.arguments.add(new Instruction(Instruction.Type.SetVariable)); // int i = 0;
                block.arguments.get(0).arguments.add(new Instruction(Instruction.Type.Raw_Value, "int")); // The var type.
                block.arguments.get(0).arguments.add(new Instruction(Instruction.Type.GetVariable, "*FOREACH*")); // The var name.
                block.arguments.get(0).arguments.add(new Instruction(Instruction.Type.Raw_Value, 0)); // The var value.

                whileInstruction = new Instruction(Instruction.Type.While); // The while.

                whileInstruction.arguments.add(new Instruction(Instruction.Type.Lesser)); // i < y.size
                whileInstruction.arguments.get(0).arguments.add(new Instruction(Instruction.Type.GetVariable, "*FOREACH*")); // i
                whileInstruction.arguments.get(0).arguments.add(new Instruction(Instruction.Type.Raw_Value, ((Array<Reference>) execute(pair.get(1), interpreter, ownerClass, instanceBlock).value).size)); // y.size

                whileInstruction.arguments.add(new Instruction(Instruction.Type.List)); // The while block.
                Array<Instruction> whileBlock = whileInstruction.arguments.get(1).arguments;

                whileBlock.add(new Instruction(Instruction.Type.SetVariable)); // x = y[i]
                whileBlock.get(0).arguments.add(new Instruction(Instruction.Type.Raw_Value, "")); // No type.
                whileBlock.get(0).arguments.add(new Instruction(Instruction.Type.GetVariable, pair.get(0).data)); // x

                Instruction element = new Instruction(Instruction.Type.Element); // y[i]
                element.arguments.add(pair.get(1)); // y
                element.arguments.add(new Instruction(Instruction.Type.GetVariable, "*FOREACH*")); // [i]

                whileBlock.get(0).arguments.add(element); // y[i]
                Array<Instruction> forEachBlock = instruction.arguments.get(1).arguments;
                for (int i = 0; i < forEachBlock.size; i++) // Adding the foreach block to the while block.
                    whileBlock.add(forEachBlock.get(i));

                Instruction repeated = new Instruction(Instruction.Type.SetVariable); // i = i + 1
                repeated.arguments.add(new Instruction(Instruction.Type.Raw_Value, "")); // No type.
                repeated.arguments.add(new Instruction(Instruction.Type.GetVariable, "*FOREACH*")); // i

                Instruction sum = new Instruction(Instruction.Type.Sum); // i + 1
                sum.arguments.add(new Instruction(Instruction.Type.GetVariable, "*FOREACH*")); // i
                sum.arguments.add(new Instruction(Instruction.Type.Raw_Value, 1)); // 1

                repeated.arguments.add(sum); // i + 1
                whileBlock.add(repeated);
                block.arguments.add(whileInstruction);
                return executeBlock(block.arguments, interpreter, ownerClass, null, null, instanceBlock);
            case When:
                interpreter.addEvent(new Event(instruction.arguments.get(0), false, instruction.arguments.get(1).arguments, ownerClass, instanceBlock));
                break;
            case Whenever:
                interpreter.addEvent(new Event(instruction.arguments.get(0), true, instruction.arguments.get(1).arguments, ownerClass, instanceBlock));
                break;
            case Thread:
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        executeBlock(instruction.arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                    }
                });
                thread.setDaemon((Boolean) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value);
                thread.start();
                break;
            case RunLater:
                if ((Boolean) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            executeBlock(instruction.arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                        }
                    });
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            executeBlock(instruction.arguments.get(1).arguments, interpreter, ownerClass, null, null, instanceBlock);
                        }
                    });
                }
                break;
            case Lesser:
                return new Reference(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)) < Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case Equal:
                return new Reference(Variable.areEqual(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock), execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case Not_Equal:
                return new Reference(!Variable.areEqual(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock), execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case Greater_Or_Equal:
                return new Reference(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)) >= Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case Lesser_Or_Equal:
                return new Reference(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)) <= Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case And:
                return new Reference((Boolean) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value && (Boolean) execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
            case Implies:
                return new Reference(!(Boolean) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value || (Boolean) execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
            case Or:
                return new Reference((Boolean) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value || (Boolean) execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
            case Xor:
                int numOFTrue = 0;
                for (int i = 0; i < instruction.arguments.size; i++)
                    if ((Boolean) execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock).value)
                        numOFTrue++;
                return new Reference(numOFTrue == 1);
            case Not:
                return new Reference(!(Boolean) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value);
            case Greater:
                return new Reference(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)) > Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case Sum:
                return new Reference(Variable.getOperationValue(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value, execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value, Instruction.Type.Sum, interpreter));
            case Concat:
                sb = new StringBuilder();
                for (int i = 0; i < instruction.arguments.size; i++)
                    sb.append(execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock).value.toString());
                return new Reference(sb.toString());
            case Difference:
                return new Reference(Variable.getOperationValue(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value, execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value, Instruction.Type.Difference, interpreter));
            case Product:
                return new Reference(Variable.getOperationValue(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value, execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value, Instruction.Type.Product, interpreter));
            case Quotient:
                return new Reference(Variable.getOperationValue(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value, execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value, Instruction.Type.Quotient, interpreter));
            case Remainder:
                return new Reference(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)) % Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)));
            case Power:
                return new Reference(Math.pow(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)), Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock))));
            case Pointer:
                return new Reference(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock));
            case Copy:
                return Variable.copyOf(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value);
            case Wait:
                try {
                    Thread.sleep((long) Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case TypeOf:
                return new Reference(Variable.typeOf(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value));
            case Exec:
                Array<Instruction> instructions = Expression.parseExpr(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString(), true).getInstruction().arguments;
                Reference ref;
                for (int i = 1; i < instructions.size; i++) {
                    ref = execute(Expression.parseExpr(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString(), true).getInstruction().arguments.get(i), interpreter, ownerClass, instanceBlock);
                    if (ref != null)
                        return ref;
                }
                break;
            case Random:
                if (instruction.arguments.size == 1) {
                    if (instruction.arguments.get(0).type == Instruction.Type.Empty)
                        return new Reference(MathUtils.random());
                    else
                        return new Reference(MathUtils.random((int) Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
                } else {
                    return new Reference(MathUtils.random((int) Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)), (int) Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock))));
                }
            case Abs:
                return new Reference(Math.abs(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Atan:
                return new Reference(Math.atan(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Atan2:
                return new Reference(Math.atan2(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)), Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock))));
            case Sin:
                return new Reference(Math.sin(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Cos:
                return new Reference(Math.cos(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Tan:
                return new Reference(Math.tan(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Sinh:
                return new Reference(Math.sinh(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Cosh:
                return new Reference(Math.cosh(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Tanh:
                return new Reference(Math.tanh(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Round:
                return new Reference((int) Math.round(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Ceil:
                return new Reference((int) Math.ceil(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Floor:
                return new Reference((int) Math.floor(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Asin:
                return new Reference(Math.asin(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Acos:
                return new Reference(Math.acos(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Max:
                return new Reference(Math.max(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)), Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock))));
            case Min:
                return new Reference(Math.min(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)), Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock))));
            case Sqrt:
                return new Reference(Math.sqrt(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case ToDeg:
                return new Reference(Math.toDegrees(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case ToRad:
                return new Reference(Math.toRadians(Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock))));
            case Empty:
                return new Reference(new Array<Reference>());
            case Img:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args[0] instanceof FileHandle)
                    return new Reference(interpreter.getTexture((FileHandle) args[0]));
                return new Reference(interpreter.getTexture(Gdx.files.internal(Interpreter.IMAGE_PATH + args[0].toString())));
            case Sound:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args[0] instanceof FileHandle)
                    return new Reference(interpreter.getSound((FileHandle) args[0]));
                return new Reference(interpreter.getSound(Gdx.files.internal(Interpreter.SOUND_PATH + args[0].toString())));
            case Music:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args[0] instanceof FileHandle)
                    return new Reference(interpreter.getMusic((FileHandle) args[0]));
                return new Reference(interpreter.getMusic(Gdx.files.internal(Interpreter.MUSIC_PATH + args[0].toString())));
            case Font:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args.length == 1)
                    return new Reference(interpreter.getFont(Gdx.files.internal(Interpreter.FONT_PATH + "cavestory.ttf"), (int) Variable.getNum(args[0])));
                else if (args[0] instanceof FileHandle)
                    return new Reference(interpreter.getFont((FileHandle) args[0], (int) Variable.getNum(args[1])));
                return new Reference(interpreter.getFont(Gdx.files.internal(Interpreter.FONT_PATH + args[0].toString()), (int) Variable.getNum(args[1])));
            case Str:
                return new Reference(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString());
            case String:
                return new Reference(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString());
            case Int:
                return new Reference(Integer.parseInt(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Float:
                return new Reference(Float.parseFloat(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Double:
                return new Reference(Double.parseDouble(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case File:
                return new Reference(Gdx.files.internal(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Internal:
                return new Reference(Gdx.files.internal(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Local:
                return new Reference(Gdx.files.local(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case External:
                return new Reference(Gdx.files.external(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Absolute:
                return new Reference(Gdx.files.absolute(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Classpath:
                return new Reference(Gdx.files.classpath(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Rect:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                return new Reference(new RontoRect(args[0], args[1], args[2], args[3], interpreter));
            case Sprite:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args[0] instanceof Texture)
                    return new Reference(new RontoSprite((Texture) args[0], interpreter));
                else if (args[0] instanceof FileHandle)
                    return new Reference(new RontoSprite(interpreter.getTexture((FileHandle) args[0]), interpreter));
                return new Reference(new RontoSprite(interpreter.getTexture(Gdx.files.internal(Interpreter.IMAGE_PATH + execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString())), interpreter));
            case Camera2d:
                return new Reference(new RontoCamera2D(interpreter));
            case Point:
                return new Reference(new RontoPoint(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value, execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value, interpreter));
            case Color:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args.length == 1)
                    return new Reference(new RontoColor(Colors.get(args[0].toString().toUpperCase()), interpreter));
                if (args.length == 2) {
                    Color color = Colors.get(args[0].toString());
                    color.a = (float) Variable.getNum(args[1]);
                    return new Reference(new RontoColor(color, interpreter));
                } else if (args.length == 3)
                    return new Reference(new RontoColor(args[0], args[1], args[2], 1, interpreter));
                else
                    return new Reference(new RontoColor(args[0], args[1], args[2], args[3], interpreter));
            case Draw:
                interpreter.setToSpriteState();
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args[0] instanceof BitmapFont || args[0] instanceof String) { // Drawing text.
                    if (args.length == 1)// text
                        interpreter.defaultFont.draw(interpreter.spriteBatch, args[0].toString(), 0, 0 + RontoUI.getTextHeight(args[0].toString(), interpreter.defaultFont));
                    else if (args.length == 3)// text, x, y
                        interpreter.defaultFont.draw(interpreter.spriteBatch, args[0].toString(), (int) Variable.getNum(args[1]), (int) Variable.getNum(args[2]) + RontoUI.getTextHeight(args[0].toString(), interpreter.defaultFont));
                    else if (args.length == 4) { // font, text, x, y        or           text, x, y, color
                        if (args[0] instanceof BitmapFont)
                            ((BitmapFont) args[0]).draw(interpreter.spriteBatch, args[1].toString(), (int) Variable.getNum(args[2]), (int) Variable.getNum(args[3]) + RontoUI.getTextHeight(args[1].toString(), ((BitmapFont) args[0])));
                        else {
                            interpreter.defaultFont.setColor(((RontoColor) args[3]).getColor());
                            interpreter.defaultFont.draw(interpreter.spriteBatch, args[0].toString(), (int) Variable.getNum(args[1]), (int) Variable.getNum(args[2]) + RontoUI.getTextHeight(args[0].toString(), interpreter.defaultFont));
                        }
                    } else { // font, text, x, y, color
                        ((BitmapFont) args[0]).setColor(((RontoColor) args[4]).getColor());
                        ((BitmapFont) args[0]).draw(interpreter.spriteBatch, args[1].toString(), (int) Variable.getNum(args[2]), (int) Variable.getNum(args[3]) + RontoUI.getTextHeight(args[1].toString(), ((BitmapFont) args[0])));
                    }
                } else { // Drawing sprites.
                    if(args[0] instanceof Array){
                        Array<Reference> arr = (Array<Reference>)args[0];
                        for(int i = 0; i < arr.size; i++){
                            ((RontoSprite) arr.get(i).value).draw(interpreter);
                        }
                    }
                    for (int i = 0; i < args.length; i++) {
                        ((RontoSprite) args[i]).draw(interpreter);
                    }
                }
                break;
            case Fill:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if(args.length == 1) {
                    Color fillColor = ((RontoColor) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value).getColor();
                    Gdx.gl.glClearColor(fillColor.r, fillColor.g, fillColor.b, fillColor.a);
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                }else if(args.length == 3){
                    Gdx.gl.glClearColor((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), 1);
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                }else{
                    Gdx.gl.glClearColor((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), (float)Variable.getNum(args[3]));
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                }
                break;
            case Dst:
                if (instruction.arguments.size == 2) {
                    RontoPoint point1 = (RontoPoint) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value;
                    RontoPoint point2 = (RontoPoint) execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock).value;
                    return new Reference(point1.getVec().dst(point2.getVec()));
                } else {
                    return new Reference(Vector2.dst((float) Variable.getNum(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock)), (float) Variable.getNum(execute(instruction.arguments.get(1), interpreter, ownerClass, instanceBlock)), (float) Variable.getNum(execute(instruction.arguments.get(2), interpreter, ownerClass, instanceBlock)), (float) Variable.getNum(execute(instruction.arguments.get(3), interpreter, ownerClass, instanceBlock))));
                }
            case Serialize:
                return Variable.serialize(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value);
            case Deserialize:
                return Variable.deserialize(Variable.getByteArr((Array<Reference>) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value));
            case Parse:
                return Variable.deserialize(Variable.getByteArr((Array<Reference>) execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value));
            case KeyDown:
                return new Reference(Gdx.input.isKeyPressed(Input.Keys.valueOf(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString())));
            case KeyPressed:
                return new Reference(Gdx.input.isKeyJustPressed(Input.Keys.valueOf(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString())));
            case MouseDown:
                String mouseBtn = execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString();
                if (mouseBtn.toLowerCase().equals("left"))
                    return new Reference(Gdx.input.isButtonPressed(Input.Buttons.LEFT));
                else if (mouseBtn.toLowerCase().equals("right"))
                    return new Reference(Gdx.input.isButtonPressed(Input.Buttons.RIGHT));
                else if (mouseBtn.toLowerCase().equals("mid") || mouseBtn.toLowerCase().equals("middle"))
                    return new Reference(Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
                ErrorHandler.throwMouseError(mouseBtn);
                return null;
            case MousePressed:
                mouseBtn = execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString();
                if (mouseBtn.toLowerCase().equals("left"))
                    return new Reference(RontoUI.JUST_LEFT_CLICKED);
                else if (mouseBtn.toLowerCase().equals("right"))
                    return new Reference(RontoUI.JUST_RIGHT_CLICKED);
                ErrorHandler.throwMouseError(mouseBtn);
                return null;
            case MouseClicked:
                mouseBtn = execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString();
                if (mouseBtn.toLowerCase().equals("left"))
                    return new Reference(RontoUI.JUST_LEFT_CLICKED);
                else if (mouseBtn.toLowerCase().equals("right"))
                    return new Reference(RontoUI.JUST_RIGHT_CLICKED);
                ErrorHandler.throwMouseError(mouseBtn);
                return null;
            case MouseDouble:
                mouseBtn = execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString();
                if (mouseBtn.toLowerCase().equals("left"))
                    return new Reference(RontoUI.JUST_DOUBLE_CLICKED_LEFT);
                else if (mouseBtn.toLowerCase().equals("right"))
                    return new Reference(RontoUI.JUST_DOUBLE_CLICKED_RIGHT);
                ErrorHandler.throwMouseError(mouseBtn);
                return null;
            case MouseReleased:
                mouseBtn = execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString();
                if (mouseBtn.toLowerCase().equals("left"))
                    return new Reference(RontoUI.JUST_LEFT_RELEASED);
                else if (mouseBtn.toLowerCase().equals("right"))
                    return new Reference(RontoUI.JUST_RIGHT_RELEASED);
                ErrorHandler.throwMouseError(mouseBtn);
                return null;
            case MoveMouse:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args.length == 1) {
                    RontoPoint point = (RontoPoint) args[0];
                    Gdx.input.setCursorPosition((int) point.floatX(), (int) point.floatY());
                } else
                    Gdx.input.setCursorPosition((int) Variable.getNum(args[0]), (int) Variable.getNum(args[1]));
                return null;
            case Browse:
                return new Reference(Gdx.net.openURI(execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Google:
                return new Reference(Gdx.net.openURI("https://www.google.com/search?q=" + execute(instruction.arguments.get(0), interpreter, ownerClass, instanceBlock).value.toString()));
            case Noise:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if (args.length == 2)
                    return new Reference(SimplexNoise.sample(Variable.getNum(args[0]), Variable.getNum(args[1])));
                else if (args.length == 3)
                    return new Reference(SimplexNoise.sample(Variable.getNum(args[0]), Variable.getNum(args[1]), Variable.getNum(args[2])));
                else
                    return new Reference(SimplexNoise.sample(Variable.getNum(args[0]), Variable.getNum(args[1]), Variable.getNum(args[2]), Variable.getNum(args[3])));
            case Path:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                return Variable.getList(Pathfinding.path((RontoPoint) args[0], (RontoPoint) args[1], (int) Variable.getNum(args[2]), (int) Variable.getNum(args[3]), (Function)args[4], interpreter, ownerClass, instanceBlock));
            case Circle:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                interpreter.setToShapeState();
                if(args.length == 3)
                    interpreter.shapeRenderer.circle((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]));
                else
                    interpreter.shapeRenderer.circle((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), (int)Variable.getNum(args[3]));
                break;
            case Ellipse:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                interpreter.setToShapeState();
                if(args.length == 4)
                    interpreter.shapeRenderer.ellipse((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), (float)Variable.getNum(args[3]));
                else
                    interpreter.shapeRenderer.ellipse((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), (float)Variable.getNum(args[3]), (int) Variable.getNum(args[3]));
                break;
            case Line:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                interpreter.setToShapeState();
                if(args.length == 4)
                    interpreter.shapeRenderer.line((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), (float)Variable.getNum(args[3]));
                else
                    interpreter.shapeRenderer.line(((RontoPoint)args[0]).getVec(), ((RontoPoint)args[1]).getVec());
                break;
            case Polygon:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                interpreter.setToShapeState();
                if(args.length == 1)
                    interpreter.shapeRenderer.polygon(Variable.getFloatArr((Array<Reference>)args[0]));
                else{
                    float[] vertices = new float[args.length];
                    for(int i = 0; i < args.length; i++)
                        vertices[i] = (float)Variable.getNum(args[i]);
                    interpreter.shapeRenderer.polygon(vertices);
                }
                break;
            case ShapeColor:
                args = getArgs(instruction, interpreter, ownerClass, instanceBlock);
                if(args.length == 1) {
                    if(args[0] instanceof RontoColor)
                        interpreter.shapeRenderer.setColor(((RontoColor) args[0]).getColor());
                    else
                        interpreter.shapeRenderer.setColor(Colors.get(args[0].toString().toUpperCase()));
                }
                else if(args.length == 3)
                    interpreter.shapeRenderer.setColor((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), 1);
                else
                    interpreter.shapeRenderer.setColor((float)Variable.getNum(args[0]), (float)Variable.getNum(args[1]), (float)Variable.getNum(args[2]), (float)Variable.getNum(args[3]));
                break;
            default:
                ErrorHandler.throwInstructionError(instruction.type);
        }
        return null;
    }

    private static Object[] getArgs(Instruction instruction, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        Object[] args = new Object[instruction.arguments.size];
        for(int i = 0; i < args.length; i++)
            args[i] = execute(instruction.arguments.get(i), interpreter, ownerClass, instanceBlock).value;
        return args;
    }

    private static Reference memberOfNewValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock) {
        if (parent.value instanceof Instance) { // TODO: If the parent class is a RontoObject, check if that RontoObject can execute the member.
            return execute(child, interpreter, ((Instance) parent.value).baseClass, ((Instance) parent.value).properties, true, true);
        }
        if (child.type == Instruction.Type.Function && child.arguments.size == 2 && child.arguments.get(1).type == Instruction.Type.Empty) { // Change functions with no arguments into simple gets, ex: dog.bark() -> dog.bark
            child.type = Instruction.Type.GetVariable;
            child.data = child.arguments.get(0).data;
            child.arguments.clear();
        }
        if (child.type == Instruction.Type.GetVariable && (child.data.equals("cpy") || child.data.equals("copy"))) {
            return Variable.copyOf(parent.value);
        } else if (child.type == Instruction.Type.GetVariable && (child.data.equals("type"))) {
            return new Reference(Variable.typeOf(parent.value));
        } else if (child.type == Instruction.Type.GetVariable && (child.data.equals("print"))) {
            System.out.print(parent.value.toString());
            interpreter.console.print(parent.value.toString(), interpreter);
            return null;
        } else if (child.type == Instruction.Type.GetVariable && (child.data.equals("println"))) {
            System.out.println(parent.value.toString());
            interpreter.console.println(parent.value.toString(), interpreter);
            return null;
        } else if (child.type == Instruction.Type.GetVariable && (child.data.equals("serialize"))) {
            return Variable.serialize(parent.value);
        } else if (parent.value instanceof Double || parent.value instanceof Float || parent.value instanceof Integer) {
            return NumberMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        }  else if (parent.value instanceof Array) {
            return ArrayMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof ObjectMap) {
            return MapMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof String) {
            return StringMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof Character) {
            return CharMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof Texture) {
            return TextureMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof Sound) {
            return SoundMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof Music) {
            return MusicMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof BitmapFont) {
            return FontMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof Function) {
            return FunctionMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof FileHandle) {
            return FileMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof RontoServer) {
            return ServerMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof RontoClient) {
            return ClientMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof RontoSocket) {
            return SocketMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof RontoPacket) {
            return PacketMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof Connection) {
            return ConnectionMember.getMemberValue(parent, child, interpreter, ownerClass, instanceBlock);
        } else if (parent.value instanceof RontoObject) {
            return ((RontoObject) parent.value).member(child, interpreter, ownerClass, instanceBlock);
        }
        ErrorHandler.throwMemberError(true);
        return null;
    }

    private static Reference getElement(Array<Reference> array, Object[] args, int index){
        if(index == args.length - 1){
            return array.get((int)Variable.getNum(args[index]));
        }
        return getElement((Array<Reference>)array.get((int)Variable.getNum(args[index])).value, args, index + 1);
    }
}
