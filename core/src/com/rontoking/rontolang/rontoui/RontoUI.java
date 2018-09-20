package com.rontoking.rontolang.rontoui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.interpreter.Reference;
import com.rontoking.rontolang.rontoui.nodes.RadioButton;

public class RontoUI implements InputProcessor {
    private static Array<Node> nodes;
    private static Array<Vector2> positions;

    private static final float KEY_REPEAT_DELAY = 0.3f;
    private static final float KEY_REPEAT_DELTA = 0.1f;
    private static final float DOUBLE_CLICK_LEFT_TIME = 0.3f;
    private static final float DOUBLE_CLICK_RIGHT_TIME = 0.3f;

    public static boolean JUST_LEFT_CLICKED = false;
    public static boolean JUST_RIGHT_CLICKED = false;
    public static boolean JUST_LEFT_RELEASED = false;
    public static boolean JUST_RIGHT_RELEASED = false;
    public static boolean MOUSE_JUST_MOVED = false;
    public static boolean JUST_DOUBLE_CLICKED_LEFT = false;
    public static boolean JUST_DOUBLE_CLICKED_RIGHT = false;
    public static int SCROLL_AMOUNT = 0;
    public static Array<Reference> PRESSED_KEYS = new Array<Reference>();

    public static final Color TEXT_MARKER_COLOR = Color.BLACK;
    public static final Color TEXT_SELECTION_COLOR = new Color(0.0f, 0.0f, 1f, 0.5f);
    public static final float TEXT_MARKER_WIDTH = 3;

    private static float keyTypedTimeLeft;
    private static KeyTypedState keyTypedState;
    private static float doubleClickLeftTimeLeft, doubleClickRightTimeLeft;
    private static Array<RadioButton> RADIO_BUTTONS;

    private static float DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT;

    public enum KeyTypedState{
        None, Delay, Repeat
    }

    public static int TYPED_KEY;
    public static boolean IS_KEY_TYPED;
    public static boolean IS_CHAR_TYPED;

    public static void init(){
        DEFAULT_WINDOW_WIDTH = Gdx.graphics.getWidth();
        DEFAULT_WINDOW_HEIGHT = Gdx.graphics.getHeight();

        nodes = new Array<Node>();
        positions = new Array<Vector2>();

        Gdx.input.setInputProcessor(new RontoUI());
        Assets.load();
        nodes = new Array<Node>();

        IS_KEY_TYPED = false;
        IS_CHAR_TYPED = false;
        keyTypedTimeLeft = 0;
        keyTypedState = KeyTypedState.None;
        RADIO_BUTTONS = new Array<RadioButton>();

        RightClickMenu.init();
    }

    public static void addNode(Node node, float x, float y){
        nodes.add(node);
        positions.add(new Vector2(x, y));
    }

    public static void update(Interpreter interpreter){
        for(int i = 0; i < nodes.size; i++){
            nodes.get(i).update(positions.get(i).x, positions.get(i).y);
        }
        RightClickMenu.update();

        interpreter.setToSpriteState();
        for(int i = 0; i < nodes.size; i++){
            nodes.get(i).render(interpreter.spriteBatch, positions.get(i).x, positions.get(i).y);
        }
        RightClickMenu.render(interpreter.spriteBatch);
        updateNonUI();
    }

    public static void updateNonUI(){
        updateTypedKey();
        if(doubleClickLeftTimeLeft > 0)
            doubleClickLeftTimeLeft -= Gdx.graphics.getDeltaTime();
        if(doubleClickRightTimeLeft > 0)
            doubleClickRightTimeLeft -= Gdx.graphics.getDeltaTime();
        JUST_LEFT_CLICKED = false;
        JUST_RIGHT_CLICKED = false;
        JUST_LEFT_RELEASED = false;
        JUST_RIGHT_RELEASED = false;
        MOUSE_JUST_MOVED = false;
        JUST_DOUBLE_CLICKED_LEFT = false;
        JUST_DOUBLE_CLICKED_RIGHT = false;
        SCROLL_AMOUNT = 0;

        PRESSED_KEYS.clear();
        for(int i = 0; i < 256; i++){
            if(Gdx.input.isKeyPressed(i))
                PRESSED_KEYS.add(new Reference(Input.Keys.toString(i)));
        }
    }

