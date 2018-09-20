package com.rontoking.rontolang.rontoui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontolang.rontoui.nodes.Button;
import com.rontoking.rontolang.rontoui.nodes.VBox;

public class RightClickMenu {
    public static VBox menu;
    public static Vector2 position;
    public static Node ownerNode;
    public static String selectedOption;
    private static final Color hoverColor = Color.LIGHT_GRAY;
    private static int arrowHoveredOption;
    
    public static void init(){
        menu = new VBox();
        position = null;
        arrowHoveredOption = -1;
        ownerNode = null;
        selectedOption = null;
        menu.onUpdate = new Runnable() {
            @Override
            public void run() {
                if(position != null && (RontoUI.JUST_LEFT_CLICKED || RontoUI.JUST_RIGHT_CLICKED) && !RontoUI.isMouseInRect(menu.getRect(position.x, position.y))){
                    close();
                }else if(RontoUI.MOUSE_JUST_MOVED && RontoUI.isMouseInRect(menu.getRect(position.x, position.y))){
                    arrowHoveredOption = -1;
                }
            }
        };
        menu.borderSize = 2f;
        menu.borderColor = Color.BLACK;
        addOption("Cut");
        addOption("Copy");
        addOption("Paste");
        addOption("Delete");
        addOption("Select All");
        menu.childDelta = 1;
        menu.setChildrenWidthsToMax();
    }

    private static void addOption(final String name){
        final Button btn = new Button(name);
        btn.alignment = Node.Alignment.CENTER;
        final int index = menu.children.size;
        btn.onPress = new Runnable() {
            @Override
            public void run() {
                selectedOption = name;
                close();
            }
        };
        btn.onUpdate = new Runnable() {
            @Override
            public void run() {
                if (arrowHoveredOption == -1) {
                    if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && position != null && RontoUI.isMouseInRect(btn.getRect(menu.getChildX(index, position.x), menu.getChildY(index, position.y))))
                        btn.releasedColor = hoverColor;
                    else
                        btn.releasedColor = Color.WHITE;
                } else {
                    if (arrowHoveredOption == index)
                        btn.releasedColor = hoverColor;
                    else
                        btn.releasedColor = Color.WHITE;
                }
            }
        };
        menu.children.add(btn);
    }

    protected static void update(){
        if(position != null){
            if(RontoUI.IS_KEY_TYPED && RontoUI.TYPED_KEY == Input.Keys.UP){
                arrowHoveredOption--;
                if(arrowHoveredOption < 0)
                    arrowHoveredOption = menu.children.size - 1;
            }
            else if(RontoUI.IS_KEY_TYPED && RontoUI.TYPED_KEY == Input.Keys.DOWN){
                arrowHoveredOption++;
                if(arrowHoveredOption > menu.children.size - 1)
                    arrowHoveredOption = 0;
            }else if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && arrowHoveredOption != -1){
                selectedOption = ((Button)menu.children.get(arrowHoveredOption)).getText();
                close();
            }

            menu.update(position.x, position.y);
        }
    }

    protected static void render(SpriteBatch spriteBatch){
        if(position != null){
            menu.render(spriteBatch, position.x, position.y);
        }
    }

    public static void open(Node n){
        /*
        position = new Vector2(RontoUI.getMouseX(), RontoUI.getMouseY());
        arrowHoveredOption = -1;
        ownerNode = n;
        selectedOption = null;
        */
    }

    public static void close(){
        position = null;
    }
}
