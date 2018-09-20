package com.rontoking.rontolang.interpreter.serializable;

import com.badlogic.gdx.files.FileHandle;

import java.io.Serializable;

public class SerializableFile extends FileHandle implements Serializable{
    public SerializableFile(FileHandle fileHandle){
        super(fileHandle.path(), fileHandle.type());
    }
}
