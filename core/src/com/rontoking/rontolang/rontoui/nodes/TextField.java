package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.rontoui.Assets;
import com.rontoking.rontolang.rontoui.Font;
import com.rontoking.rontolang.rontoui.RightClickMenu;
import com.rontoking.rontolang.rontoui.RontoUI;

public class TextField extends Label{
    private static final String DEFAULT_TEXT = "THIS IS DEFAULT";
    private static final float MARKER_VISIBLE_SECONDS = 0.5f;
    private static final float MARKER_INVISIBLE_SECONDS = 0.5f;

    public int maxSize;
    private boolean isMarkerVisible;
    private float markerTimeLeft;
    private int charOffset;
    private int markerIndex;
    private int selectionStart, selectionEnd;
    private boolean isSelecting;

    public TextField(String text) {
        super(text);
        maxSize = 75;
        alignment = Alignment.LEFT;
        charOffset = 0;
        markerIndex = text.length();
        selectionStart = -1;
        selectionEnd = -1;
        isSelecting = false;
    }

    public TextField(String text, Font font) {
        super(text, font);
        maxSize = 100;
        alignment = Alignment.LEFT;
        charOffset = 0;
        markerIndex = text.length();
        selectionStart = -1;
        selectionEnd = -1;
        isSelecting = false;
    }

    @Override
    public float getPrefWidth(){
        return marginLeft + RontoUI.getTextWidth(DEFAULT_TEXT, font.font) + marginRight;
    }

    @Override
    public void update(float x, float y) {
        super.update(x, y);
        if(isFocused && RontoUI.JUST_DOUBLE_CLICKED_LEFT) {
            //doSelectAll();
        }else {
            updateMarker();
            checkTypedKey();
            checkSelection(x);
            updateMenu();
        }
    }

    private void updateMenu(){
        if(isFocused && RontoUI.JUST_RIGHT_CLICKED){
           RightClickMenu.open(this);
        }if(RightClickMenu.ownerNode == this && RightClickMenu.position == null && RightClickMenu.selectedOption != null){
            isFocused = true;
            RightClickMenu.ownerNode = null;
            if(RightClickMenu.selectedOption.equals("Cut"))
                doCut();
            else if(RightClickMenu.selectedOption.equals("Copy"))
                doCopy();
            else if(RightClickMenu.selectedOption.equals("Paste"))
                doPaste();
            else if(RightClickMenu.selectedOption.equals("Delete")) {
                if(getSelection().length() > 0) {
                    doBackSpace();
                }
            }else if(RightClickMenu.selectedOption.equals("Select All")) {
                doSelectAll();
            }
        }
    }

    private void checkSelection(float x){
        if(isFocused && text.length() > 0) {
            if (RontoUI.JUST_LEFT_CLICKED) {
                markerIndex = getCharIndexAtMousePos(x) + charOffset;
                selectionStart = getCharIndexAtMousePos(x) + charOffset;
                selectionEnd = selectionStart;
                isSelecting = true;
            }else if(isSelecting && selectionStart != -1 && Gdx.input.isButtonPressed(Input.Buttons.LEFT )&& RightClickMenu.position == null && !RontoUI.JUST_LEFT_RELEASED) {
                selectionEnd = getCharIndexAtMousePos(x) + charOffset;
                markerIndex = selectionEnd;
                if (RontoUI.getMouseX() < x + marginLeft) {
                    if (charOffset > 0) {
                        charOffset--;
                        super.updateTextTexture();
                    }
                } else if (RontoUI.getMouseX() > x + marginLeft + getWidth()) {
                    if (!isMarkerInSight()) {
                        charOffset++;
                        super.updateTextTexture();
                    }
                }
            }
        }else if(text.length() == 0 || (RightClickMenu.position == null && RightClickMenu.ownerNode != this)){
            selectionStart = -1;
            selectionEnd = -1;
        }
        if(RontoUI.JUST_LEFT_RELEASED){
            isSelecting = false;
        }
    }

    private int getCharIndexAtMousePos(float x){
        String txt = renderedText();
        for(int i = 0; i < txt.length(); i++){
            if(x + marginLeft + RontoUI.getTextWidth(txt.substring(0, i + 1), font.font) > RontoUI.getMouseX()){
                return i;
            }
        }
        return txt.length();
    }

