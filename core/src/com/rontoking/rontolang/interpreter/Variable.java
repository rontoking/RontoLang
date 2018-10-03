package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontolang.interpreter.objects.*;
import com.rontoking.rontolang.interpreter.objects.networking.RontoClient;
import com.rontoking.rontolang.interpreter.objects.networking.RontoPacket;
import com.rontoking.rontolang.interpreter.objects.networking.RontoServer;
import com.rontoking.rontolang.interpreter.serializable.SerializableArray;
import com.rontoking.rontolang.interpreter.serializable.SerializableFile;
import com.rontoking.rontolang.interpreter.serializable.SerializableMap;
import com.rontoking.rontolang.interpreter.serializable.SerializableTexture;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Function;
import com.rontoking.rontolang.program.Instruction;

import java.io.*;
import java.util.ArrayList;

public class Variable {
    public String type;
    public Function.Access access;
    private Reference reference;

    // Edit typeOf() and checkIfSimpleTypeAndValueMatch() for adding a new var!

    public Variable(){
        // For KryoNet.
    }

    public Variable(String type, Reference ref, boolean isReference){
        this.type = type;
        this.access = Function.Access.Public;
        if(isReference)
            reference = ref;
        else {
            if(ref.value instanceof Reference)
                reference = (Reference)ref.value;
            else {
                reference = new Reference(ref.value);
                round();
            }
        }
    }

    public void setRefValue(Object value){
        if(value instanceof Reference)
            reference = (Reference)value;
        else {
            reference.value = value;
            round();
        }
    }

    public Reference getRef(){
        return reference;
    }

    public Variable(String type, Function.Access access, Object value){ // For properties.
        this.type = type;
        this.access = access;
        if(value instanceof Reference)
            this.reference = (Reference)value;
        else {
            this.reference = new Reference(value);
            round();
        }
    }

    public static Object getDefaultValue(String type){
        if(type.equals("byte"))
            return (byte)0;
        if(type.equals("bool"))
            return false;
        if(type.equals("char"))
            return 'a';
        if(type.equals("int"))
            return 0;
        if(type.equals("float"))
            return 0f;
        if(type.equals("double"))
            return 0d;
        if(type.equals("str"))
            return "";
        return null;
    }

    public static String typeOf(Object value){
        if(value instanceof Byte)
            return "byte";
        if(value instanceof Boolean)
            return "bool";
        if(value instanceof Character)
            return "char";
        if(value instanceof Integer)
            return "int";
        if(value instanceof Float)
            return "float";
        if(value instanceof Double)
            return "double";
        if(value instanceof String)
            return "str";
        if(value instanceof Texture)
            return "img";
        if(value instanceof Sound)
            return "sound";
        if(value instanceof Music)
            return "music";
        if(value instanceof BitmapFont)
            return "font";
        if(value instanceof FileHandle)
            return "file";
        if(value instanceof RontoSprite)
            return "sprite";
        if(value instanceof RontoCamera2D)
            return "camera2d";
        if(value instanceof RontoServer)
            return "server";
        if(value instanceof RontoClient)
            return "client";
        if(value instanceof RontoPacket)
            return "packet";
        if(value instanceof Connection)
            return "conn";
        if(value instanceof RontoRect)
            return "rect";
        if(value instanceof RontoColor)
            return "color";
        if(value instanceof RontoPoint)
            return "point";
        if(value instanceof RontoEnum)
            return "enum";
        if(value instanceof Array){
            Array<Reference> array = (Array<Reference>)value;
            int dimensions = 1;
            while (array.size > 0 && array.get(0).value instanceof Array){
                array = (Array<Reference>)array.get(0).value;
                dimensions++;
            }
            if(array.size > 0)
                return typeOf(array.get(0).value) + "[" + dimensions + "]";
            return "list";
        }
        if(value instanceof ObjectMap) {
            ObjectMap<Object, Reference> map = (ObjectMap<Object, Reference>)value;
            Array<Object> keys = map.keys().toArray();
            if(keys.size > 0)
                return typeOf(keys.get(0)) + "[" + typeOf(map.get(keys.get(0)).value) + "]";
            return "map";
        }
        if(value instanceof Instance)
            return ((Instance) value).baseClass.name;
        if(value == null)
            return "null";
        return "void";
    }

