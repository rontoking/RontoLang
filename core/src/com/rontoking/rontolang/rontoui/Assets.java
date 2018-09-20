package com.rontoking.rontolang.rontoui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Assets {
    public static NinePatch node;
    public static Texture pixel, slider, checkBox, radioButton;
    public static GlyphLayout glyphLayout;
    public static FreeTypeFontGenerator generator;

    static void load() {
        node = new NinePatch(new Texture(Gdx.files.internal("rontoui/node.png")), 8, 8, 8, 8);
        pixel = new Texture(Gdx.files.internal("rontoui/pixel.png"));
        slider = new Texture(Gdx.files.internal("rontoui/slider.png"));
        checkBox = new Texture(Gdx.files.internal("rontoui/checkbox.png"));
        radioButton = new Texture(Gdx.files.internal("rontoui/radiobutton.png"));
        glyphLayout = new GlyphLayout();
        loadFontGenerator();
    }

    private static void loadFontGenerator() {
        FreeTypeFontGenerator.setMaxTextureSize(2048);
        generator = new FreeTypeFontGenerator(Gdx.files.internal("rontoui/cavestory.ttf"));
    }

    static void dispose() {
        node.getTexture().dispose();
        pixel.dispose();
        slider.dispose();
        checkBox.dispose();
        radioButton.dispose();
        generator.dispose();
    }
}
