package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rontoking.rontolang.interpreter.objects.*;
import com.rontoking.rontolang.interpreter.objects.networking.RontoClient;
import com.rontoking.rontolang.interpreter.objects.networking.RontoPacket;
import com.rontoking.rontolang.interpreter.objects.networking.RontoServer;
import com.rontoking.rontolang.interpreter.objects.networking.RontoSocket;
import com.rontoking.rontolang.program.Class;
import com.rontoking.rontolang.program.Function;
import com.rontoking.rontolang.program.Program;
import com.rontoking.rontolang.rontoui.Node;
import com.rontoking.rontolang.rontoui.RontoUI;
import com.rontoking.rontolang.rontoui.nodes.TextArea;
import com.rontoking.rontolang.rontoui.nodes.TextField;

import java.io.IOException;

public class Interpreter {
    private Program program;
    private ObjectMap<String, Block> classBlocks;
    private Array<Block> stack;

    private int propertyNum;
    private Function updateFunc, disposeFunc;
    private Array<Event> events;

    public static final String CODE_PATH = "code/";
    public static final String IMAGE_PATH = "images/";
    public static final String SOUND_PATH = "sounds/";
    public static final String MUSIC_PATH = "music/";
    public static final String FONT_PATH = "fonts/";
    public static final String ICON_PATH = "icons/";

    public SpriteBatch spriteBatch;
    public ShapeRenderer shapeRenderer;
    public TextArea consoleOutput;
    public TextField consoleInput;
    public BitmapFont defaultFont;
    public Console console;
    public Window window;
    public boolean consoleEnteredNextFrame;

    private Array<Texture> textures;
    private Array<BitmapFont> fonts;
    private Array<Sound> sounds;
    private Array<Music> music;

    public RontoServer server;
    public RontoClient client;
    public RontoSocket socket;
    public RontoPacket packet;
    public SocketState socketState;
    public BatchState batchState;
    public Preferences preferences;

    public RontoCamera2D camera2d;

    public enum SocketState{
        None, Client, Server
    }

    public enum BatchState{
        None, Shapes, Sprites
    }

    public Interpreter(Program program, SpriteBatch spriteBatch){
        this.program = program;
        this.stack = new Array<Block>();
        this.classBlocks = new ObjectMap<String, Block>();
        this.events = new Array<Event>();
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        batchState = BatchState.None;
        loadFont();

        textures = new Array<Texture>();
        fonts = new Array<BitmapFont>();
        sounds = new Array<Sound>();
        music = new Array<Music>();

        consoleInput = new TextField("");
        consoleInput.onUpdate = new Runnable() {
            @Override
            public void run() {
                consoleInput.isFocused = true;
                if(!consoleInput.getText().trim().equals("") && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                    consoleEnteredNextFrame = true;
                    consoleOutput.manuallyScrolling = false;
                }
            }
        };
        consoleInput.setSizeMode(Node.SizeMode.MANUAL);
        consoleInput.manualWidth = Gdx.graphics.getWidth();

        consoleOutput = new TextArea("");
        consoleOutput.text = "";
        consoleOutput.color.set(Color.BLACK);
        consoleOutput.setSizeMode(Node.SizeMode.MANUAL);
        consoleOutput.manualWidth = Gdx.graphics.getWidth();
        consoleOutput.manualHeight = Gdx.graphics.getHeight() - consoleInput.getHeight();

        RontoUI.addNode(consoleOutput, 0, consoleInput.getHeight());
        RontoUI.addNode(consoleInput, 0, 0);

        socketState = SocketState.None;
        server = new RontoServer();
        socket = new RontoSocket();
        client = new RontoClient();
        camera2d = null;
    }

    private void loadFont(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH + "cavestory.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48;
        defaultFont = generator.generateFont(parameter);
        generator.dispose();
    }

    public void dispose(){
        if(disposeFunc != null)
            Executor.executeBlock(disposeFunc.code, this, program.getClass("Main"), disposeFunc, new Reference[disposeFunc.parameters.size], null);

        defaultFont.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();

        for(int i = 0; i < textures.size; i++)
            textures.get(i).dispose();
        for(int i = 0; i < fonts.size; i++)
            fonts.get(i).dispose();
        for(int i = 0; i < sounds.size; i++)
            sounds.get(i).dispose();
        for(int i = 0; i < music.size; i++)
            music.get(i).dispose();

        if(socketState == SocketState.Server)
            server.server.stop();
        else if(socketState == SocketState.Client)
            client.client.stop();
        if(server.server != null) {
            try {
                server.server.dispose();
            } catch (IOException e) {
               // e.printStackTrace();
            }
        }
        if(client.client != null) {
            try {
                client.client.dispose();
            } catch (IOException e) {
               // e.printStackTrace();
            }
        }
    }

