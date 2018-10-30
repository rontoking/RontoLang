package com.rontoking.rontolang.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Function;
import fi.iki.elonen.NanoHTTPD;
// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
//       instead of the above import use the following:
// import org.nanohttpd.NanoHTTPD;

public class Website extends NanoHTTPD{
    private FileHandle root;
    private Function func;
    private Interpreter interpreter;
    private Class ownerClass;
    private Block instanceBlock;

    public Website(int port, FileHandle root, Function func, Interpreter interpreter, Class ownerClass, Block instanceBlock) {
        super(port);
        this.root = root;
        this.func = func;
        this.interpreter = interpreter;
        this.ownerClass = ownerClass;
        this.instanceBlock = instanceBlock;
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getUri() != null) {
            if (session.getUri().equals("/")) {
                InputStream inputStream = getFile("index.html");
                if(inputStream != null)
                    return newFixedLengthResponse(Response.Status.OK, MimeType.getType("index.html"), inputStream, inputStreamLength(inputStream));
            }
            else {
                InputStream inputStream = getFile(session.getUri());
                if(inputStream != null)
                    return newFixedLengthResponse(Response.Status.OK, MimeType.getType(session.getUri()), inputStream, inputStreamLength(inputStream));
            }
        }
        Object ret = Executor.executeBlock(func.code, interpreter, ownerClass, func, new Reference[]{new Reference(session.getUri()), getMapRef(session.getParms())}, instanceBlock).value;
        if(ret instanceof FileHandle){
            InputStream inputStream = ((FileHandle) ret).read();
            if(inputStream != null)
                return newFixedLengthResponse(Response.Status.OK, MimeType.getType(((FileHandle) ret).name()), inputStream, inputStreamLength(inputStream));
        }
        return newFixedLengthResponse(ret.toString());
    }

    private InputStream getFile(String name){
        if(root.child(name).exists())
            return root.child(name).read();
        return null;
    }

    public long inputStreamLength(InputStream inputStream){
        try {
            return inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Reference getMapRef(Map<String, String> map){
        ObjectMap<String, String> ret = new ObjectMap<String, String>();
        for(String key : map.keySet())
            ret.put(key, map.get(key));
        return new Reference(ret);
    }
}
