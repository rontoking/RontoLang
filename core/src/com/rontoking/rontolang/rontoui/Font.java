package com.rontoking.rontolang.rontoui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Font {
    public BitmapFont font;
    public FreeTypeFontGenerator.FreeTypeFontParameter parameter;

    public Font(Color color){
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.magFilter = Texture.TextureFilter.Nearest;
        parameter.minFilter = Texture.TextureFilter.Nearest;
        parameter.color = new Color(color);
        parameter.size = 24;
        font = Assets.generator.generateFont(parameter);
        font.setColor(parameter.color);
    }

    public Font(Color color, int size, Color borderColor, int borderSize){
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.magFilter = Texture.TextureFilter.Nearest;
        parameter.minFilter = Texture.TextureFilter.Nearest;
        parameter.color = new Color(color);
        parameter.size = size;
        parameter.borderColor = new Color(borderColor);
        parameter.borderWidth = borderSize;
        font = Assets.generator.generateFont(parameter);
        font.setColor(parameter.color);
    }
}