    public boolean runMain(){
        console = new Console(this);
        window = new Window(this);
        consoleEnteredNextFrame = false;
        consoleOutput.text = "";
        consoleInput.setText("");
        stack.clear();
        classBlocks.clear();
        propertyNum = 0;
        for(int i = 0; i < program.classes.size; i++)
            classBlocks.put(program.classes.get(i).name, getClassBlock(program.classes.get(i)));
        while (propertyNum > 0)
            if (resolvePropertyDependencies())
                ErrorHandler.throwPropertyError();
        if(program.classes.size > 0) {
            if(program.getClass("Main").getFunc("main", true, new String[]{}, false, this) != null)
                Executor.executeBlock(program.getClass("Main").getFunc("main", true, new String[]{}, true, this).code, this, program.getClass("Main"), program.getClass("Main").getFunc("main", true, new String[]{}, true, this), new Reference[program.getClass("Main").getFunc("main", true, new String[]{}, true, this).parameters.size], null);
            updateFunc = program.getClass("Main").getFunc("update", true, new String[]{}, false, this);
            disposeFunc = program.getClass("Main").getFunc("close", true, new String[]{}, false, this);
        }
        return updateFunc == null;
    }

    public void runUpdate(){
        console.setEntered(false);
        if(consoleEnteredNextFrame)
            console.setEntered(true);
        consoleEnteredNextFrame = false;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(camera2d != null){
            camera2d.setBatchMatrix(spriteBatch);
            camera2d.setBatchMatrix(shapeRenderer);
        }
        batchState = BatchState.None;
        checkEvents();
        Executor.executeBlock(updateFunc.code, this, program.getClass("Main"), updateFunc, new Reference[updateFunc.parameters.size], null);
        if(console.isVisible()){
            RontoUI.update(this);
        }else{
            RontoUI.updateNonUI();
        }
        if(batchState == BatchState.Sprites)
            spriteBatch.end();
        else if(batchState == BatchState.Shapes)
            shapeRenderer.end();
    }

    private Block getClassBlock(Class c){
        Block block = new Block(null);
        for(int i = 0; i <  c.classProperties.size; i++) {
            Object value = Executor.execute(c.classProperties.get(i).value, this, c, true, null, false, false).value;
            Variable.checkIfTypeAndValueMatch(c.classProperties.get(i).name, c.classProperties.get(i).type, value, this);
            block.set(c.classProperties.get(i).name, new Variable(c.classProperties.get(i).type, c.classProperties.get(i).access, value));
            if(value == ErrorHandler.Errors.MISSING_PROPERTY_DEPENDENCY) // Properties that need further dependencies to initialize.
                propertyNum++;
        }
        return block;
    }

    private boolean resolvePropertyDependencies() {
        boolean isAtDeadEnd = true;
        for (int c = 0; c < program.classes.size; c++) {
            for (int p = 0; p < program.classes.get(c).classProperties.size; p++) {
                Variable prop = classBlocks.get(program.classes.get(c).name).get(program.classes.get(c).classProperties.get(p).name);
                if(prop.getRef().value == ErrorHandler.Errors.MISSING_PROPERTY_DEPENDENCY){
                    prop.setRefValue(Executor.execute(program.classes.get(c).classProperties.get(p).value, this, program.classes.get(c), true, null, false, false).value);
                    if(prop.getRef().value != ErrorHandler.Errors.MISSING_PROPERTY_DEPENDENCY) { // Successfully assigned the property's value.
                        propertyNum--;
                        isAtDeadEnd = false;
                    }
                }
            }
        }
        return isAtDeadEnd;
    }

    public Variable getClassProp(String className, String propName, boolean mustBePublic){
        if(classBlocks.get(className) == null)
            return null;
        Variable var = classBlocks.get(className).get(propName);
        if(var != null) {
            if(var.access == Function.Access.Public || !mustBePublic)
                return var;
            else
                ErrorHandler.throwAccessError(propName);
        }
        return null;
    }

    public Class getClass(String className){
        return program.getClass(className);
    }

    public Block stackTop(){
        return stack.get(stack.size - 1);
    }

    public Variable var(String name, Block instanceBlock, boolean isMemberInstruction, boolean mustBePublic){
        if(!isMemberInstruction) {
            for (int i = stack.size - 1; i >= 0; i--)
                if (stack.get(i).get(name) != null)
                    return stack.get(i).get(name);
        }
        if(instanceBlock != null) {
            Variable var = instanceBlock.get(name);
            if(var != null) {
                if(var.access == Function.Access.Public || !mustBePublic)
                    return var;
                else
                    ErrorHandler.throwAccessError(name);
            }
        }
        return null;
    }

