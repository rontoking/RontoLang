package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.rontoui.Node;

public class VBox extends Node{
    public float childDelta;

    public VBox(){
        super();
        childDelta = 5;
    }

    @Override
    public float getPrefWidth(){
        float maxWidth = 0;
        for(int i = 0; i < children.size; i++){
            if(children.get(i).getWidth() > maxWidth)
                maxWidth = children.get(i).getWidth();
        }
        return marginLeft + maxWidth + marginRight;
    }

    @Override
    public float getPrefHeight(){
        float totalHeight = -childDelta;
        for(int i = 0; i < children.size; i++){
            totalHeight += children.get(i).getHeight() + childDelta;
        }
        return marginUp + totalHeight + marginDown;
    }

    public Array<Node> getChildren(){
        return children;
    }

    public void setChildrenWidthsToMax(){
        for(int i = 0; i < children.size; i++){
            children.get(i).setSizeMode(SizeMode.MANUAL);
            children.get(i).manualWidth = getWidth() - marginLeft - marginRight;
            children.get(i).manualHeight = children.get(i).getPrefHeight();
        }
    }

    @Override
    public float getChildY(int index, float y){
        float pos = -childDelta;
        for(int i = 0; i <= index; i++)
            pos += children.get(i).getHeight() + childDelta;
        return getTop(y, true) - pos;
    }
}