    public static String typeToStr(Instruction type){
        if(type.type == Instruction.Type.GetVariable){
            return type.data.toString();
        }
        if(type.type == Instruction.Type.Raw_Value){
            return "";
        }
        if(type.type == Instruction.Type.Element){
            String baseType = typeToStr(type.arguments.get(0)); // The base list or map type, ex: baseType of int[2] is int.
            if(type.arguments.size == 2) { // If no number is given, that means the type is like ex: int[] which is equivalent to int[1].
                if(type.arguments.get(1).type == Instruction.Type.Raw_Value) // It's a list.
                    return baseType + "[" + type.arguments.get(1).data + "]";
                else if(type.arguments.get(1).type == Instruction.Type.GetVariable || type.arguments.get(1).type == Instruction.Type.Element) // It's a map.
                    return baseType + "[" + typeToStr(type.arguments.get(1)) + "]";
                else
                    ErrorHandler.throwVarTypeError(type.type.name());
            }
            return baseType + "[1]";
        }else{
            ErrorHandler.throwVarTypeError(type.type.name());
            return null;
        }
    }

    public static double getNum(Reference ref){
        return getNum(ref.value);
    }

    public static double getNum(Object value){
        if(value instanceof Byte)
            return (Byte)value;
        if(value instanceof Boolean) {
            if ((Boolean) value)
                return 1;
            return 0;
        }
        if(value instanceof Character)
            return (Character)value;
        if(value instanceof Integer)
            return (Integer)value;
        if(value instanceof Float)
            return (Float)value;
        if(value instanceof Double)
            return (Double)value;
        if(value instanceof String)
            return value.toString().length();
        if(value instanceof  Instance)
            return ((Instance) value).baseClass.objectProperties.size;
        if(value instanceof Array)
            return ((Array) value).size;
        if(value instanceof ObjectMap)
            return ((ObjectMap) value).size;
        return 0;
    }

    public static boolean areEqual(Reference ref1, Reference ref2){
        return areObjectsEqual(ref1.value, ref2.value);
    }

    private static boolean areObjectsEqual(Object val1, Object val2){
        if(val1 instanceof Array && val2 instanceof Array){
            Array<Reference> arr1 = (Array<Reference>)val1;
            Array<Reference> arr2 = (Array<Reference>)val2;
            if(arr1.size != arr2.size)
                return false;
            for(int i = 0; i < arr1.size; i++){
                if(!areEqual(arr1.get(i), arr2.get(i)))
                    return false;
            }
            return true;
        }
        if(val1 instanceof ObjectMap && val2 instanceof ObjectMap){
            ObjectMap<Object, Reference> map1 = (ObjectMap<Object, Reference>)val1;
            ObjectMap<Object, Reference> map2 = (ObjectMap<Object, Reference>)val2;

            Array<Object> keys1 = map1.keys().toArray();
            Array<Object> keys2 = map2.keys().toArray();

            if(keys1.size != keys2.size)
                return false;
            for(int i = 0; i < keys1.size; i++){
                if(!areObjectsEqual(keys1.get(i), keys2.get(i)))
                    return false;
                if(!areEqual(map1.get(keys1.get(i)), map2.get(keys2.get(i))))
                    return false;
            }
            return true;
        }
        return val1.equals(val2);
    }

    private void round(){
        if(type.equals("int")) {
            if(reference.value instanceof Double)
                reference.value = (int) Math.round((Double) reference.value);
            else if(reference.value instanceof Float)
                reference.value = (int) Math.round((Float) reference.value);
        }
        else if(type.equals("float") && reference.value instanceof Double)
            reference.value = (int)Math.round((Double) reference.value);
    }

    public static Array<Character> toCharArray(String str){
        Array<Character> array = new Array<Character>(str.length());
        for(int i = 0; i < str.length(); i++)
            array.add(str.charAt(i));
        return array;
    }

