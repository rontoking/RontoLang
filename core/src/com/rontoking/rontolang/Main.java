package com.rontoking.rontolang;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rontoking.rontolang.interpreter.Interpreter;
import com.rontoking.rontolang.parser.Parser;
import com.rontoking.rontolang.program.Function;
import com.rontoking.rontolang.rontoui.RontoUI;

public class Main extends ApplicationAdapter {
	public static Interpreter interpreter;

	@Override
	public void create () {
		RontoUI.init();
		interpreter = new Interpreter(Parser.parseProgram(getCode(), "Main", "main"), new SpriteBatch());
		if(interpreter.runMain())
			Gdx.app.exit();
	}

	@Override
	public void render () {
		interpreter.runUpdate();
	}

	@Override
	public void dispose () {
	    RontoUI.dispose();
		interpreter.dispose();
	}

	@Override
    public void resize(int width, int height){
        interpreter.resize(width, height);
    }

	private String getCode(){
		if(Gdx.files.internal(Interpreter.CODE_PATH + "code").exists())
			return Gdx.files.internal(Interpreter.CODE_PATH + "code").readString();
		return "";
	}
}
