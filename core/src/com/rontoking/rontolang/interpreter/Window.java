package com.rontoking.rontolang.interpreter;

import com.badlogic.gdx.Gdx;

public class Window {
    public Block properties;
    private String lastTitle;
    private boolean isResizable, vSyncOn, isDecorated;

    public Window(Interpreter interpreter){
        properties = new Block(null);
        lastTitle = "RontoLang";
        isResizable = true;
        vSyncOn = false;
        isDecorated = true;
        interpreter.addProperty(properties, "width", "int", Gdx.graphics.getWidth());
        interpreter.addProperty(properties, "height", "int", Gdx.graphics.getHeight());
        interpreter.addProperty(properties, "title", "str", lastTitle);
        interpreter.addProperty(properties, "fullscreen", "bool", false);
        interpreter.addProperty(properties, "resizable", "bool", isResizable);
        interpreter.addProperty(properties, "vsync", "bool", vSyncOn);
        interpreter.addProperty(properties, "decorated", "bool", isDecorated);
    }

    public void update(){
        if(!properties.get("title").getRef().value.toString().equals(lastTitle)) {
            lastTitle = properties.get("title").getRef().value.toString();
            Gdx.graphics.setTitle(lastTitle);
        }if(Gdx.graphics.isFullscreen() != (Boolean)properties.get("fullscreen").getRef().value){
            if(Gdx.graphics.isFullscreen())
                Gdx.graphics.setWindowedMode((int)Variable.getNum(properties.get("width").getRef().value), (int)Variable.getNum(properties.get("height").getRef().value));
            else
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }if(!Gdx.graphics.isFullscreen() && (Gdx.graphics.getWidth() != (int)Variable.getNum(properties.get("width").getRef().value) || Gdx.graphics.getHeight() != (int)Variable.getNum(properties.get("height").getRef().value))) {
            Gdx.graphics.setWindowedMode((int)Variable.getNum(properties.get("width").getRef().value), (int)Variable.getNum(properties.get("height").getRef().value));
        }if((Boolean)properties.get("resizable").getRef().value != isResizable){
            isResizable = !isResizable;
            Gdx.graphics.setResizable(isResizable);
        }if((Boolean)properties.get("vsync").getRef().value != vSyncOn){
            vSyncOn = !vSyncOn;
            Gdx.graphics.setVSync(vSyncOn);
        }if((Boolean)properties.get("decorated").getRef().value != isDecorated){
            isDecorated = !isDecorated;
            Gdx.graphics.setUndecorated(!isDecorated);
        }
    }
}