    private void checkTypedKey() {
        if (isFocused && RontoUI.IS_KEY_TYPED) {
            if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)){
                text = "";
                charOffset = 0;
                markerIndex = 0;
            }
            else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) { // Paste
                if (RontoUI.TYPED_KEY == Input.Keys.V) {
                    RightClickMenu.close();
                    doPaste();
                } else if (RontoUI.TYPED_KEY == Input.Keys.C) {
                    RightClickMenu.close();
                    doCopy();
                } else if (RontoUI.TYPED_KEY == Input.Keys.X) {
                    RightClickMenu.close();
                    doCut();
                }
            } else if (RontoUI.IS_CHAR_TYPED) {
                RightClickMenu.close();
                removeSelection();
                if (text.length() < maxSize) {
                    text = text.substring(0, markerIndex) + (char) RontoUI.TYPED_KEY + text.substring(markerIndex);
                    markerIndex++;
                    while (!isMarkerInSight()) {
                        charOffset++;
                    }
                    updateTextTexture();
                }
            } else if (RontoUI.TYPED_KEY == Input.Keys.RIGHT) {
                RightClickMenu.close();
                if (isSelected()) {
                    markerIndex = Math.max(selectionStart, selectionEnd) - 1;
                    selectionStart = selectionEnd = -1;
                }
                if (markerIndex < text.length()) {
                    markerIndex++;
                    if (!isMarkerInSight()) {
                        charOffset++;
                    }
                    updateTextTexture();
                }
            } else if (RontoUI.TYPED_KEY == Input.Keys.LEFT) {
                RightClickMenu.close();
                if (isSelected()) {
                    markerIndex = Math.min(selectionStart, selectionEnd) + 1;
                    selectionStart = selectionEnd = -1;
                }
                if (markerIndex > 0) {
                    markerIndex--;
                    if (charOffset > 0 && !isMarkerInSight()) {
                        charOffset--;
                    }
                    updateTextTexture();
                }
            } else if (RontoUI.TYPED_KEY == Input.Keys.BACKSPACE) {
                RightClickMenu.close();
                doBackSpace();
            }
            initMarker();
        } else if (!isFocused && isMarkerVisible) {
            isMarkerVisible = false;
        }
    }

    private void updateMarker(){
        if(RontoUI.JUST_LEFT_CLICKED){
            initMarker();
        }else if(isFocused || RightClickMenu.ownerNode == this){
            markerTimeLeft -= Gdx.graphics.getDeltaTime();
            if (markerTimeLeft <= 0) {
                isMarkerVisible = !isMarkerVisible;
                if (isMarkerVisible)
                    markerTimeLeft = MARKER_VISIBLE_SECONDS;
                else
                    markerTimeLeft = MARKER_INVISIBLE_SECONDS;
            }
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch, float x, float y){
        super.render(spriteBatch, x, y);
        if(isSelected()){
            Color c = new Color(spriteBatch.getColor());
            spriteBatch.setColor(RontoUI.TEXT_SELECTION_COLOR);
            int start = Math.min(selectionStart, selectionEnd);
            int end = Math.max(selectionStart, selectionEnd);
            start -= charOffset;
            end -= charOffset;
            start = Math.max(start, 0);

            float width = RontoUI.getTextWidth(renderedText().substring(0, end), font.font) - RontoUI.getTextWidth(renderedText().substring(0, start), font.font);
            if(RontoUI.getTextWidth(renderedText().substring(0, start), font.font) + width > getWidth() - marginRight - marginLeft)
                width = getWidth() - marginRight - marginLeft - RontoUI.getTextWidth(renderedText().substring(0, start), font.font);
            if(width > 0)
                spriteBatch.draw(Assets.pixel, x + marginLeft + RontoUI.getTextWidth(renderedText().substring(0, start), font.font), y, width, getHeight());
            spriteBatch.setColor(c);
        }else if(isMarkerVisible) {
            Color c = new Color(spriteBatch.getColor());
            spriteBatch.setColor(RontoUI.TEXT_MARKER_COLOR);
            spriteBatch.draw(Assets.pixel, x + marginLeft + RontoUI.getTextWidth(renderedText().substring(0, getMarkerPos()), font.font), y, RontoUI.TEXT_MARKER_WIDTH, getHeight());
            spriteBatch.setColor(c);
        }
    }

    private boolean isMarkerInSight(){
        return text.length() <= 1 || (getMarkerPos() >= 1 && RontoUI.getTextWidth(renderedText().substring(0, getMarkerPos()), font.font) + RontoUI.TEXT_MARKER_WIDTH < getWidth() - marginLeft);
    }

    private void removeSelection(){
        if(isSelected()) {
            markerIndex = Math.min(selectionStart, selectionEnd);
            text = text.substring(0, markerIndex) + text.substring(Math.max(selectionStart, selectionEnd));
            charOffset -= Math.abs(selectionStart - selectionEnd);
            if(charOffset < 0)
                charOffset = 0;
            updateTextTexture();
        }
    }

    public String getSelection(){
        if(isSelected()){
            return text.substring(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd));
        }
        return "";
    }

    private void initMarker(){
        isMarkerVisible = true;
        markerTimeLeft = MARKER_VISIBLE_SECONDS;
    }

    private int getMarkerPos(){
        return markerIndex - charOffset;
    }

    private boolean isSelected(){
        return selectionStart != -1 && selectionStart != selectionEnd;
    }

    private void doBackSpace(){
        if(isSelected()){
            removeSelection();
        }else if (markerIndex > 0) {
            text = text.substring(0, markerIndex - 1) + text.substring(markerIndex);
            markerIndex--;
            if(charOffset > 0) {
                charOffset--;
            }
            updateTextTexture();
        }
    }

    private void doCut(){
        if(doCopy())
            doBackSpace();
    }

    private void doSelectAll(){
        selectionStart = 0;
        selectionEnd = text.length();
        isSelecting = false;
    }

    private boolean doCopy(){
        if(getSelection().length() > 0) {
            Gdx.app.getClipboard().setContents(getSelection());
            return true;
        }
        return false;
    }

    private void doPaste(){
        removeSelection();
        String cb = Gdx.app.getClipboard().getContents();
        cb = cb.replace('\n', ' ');
        if (cb.length() > maxSize - text.length())
            cb = cb.substring(0, maxSize - text.length());
        text = text.substring(0, markerIndex) + cb + text.substring(markerIndex);
        markerIndex += cb.length();
        if (text.length() > maxSize) {
            text = text.substring(0, maxSize);
            markerIndex = text.length();
        }
        while (!isMarkerInSight()) {
            charOffset++;
        }
        updateTextTexture();
    }

    @Override
    protected void updateTextTexture(){
        selectionStart = -1;
        selectionEnd = -1;
        if(text.length() == 0) {
            markerIndex = 0;
            charOffset = 0;
        }
        super.updateTextTexture();
    }

    @Override
    public String renderedText(){
        return text.substring(charOffset);
    }
}