    public static Object getOperationValue(Object object1, Object object2, Instruction.Type operation, Interpreter interpreter){
        switch (operation){
            case Sum:
                if(object1 instanceof Array && object2 instanceof Array) {
                    Array<Reference> result = new Array<Reference>();
                    result.addAll((Array<Reference>) object1);
                    result.addAll((Array<Reference>) object2);
                    return result;
                }
                if(object1 instanceof RontoPoint && object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().add(((RontoPoint) object2).getVec()), interpreter);
                }
                if(object1 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().add((float)getNum(object2), (float)getNum(object2)), interpreter);
                }
                if(object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object2).getVec().add((float)getNum(object1), (float)getNum(object1)), interpreter);
                }
                if(object1 instanceof String || object2 instanceof String)
                    return object1.toString() + object2.toString();
                if(object1 instanceof Double || object2 instanceof Double)
                    return Double.valueOf(object1.toString()) + Double.valueOf(object2.toString());
                if(object1 instanceof Float || object2 instanceof Float)
                    return Float.valueOf(object1.toString()) + Float.valueOf(object2.toString());
                if(object1 instanceof Integer || object2 instanceof Integer)
                    return Integer.valueOf(object1.toString()) + Integer.valueOf(object2.toString());
                if(object1 instanceof Byte && object2 instanceof Byte)
                    return Byte.valueOf(object1.toString()) + Byte.valueOf(object2.toString());
                System.out.println("WTF: " + object1.toString());
                ErrorHandler.throwOperationError(operation, typeOf(object1), typeOf(object2));
                return null;
            case Difference:
                if(object1 instanceof Array) // For things like -x
                    object1 = 0;
                if(object1 instanceof RontoPoint && object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().sub(((RontoPoint) object2).getVec()), interpreter);
                }
                if(object1 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().sub((float)getNum(object2), (float)getNum(object2)), interpreter);
                }
                if(object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object2).getVec().sub((float)getNum(object1), (float)getNum(object1)), interpreter);
                }
                if(object1 instanceof Double || object2 instanceof Double)
                    return Double.valueOf(object1.toString()) - Double.valueOf(object2.toString());
                if(object1 instanceof Float || object2 instanceof Float)
                    return Float.valueOf(object1.toString()) - Float.valueOf(object2.toString());
                if(object1 instanceof Integer || object2 instanceof Integer)
                    return Integer.valueOf(object1.toString()) - Integer.valueOf(object2.toString());
                if(object1 instanceof Byte && object2 instanceof Byte)
                    return Byte.valueOf(object1.toString()) - Byte.valueOf(object2.toString());
                ErrorHandler.throwOperationError(operation, typeOf(object1), typeOf(object2));
                return null;
            case Product:
                if(object1 instanceof String && object2 instanceof Integer){
                    return repeat(object1.toString(), (Integer)object2);
                }
                if(object1 instanceof Integer && object2 instanceof String){
                    return repeat(object2.toString(), (Integer)object1);
                }
                if(object1 instanceof RontoPoint && object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().scl(((RontoPoint) object2).getVec()), interpreter);
                }
                if(object1 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().scl((float)getNum(object2), (float)getNum(object2)), interpreter);
                }
                if(object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object2).getVec().scl((float)getNum(object1), (float)getNum(object1)), interpreter);
                }
                if(object1 instanceof Double || object2 instanceof Double)
                    return Double.valueOf(object1.toString()) * Double.valueOf(object2.toString());
                if(object1 instanceof Float || object2 instanceof Float)
                    return Float.valueOf(object1.toString()) * Float.valueOf(object2.toString());
                if(object1 instanceof Integer || object2 instanceof Integer)
                    return Integer.valueOf(object1.toString()) * Integer.valueOf(object2.toString());
                if(object1 instanceof Byte && object2 instanceof Byte)
                    return Byte.valueOf(object1.toString()) * Byte.valueOf(object2.toString());
                ErrorHandler.throwOperationError(operation, typeOf(object1), typeOf(object2));
                return null;
            case Quotient:
                if(object1 instanceof RontoPoint && object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().scl(1f / ((RontoPoint) object2).getVec().x, 1f / ((RontoPoint) object2).getVec().y), interpreter);
                }
                if(object1 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object1).getVec().scl(1f / (float)getNum(object2), (float)getNum(object2)), interpreter);
                }
                if(object2 instanceof RontoPoint){
                    return new RontoPoint(((RontoPoint) object2).getVec().scl(1f / (float)getNum(object1), (float)getNum(object1)), interpreter);
                }
                if(object1 instanceof Double || object2 instanceof Double)
                    return Double.valueOf(object1.toString()) / Double.valueOf(object2.toString());
                if(object1 instanceof Float || object2 instanceof Float)
                    return Float.valueOf(object1.toString()) / Float.valueOf(object2.toString());
                if(object1 instanceof Integer || object2 instanceof Integer)
                    return Integer.valueOf(object1.toString()) / Integer.valueOf(object2.toString());
                if(object1 instanceof Byte && object2 instanceof Byte)
                    return Byte.valueOf(object1.toString()) / Byte.valueOf(object2.toString());
                ErrorHandler.throwOperationError(operation, typeOf(object1), typeOf(object2));
                return null;
        }
        return 0;
    }

    public static String join(Array<Reference> array, String str){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < array.size; i++){
            if(i > 0)
                sb.append(str);
            sb.append(array.get(i).toString());
        }
        return sb.toString();
    }

    public static void checkIfTypeAndValueMatch(String name, String type, Object value, Interpreter interpreter){
        if(value instanceof Reference){
            checkIfTypeAndValueMatch(name, type, ((Reference) value).value, interpreter);
            return;
        }
        if(type.equals("void"))
            ErrorHandler.throwTypeValueError(name, type, value);
        if(type.equals(""))
            return;
        if(type.charAt(type.length() - 1) == ']'){
            int index = type.length() - 1;
            int parenLevel = 1;
            while (parenLevel > 0){
                index--;
                if(type.charAt(index) == '[')
                    parenLevel--;
                else if(type.charAt(index) == ']')
                    parenLevel++;
            }
            String baseType = type.substring(0, index);
            String argument = type.substring(index + 1);
            argument = argument.substring(0, argument.length() - 1); // Removes the last ']'.

            if(argument.equals("") || Character.isDigit(argument.charAt(0))){ // It's a list.
                int dimensions = 1;
                if(!argument.equals(""))
                    dimensions = Integer.parseInt(argument);
                try {
                    Array<Reference> array = (Array<Reference>) value;
                    dimensions--;
                    checkArrayType(array, dimensions, name, baseType, interpreter);
                } catch (Exception e) {
                    ErrorHandler.throwTypeValueError(name, type, value);
                }
            }else{ // It's a map.
                try {
                    ObjectMap<Object, Reference> map = (ObjectMap<Object, Reference>) value;
                    Array<Object> keys = map.keys().toArray();
                    for(int i = 0; i < keys.size; i++){
                        checkIfTypeAndValueMatch(name, baseType, keys.get(i), interpreter);
                        checkIfTypeAndValueMatch(name, argument, map.get(keys.get(i)).value, interpreter);
                    }
                }catch (Exception e){
                    ErrorHandler.throwTypeValueError(name, type, value);
                }
            }
        }else{
            checkIfSimpleTypeAndValueMatch(name, type, value, interpreter);
        }
    }

    public static void checkIfTypeAndValueMatch(String name, Instruction type, Object value, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if(value instanceof Reference){
            checkIfTypeAndValueMatch(name, type, ((Reference) value).value, interpreter, ownerClass, instanceBlock);
            return;
        }
        if(type.type == Instruction.Type.Element){
            if(type.arguments.size == 1 || type.arguments.get(1).type == Instruction.Type.Raw_Value) { // It's a list.
                Instruction baseType = type.arguments.get(0); // The base list type, ex: baseType of int[][] is int.
                int dimensions = 1;
                if (type.arguments.size == 2) { // If no number is given, that means the type is like ex: int[] which is equivalent to int[1].
                    dimensions = (Integer) Executor.execute(type.arguments.get(1), interpreter, ownerClass, instanceBlock).value;
                }
                try {
                    Array<Reference> array = (Array<Reference>) value;
                    dimensions--;
                    checkArrayType(array, dimensions, name, baseType, interpreter, ownerClass, instanceBlock);
                } catch (Exception e) {
                    ErrorHandler.throwTypeValueError(name, Variable.typeToStr(type), value);
                }
            }else if(type.arguments.get(1).type == Instruction.Type.GetVariable || type.arguments.get(1).type == Instruction.Type.Element){ // It's a map.
                try {
                    ObjectMap<Object, Reference> map = (ObjectMap<Object, Reference>) value;
                    Array<Object> keys = map.keys().toArray();
                    for(int i = 0; i < keys.size; i++){
                        checkIfTypeAndValueMatch(name, type.arguments.get(0), keys.get(i), interpreter, ownerClass, instanceBlock);
                        checkIfTypeAndValueMatch(name, type.arguments.get(1), map.get(keys.get(i)).value, interpreter, ownerClass, instanceBlock);
                    }
                }catch (Exception e){
                    ErrorHandler.throwTypeValueError(name, Variable.typeToStr(type), value);
                }
            }else{
                ErrorHandler.throwVarTypeError(type.type.name());
            }
        }else{
            checkIfSimpleTypeAndValueMatch(name,  type.data.toString(), value, interpreter);
        }
    }

    private static void checkIfSimpleTypeAndValueMatch(String name, String type, Object value, Interpreter interpreter){
        try {
            if (type.equals("byte")) {
                byte x = (Byte) value;
            } else if (type.equals("bool")) {
                boolean x = (Boolean) value;
            } else if (type.equals("char")) {
                if(value.toString().length() > 3)
                    ErrorHandler.throwTypeValueError(name, type, value);
            } else if (type.equals("int")) {
                if(!(value instanceof Integer) && !(value instanceof Byte))
                    ErrorHandler.throwTypeValueError(name, type, value);
            } else if (type.equals("float")) {
                if(!(value instanceof Float) && !(value instanceof Integer) && !(value instanceof Byte))
                    ErrorHandler.throwTypeValueError(name, type, value);
            } else if (type.equals("double")) {
                if(!(value instanceof Double) && !(value instanceof Float) && !(value instanceof Integer) && !(value instanceof Byte))
                    ErrorHandler.throwTypeValueError(name, type, value);
            } else if (type.equals("str")) {
                String x = value.toString();
            } else if (type.equals("list")) {
                Array<Object> x = (Array<Object>) value;
            } else if (type.equals("map")) {
                ObjectMap<Object, Reference> x = (ObjectMap<Object, Reference>) value;
            }else if (type.equals("img")) {
                Texture x = (Texture) value;
            }else if (type.equals("sound")) {
                Sound x = (Sound) value;
            }else if (type.equals("music")) {
                Music x = (Music) value;
            }else if (type.equals("camera2d")) {
                RontoCamera2D x = (RontoCamera2D) value;
            }else if (type.equals("server")) {
                RontoServer x = (RontoServer) value;
            }else if (type.equals("client")) {
                RontoClient x = (RontoClient) value;
            }else if (type.equals("packet")) {
                RontoPacket x = (RontoPacket) value;
            }else if (type.equals("conn")) {
                Connection x = (Connection) value;
            }else if (type.equals("font")) {
                BitmapFont x = (BitmapFont) value;
            }else if (type.equals("file")) {
                FileHandle x = (FileHandle) value;
            }else if (type.equals("rect")) {
                RontoRect x = (RontoRect) value;
            }else if (type.equals("enum")) {
                RontoEnum x = (RontoEnum) value;
            }else if (type.equals("sprite")) {
                RontoSprite x = (RontoSprite) value;
            }else if (type.equals("color")) {
                RontoColor x = (RontoColor) value;
            }else if (type.equals("point")) {
                RontoPoint x = (RontoPoint) value;
            }  else if(interpreter.getClass(type) != null){
                Instance x = (Instance) value;
            }else if(!type.equals("")){
                ErrorHandler.throwVarTypeError(type);
            }
        }catch (Exception e){
            ErrorHandler.throwTypeValueError(name, type, value);
        }
    }

    private static void checkArrayType(Array<Reference> array, int dimensions, String name, Instruction baseType, Interpreter interpreter, Class ownerClass, Block instanceBlock){
        if(dimensions > 0){
            for(int i = 0; i < array.size; i++){
                if(!(array.get(i).value instanceof Array)) { // For lists with one element that are being treated like non-lists.
                    Array<Reference> newValue = new Array<Reference>();
                    newValue.add(array.get(i));
                    array.set(i, new Reference(newValue));
                }
                checkArrayType((Array<Reference>)(array.get(i)).value, dimensions - 1, name, baseType, interpreter, ownerClass, instanceBlock);
            }
        }else{
            for(int i = 0; i < array.size; i++){ // Finally check the base type.
                checkIfTypeAndValueMatch(name, baseType, array.get(i).value, interpreter, ownerClass, instanceBlock);
            }
        }
    }

    private static void checkArrayType(Array<Reference> array, int dimensions, String name, String baseType, Interpreter interpreter){
        if(dimensions > 0){
            for(int i = 0; i < array.size; i++){
                checkArrayType((Array<Reference>)(array.get(i)).value, dimensions - 1, name, baseType, interpreter);
            }
        }else{
            for(int i = 0; i < array.size; i++){ // Finally check the base type.
                checkIfTypeAndValueMatch(name, baseType, array.get(i).value, interpreter);
            }
        }
    }

    public Variable getCopy(){
        return new Variable(type, access, new Reference(copyOf(reference.value)));
    }

    public static Reference copyOf(Object value){
        if(value instanceof Array){
            Array<Reference> original = (Array<Reference>)value;
            Array<Reference> copy = new Array<Reference>();
            for(int i = 0; i < original.size; i++){
                copy.add(copyOf(original.get(i).value));
            }
            return new Reference(copy);
        }else if(value instanceof ObjectMap){
            ObjectMap<Object, Reference> original = (ObjectMap<Object, Reference>)value;
            ObjectMap<Object, Reference> copy = new ObjectMap<Object, Reference>();
            Array<Object> keys = original.keys().toArray();
            Array<Reference> values = original.values().toArray();
            for(int i = 0; i < keys.size; i++){
                copy.put(copyOf(keys.get(i)), copyOf(values.get(i).value));
            }
            return new Reference(copy);
        }else if(value instanceof RontoObject){
            ObjectMap<String, Variable> original = ((RontoObject)value).getProperties();
            ObjectMap<String, Variable> copy = new ObjectMap<String, Variable>();
            Array<String> keys = original.keys().toArray();
            Array<Variable> values = original.values().toArray();
            for(int i = 0; i < keys.size; i++){
                copy.put(keys.get(i), values.get(i).getCopy());
            }
            return new Reference(copy);
        }else if(value instanceof Texture){
            return new Reference(new Texture(((Texture)value).getTextureData()));
        }else if(value instanceof Reference){
            return new Reference(copyOf(((Reference)value).value));
        }
        return new Reference(value);
    }

    public static Reference getList(Object[] arr){
        Array<Reference> list = new Array<Reference>(arr.length);
        for(int i = 0; i < arr.length; i++){
            list.add(new Reference(arr[i]));
        }
        return new Reference(list);
    }

    public static Reference getList(Array<Object> arr){
        Array<Reference> list = new Array<Reference>(arr.size);
        for(int i = 0; i < arr.size; i++){
            list.add(new Reference(arr.get(i)));
        }
        return new Reference(list);
    }

    public static Object[] getArr(Array<Reference> list){
        Object[] arr = new Object[list.size];
        for(int i = 0; i < list.size; i++){
            arr[i] = list.get(i).value;
        }
        return arr;
    }

    public static byte[] getByteArr(Array<Reference> list){
        byte[] arr = new byte[list.size];
        for(int i = 0; i < list.size; i++){
            arr[i] = (Byte) list.get(i).value;
        }
        return arr;
    }

    public static float[] getFloatArr(Array<Reference> list){
        float[] arr = new float[list.size];
        for(int i = 0; i < list.size; i++){
            arr[i] = (float)getNum(list.get(i).value);
        }
        return arr;
    }

    public static Reference getList(byte[] arr){
        Array<Reference> list = new Array<Reference>(arr.length);
        for(int i = 0; i < arr.length; i++){
            list.add(new Reference(arr[i]));
        }
        return new Reference(list);
    }

    @Override
    public String toString(){
        return reference.toString();
    }

    public static Reference deserialize(byte[] data) {
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            Object obj = objStream.readObject();
            return (new Reference(obj));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ErrorHandler.throwDeserializeError();
        return null;
    }

    private static String repeat(String s, int n){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < n; i++){
            sb.append(n);
        }
        return sb.toString();
    }

    public static Reference serialize(Object object){
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            if(object instanceof Array)
                objStream.writeObject(new SerializableArray((Array)object));
            else if(object instanceof ObjectMap)
                objStream.writeObject(new SerializableMap((ObjectMap) object));
            else if(object instanceof Texture)
                objStream.writeObject(new SerializableTexture((Texture) object));
            else if(object instanceof FileHandle)
                objStream.writeObject(new SerializableFile((FileHandle) object));
            else
                objStream.writeObject(object);
            return Variable.getList(byteStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ErrorHandler.throwSerializeError(Variable.typeOf(object));
        return null;
    }
}
