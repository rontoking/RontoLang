package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

import java.util.Comparator;

public class ArrayMember {
    public static Reference getMemberValue(Reference parent, final Instruction child, final Interpreter interpreter, final Class ownerClass, final Block instanceBlock){
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("size") || child.data.equals("count") || child.data.equals("num") || child.data.equals("length") || child.data.equals("len") || child.data.equals("n"))
                return new Reference(((Array) parent.value).size);
            else if (child.data.equals("shuffle")) {
                ((Array) parent.value).shuffle();
                return null;
            } else if (child.data.equals("reverse")) {
                ((Array) parent.value).reverse();
                return null;
            } else if (child.data.equals("random") || child.data.equals("rand")) {
                return ((Array<Reference>) parent.value).random();
            } else if (child.data.equals("sort")) {
                Array<Reference> arr = (Array<Reference>)parent.value;
                if(arr.get(0).value instanceof String){
                    arr.sort(new Comparator<Reference>() {
                        @Override
                        public int compare(Reference o1, Reference o2) {
                            return o1.value.toString().compareTo(o2.value.toString());
                        }
                    });
                }else{ // It must be a number TODO: Maybe before this check if object is an instance and if so use its compare(instance) member IF it has one.
                    arr.sort(new Comparator<Reference>() {
                        @Override
                        public int compare(Reference o1, Reference o2) {
                            if(Variable.getNum(o1) < Variable.getNum(o2))
                                return -1;
                            return 1;
                        }
                    });
                }
                return new Reference(null);
            }
        } else if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("add")) {
                ((Array) parent.value).add(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock));
                return null;
            } else if (funcName.equals("remove")) {
                ((Array) parent.value).removeIndex((int) Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock)));
                return null;
            } else if (funcName.equals("join")) {
                return new Reference(Variable.join(((Array) parent.value), Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
            } else if (funcName.equals("insert")) {
                ((Array) parent.value).insert((int) Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock)), Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock));
                return null;
            }else if (funcName.equals("startsWith")) {
                Array<Reference> start = (Array<Reference>)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value;
                if(((Array) parent.value).size >= start.size){
                    Array<Reference> p = (Array<Reference>)parent.value;
                    for(int i = 0; i < start.size; i++){
                        if(!Variable.areEqual(p.get(i), start.get(i)))
                            return new Reference(false);
                    }
                    return new Reference(true);
                }
                return new Reference(false);
            }else if (funcName.equals("endsWith")) {
                Array<Reference> end = (Array<Reference>)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value;
                if(((Array) parent.value).size >= end.size){
                    Array<Reference> p = (Array<Reference>)parent.value;
                    for(int i = 0; i < end.size; i++){
                        if(!Variable.areEqual(p.get(p.size - end.size + i), end.get(i)))
                            return new Reference(false);
                    }
                    return new Reference(true);
                }
                return new Reference(false);
            }else if(funcName.equals("any")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                for(int i = 0; i < arr.size; i++){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        interpreter.removeBlock();
                        return new Reference(true);
                    }
                }
                interpreter.removeBlock();
                return new Reference(false);
            }else if(funcName.equals("sort")){
                final Array<Reference> arr = (Array) parent.value;
                if(arr.size > 1) {
                    interpreter.addBlock(null);
                    if (child.arguments.size == 2) { // sort(bool)
                        if(arr.get(0).value instanceof String){
                            if((Boolean)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value) {
                                arr.sort(new Comparator<Reference>() {
                                    @Override
                                    public int compare(Reference o1, Reference o2) {
                                        return o1.value.toString().compareTo(o2.value.toString());
                                    }
                                });
                            }else{
                                arr.sort(new Comparator<Reference>() {
                                    @Override
                                    public int compare(Reference o1, Reference o2) {
                                        return -o1.value.toString().compareTo(o2.value.toString());
                                    }
                                });
                            }
                        }else{ // It must be a number TODO: Maybe before this check if object is an instance and if so use its compare(instance) member IF it has one.
                            if((Boolean)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value) {
                                arr.sort(new Comparator<Reference>() {
                                    @Override
                                    public int compare(Reference o1, Reference o2) {
                                        if(Variable.getNum(o1) < Variable.getNum(o2))
                                            return -1;
                                        return 1;
                                    }
                                });
                            }else{
                                arr.sort(new Comparator<Reference>() {
                                    @Override
                                    public int compare(Reference o1, Reference o2) {
                                        if(Variable.getNum(o1) > Variable.getNum(o2))
                                            return -1;
                                        return 1;
                                    }
                                });
                            }
                        }
                    }else{ // sort(x, condition)
                        final String var1Name = child.arguments.get(1).data.toString();
                        final String var2Name = child.arguments.get(2).data.toString();
                        interpreter.addBlock(null);
                        arr.sort(new Comparator<Reference>() {
                            @Override
                            public int compare(Reference o1, Reference o2) {
                                interpreter.stackTop().set(var1Name, new Variable(Variable.typeOf(o1.value), o1, false));
                                interpreter.stackTop().set(var2Name, new Variable(Variable.typeOf(o2.value), o2, false));

                                if(((Boolean)Executor.execute(child.arguments.get(3), interpreter, ownerClass, instanceBlock).value)){
                                    return 1;
                                }
                                return -1;
                            }
                        });
                        interpreter.removeBlock();
                        return new Reference(null);
                    }
                }
            }else if(funcName.equals("first")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                for(int i = 0; i < arr.size; i++){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        interpreter.removeBlock();
                        return arr.get(i);
                    }
                }
                interpreter.removeBlock();
                return new Reference(null);
            }else if(funcName.equals("last")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                for(int i = arr.size - 1; i >= 0; i--){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        interpreter.removeBlock();
                        return arr.get(i);
                    }
                }
                interpreter.removeBlock();
                return new Reference(null);
            }else if(funcName.equals("index") || funcName.equals("firstIndex") || funcName.equals("indexOf") || funcName.equals("firstIndexOf")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                for(int i = 0; i < arr.size; i++){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        interpreter.removeBlock();
                        return new Reference(i);
                    }
                }
                interpreter.removeBlock();
                return new Reference(null);
            }else if(funcName.equals("lastIndex") || funcName.equals("lastIndexOf")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                for(int i = arr.size - 1; i >= 0; i--){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        interpreter.removeBlock();
                        return new Reference(i);
                    }
                }
                interpreter.removeBlock();
                return new Reference(null);
            }else if(funcName.equals("removeFirst")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                for(int i = 0; i < arr.size; i++){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        interpreter.removeBlock();
                        arr.removeIndex(i);
                        return new Reference(true);
                    }
                }
                interpreter.removeBlock();
                return new Reference(false);
            }else if(funcName.equals("removeLast")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                for(int i = arr.size - 1; i >= 0; i--){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        interpreter.removeBlock();
                        arr.removeIndex(i);
                        return new Reference(true);
                    }
                }
                interpreter.removeBlock();
                return new Reference(false);
            }else if(funcName.equals("all")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                Array<Reference> result = new Array<Reference>();
                interpreter.addBlock(null);
                for(int i = 0; i < arr.size; i++){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        result.add(arr.get(i));
                    }
                }
                interpreter.removeBlock();
                return new Reference(result);
            }else if(funcName.equals("size") || funcName.equals("count") || funcName.equals("num") || funcName.equals("length") || funcName.equals("len") || funcName.equals("n")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                int result = 0;
                interpreter.addBlock(null);
                for(int i = 0; i < arr.size; i++){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        result++;
                    }
                }
                interpreter.removeBlock();
                return new Reference(result);
            }else if(funcName.equals("removeAll")){
                String varName = child.arguments.get(1).data.toString();
                Array<Reference> arr = (Array) parent.value;
                interpreter.addBlock(null);
                int num = 0;
                for(int i = 0; i < arr.size; i++){
                    interpreter.stackTop().set(varName, new Variable(Variable.typeOf(arr.get(i).value), arr.get(i), false));
                    if((Boolean)Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock).value){
                        arr.removeIndex(i);
                        num++;
                    }
                }
                interpreter.removeBlock();
                return new Reference(num);
            }else if(funcName.equals("sub")){
                Array<Reference> arr = (Array) parent.value;
                Array<Reference> result = new Array<Reference>();
                if(child.arguments.size == 2){
                    for(int i = (int)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock)); i < arr.size; i++){
                        result.add(arr.get(i));
                    }
                    return new Reference(result);
                }else{
                    for(int i = (int)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock)); i < (int)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock)); i++){
                        result.add(arr.get(i));
                    }
                    return new Reference(result);
                }
            }else if(funcName.equals("swap")){
                ((Array) parent.value).swap((int)Variable.getNum(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock)), (int)Variable.getNum(Executor.execute(child.arguments.get(2), interpreter, ownerClass, instanceBlock)));
            }
        }
        return null;
    }
}
