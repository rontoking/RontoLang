package com.rontoking.rontolang.interpreter.members;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.interpreter.*;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Instruction;

public class FileMember {
    public static Reference getMemberValue(Reference parent, Instruction child, Interpreter interpreter, Class ownerClass, Block instanceBlock) {
        if (child.type == Instruction.Type.GetVariable) {
            if (child.data.equals("exists"))
                return new Reference(((FileHandle) parent.value).exists());
            else if (child.data.equals("parent"))
                return new Reference(((FileHandle) parent.value).parent());
            else if (child.data.equals("path"))
                return new Reference(((FileHandle) parent.value).path());
            else if (child.data.equals("name"))
                return new Reference(((FileHandle) parent.value).name());
            else if (child.data.equals("ext") || child.data.equals("extension"))
                return new Reference(((FileHandle) parent.value).extension());
            else if (child.data.equals("bytes") || child.data.equals("data"))
                return Variable.getList(((FileHandle) parent.value).readBytes());
            else if (child.data.equals("read") || child.data.equals("text") || child.data.equals("string") || child.data.equals("str"))
                return new Reference(((FileHandle) parent.value).readString());
            else if (child.data.equals("isFolder") || child.data.equals("isDir") || child.data.equals("isDirectory"))
                return new Reference(((FileHandle) parent.value).isDirectory());
            else if (child.data.equals("len") || child.data.equals("length") || child.data.equals("size"))
                return new Reference(((FileHandle) parent.value).length());
            else if (child.data.equals("lastMod") || child.data.equals("lastModified"))
                return new Reference((int) ((FileHandle) parent.value).lastModified());
            else if (child.data.equals("type"))
                return new Reference(((FileHandle) parent.value).type().name());
            else if (child.data.equals("list") || child.data.equals("children"))
                return Variable.getList(((FileHandle) parent.value).list());
            else if (child.data.equals("mkdirs") || child.data.equals("makeDirs") || child.data.equals("makeDirectories") || child.data.equals("makeFolders")) {
                ((FileHandle) parent.value).mkdirs();
                return null;
            }
            else if (child.data.equals("del") || child.data.equals("delete")) {
                ((FileHandle) parent.value).deleteDirectory();
                return null;
            }
            else if (child.data.equals("empty")) {
                ((FileHandle) parent.value).emptyDirectory(false);
                return null;
            }
            else if (child.data.equals("emptyFiles")) {
                ((FileHandle) parent.value).emptyDirectory(true);
                return null;
            }
        } else if (child.type == Instruction.Type.Function) {
            String funcName = child.arguments.get(0).data.toString();
            if (funcName.equals("write")) {
                ((FileHandle) parent.value).writeString(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), false);
                return null;
            }
            if (funcName.equals("add") || funcName.equals("append")) {
                ((FileHandle) parent.value).writeString(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString(), true);
                return null;
            }
            if (funcName.equals("writeBytes")) {
                ((FileHandle) parent.value).writeBytes(Variable.getByteArr((Array<Reference>)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), false);
                return null;
            }
            if (funcName.equals("addBytes") || funcName.equals("appendBytes")) {
                ((FileHandle) parent.value).writeBytes(Variable.getByteArr((Array<Reference>)Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value), true);
                return null;
            }
            if (funcName.equals("rename")) {
                ((FileHandle) parent.value).moveTo(((FileHandle) parent.value).parent().child(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
                return null;
            }
            if (funcName.equals("move")) {
                ((FileHandle) parent.value).moveTo((FileHandle) Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value);
                return null;
            }
            if (funcName.equals("child")) {
                return new Reference(((FileHandle) parent.value).child(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
            }
            if (funcName.equals("sibling")) {
                return new Reference(((FileHandle) parent.value).sibling(Executor.execute(child.arguments.get(1), interpreter, ownerClass, instanceBlock).value.toString()));
            }
        }
        return null;
    }
}
