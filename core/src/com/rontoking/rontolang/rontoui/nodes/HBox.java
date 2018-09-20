package com.rontoking.rontolang.rontoui.nodes;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontolang.rontoui.Node;

public class HBox extends Node{
    public float childDelta;

    public HBox(){
        super();
        childDelta = 5;
    }

    @Override
    public float getPrefWidth(){
        float totalWidth = -childDelta;
        for(int i = 0; i < children.size; i++){
            totalWidth += children.get(i).getWidth() + childDelta;
        }
        return marginLeft + totalWidth + marginRight;
    }

    @Override
    public float getPrefHeight(){
        float maxHeight = 0;
        for(int i = 0; i < children.size; i++){
            if(children.get(i).getHeight() > maxHeight)
                maxHeight = children.get(i).getHeight();
        }
        return marginDown + maxHeight + marginUp;
    }

    @Override
    public float getChildX(int index, float x){
        float pos = 0;
        for(int i = 0; i < index; i++)
            pos += children.get(i).getWidth() + childDelta;
        return x + marginLeft + pos;
    }

    public Array<Node> getChildren(){
        return children;
    }
}