    //public static String parsedString(String str){ // Removes all escape characters (the first of every sequence of backslashes).
    //    if(str.charAt(0) == '"' || str.charAt(0) == '\'')
    //        return str.substring(1, str.length() - 1);
    //    return str;
    //}

    public void addBlock(Function function){
        stack.add(new Block(function));
    }

    public void removeBlock(){
        stack.removeIndex(stack.size - 1);
    }

    public void printStackSize(){
        System.out.println("Stack Size: " + stack.size);
    }

    public Reference copyOf(Object value){ // For passing a variable by value instead of reference.
        if(value instanceof Array){
            Array<Object> array = (Array<Object>)value;
            Array<Object> copy = new Array<Object>();
            for(int i = 0; i < array.size; i++)
                copy.add(copyOf(array.get(i)));
            return new Reference(copy);
        }else if(value instanceof ObjectMap){
            ObjectMap<Object, Object> map = new ObjectMap<Object, Object>();
            ObjectMap<Object, Object> copy = new ObjectMap<Object, Object>();
            Array<Object> keys = map.keys().toArray();
            for(int i = 0; i < keys.size; i++)
                copy.put(copyOf(keys.get(i)), copyOf(map.get(keys.get(i))));
            return new Reference(copy);
        }
        return new Reference(value);
    }

    private void checkEvents(){
        for(int i = 0; i < events.size; i++){
            if(events.get(i).check(this)){
                events.removeIndex(i);
                i--; // To not skip an event.
            }
        }
    }

    public void addProperty(Block properties, String name, String type, Object value){
        Variable.checkIfTypeAndValueMatch(name, type, value, this);
        properties.set(name, new Variable(type, Function.Access.Public, value));
    }

    public void addEvent(Event event){ // Either a when or whenever.
        events.add(event);
    }

    public void resize(int width, int height){
        window.properties.get("width").getRef().value = width;
        window.properties.get("height").getRef().value = height;
    }

    public BitmapFont getFont(FileHandle path, int size){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(path);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = Color.WHITE;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        fonts.add(font);
        return font;
    }

    public Texture getTexture(FileHandle file){
        Texture texture = new Texture(file);
        textures.add(texture);
        return texture;
    }

    public Sound getSound(FileHandle file){
        Sound sound = Gdx.audio.newSound(file);
        sounds.add(sound);
        return sound;
    }

    public Music getMusic(FileHandle file){
        Music m = Gdx.audio.newMusic(file);
        music.add(m);
        return m;
    }

    public void updateGlobals(boolean changeOwnProperties){
        console.update(this, changeOwnProperties);
        window.update();
    }

    public Function getPathFunc(String str, Class ownerClass){
        if(str.contains(".")){
            String[] parts = str.split(".", 2);
            return getClass(parts[0]).getFunc(parts[1], true, new String[]{"int", "int"}, true, this);
        }
        return ownerClass.getFunc(str, true, new String[]{"int", "int"}, true, this);
    }

    public void setToSpriteState(){
        if(batchState == BatchState.Shapes){
            shapeRenderer.end();
            spriteBatch.begin();
        }else if(batchState == BatchState.None){
            spriteBatch.begin();
        }
        batchState = BatchState.Sprites;
    }

    public void setToShapeState(){
        if(batchState == BatchState.Sprites){
            spriteBatch.end();
            shapeRenderer.begin();
        }else if(batchState == BatchState.None){
            shapeRenderer.begin();
        }
        batchState = BatchState.Shapes;
    }

    public void setToShapeState(ShapeRenderer.ShapeType shapeType){
        if(batchState == BatchState.Sprites){
            spriteBatch.end();
            shapeRenderer.begin(shapeType);
        }else if(batchState == BatchState.None){
            shapeRenderer.begin(shapeType);
        }else if(shapeRenderer.getCurrentType() != shapeType){
            shapeRenderer.end();
            shapeRenderer.begin(shapeType);
        }
        batchState = BatchState.Shapes;
    }

    public ObjectMap<String, Variable> getRontoObjectProperties(String name){
        if(name.equals("camera2d"))
            return new RontoCamera2D(this).getProperties();
        if(name.equals("color"))
            return new RontoColor(Color.WHITE, this).getProperties();
        if(name.equals("point"))
            return new RontoPoint(0, 0, this).getProperties();
        if(name.equals("rect"))
            return new RontoRect(0, 0, 0, 0, this).getProperties();
        if(name.equals("sprite"))
            return new RontoSprite(new Texture(IMAGE_PATH + "rontolang.png"), this).getProperties();
        return null;
    }
}