    private static void updateTypedKey(){
        IS_KEY_TYPED = false;
        IS_CHAR_TYPED = false;

        if(keyTypedState != KeyTypedState.None){
            keyTypedTimeLeft -= Gdx.graphics.getDeltaTime();
            if(keyTypedTimeLeft <= 0){
                IS_KEY_TYPED = true;
                keyTypedState = KeyTypedState.Repeat;
                keyTypedTimeLeft = KEY_REPEAT_DELTA;
            }
        }
    }

    public static void dispose(){
        Assets.dispose();
    }

    public static float getTextWidth(String text, BitmapFont font){
        Assets.glyphLayout.setText(font, text);
        return Assets.glyphLayout.width;
    }

    public static float getTextHeight(String text, BitmapFont font){
        Assets.glyphLayout.setText(font, text);
        return Assets.glyphLayout.height;
    }

    public static float getTextHeight(String text, BitmapFont font, float width){
        Assets.glyphLayout.setText(font, text, font.getColor(), width, -1, true);
        return Assets.glyphLayout.height;
    }

    public static float getMouseX(){
        return Gdx.input.getX() * DEFAULT_WINDOW_WIDTH / Gdx.graphics.getWidth();
    }

    public static float getMouseY(){
        return (Gdx.graphics.getHeight() - Gdx.input.getY()) * DEFAULT_WINDOW_HEIGHT / Gdx.graphics.getHeight();
    }

    public static boolean isMouseInRect(Rectangle rectangle){
        return rectangle.contains(getMouseX(), getMouseY());
    }

    public static BitmapFont getColoredFontCopy(Font font){ // Used for label's updateTextTexture.
        return new Font(font.font.getColor()).font;
    }

    public static void addRadioButton(RadioButton radioButton){
        RADIO_BUTTONS.add(radioButton);
    }

    public static void deSelectRadioButtons(RadioButton radioButton){
        for(int i = 0; i < RADIO_BUTTONS.size; i++){
            if(RADIO_BUTTONS.get(i).ID == radioButton.ID && RADIO_BUTTONS.get(i) != radioButton){
                RADIO_BUTTONS.get(i).deSelect();
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        keyTypedState = KeyTypedState.None;
        TYPED_KEY = keycode;
        IS_KEY_TYPED = true;
        if(keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT || keycode == Input.Keys.UP || keycode == Input.Keys.DOWN){
            keyTypedState = KeyTypedState.Delay;
            keyTypedTimeLeft = KEY_REPEAT_DELAY;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == TYPED_KEY)
            keyTypedState = KeyTypedState.None;
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if(!Character.isISOControl(character)) {
            TYPED_KEY = character;
            IS_CHAR_TYPED = true;
        }
        IS_KEY_TYPED = true;
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT) {
            JUST_LEFT_CLICKED = true;
            if(doubleClickLeftTimeLeft > 0){
                JUST_DOUBLE_CLICKED_LEFT = true;
                doubleClickLeftTimeLeft = 0;
            }else {
                doubleClickLeftTimeLeft = DOUBLE_CLICK_LEFT_TIME;
            }
        }
        else if(button == Input.Buttons.RIGHT) {
            JUST_RIGHT_CLICKED = true;
            if(doubleClickRightTimeLeft > 0){
                JUST_DOUBLE_CLICKED_RIGHT = true;
                doubleClickRightTimeLeft = 0;
            }else {
                doubleClickRightTimeLeft = DOUBLE_CLICK_RIGHT_TIME;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT)
            JUST_LEFT_RELEASED = true;
        else if(button == Input.Buttons.RIGHT)
            JUST_RIGHT_RELEASED = true;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        MOUSE_JUST_MOVED = true;
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        SCROLL_AMOUNT = amount;
        return false;
    }
}
