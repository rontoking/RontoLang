package com.rontoking.rontolang.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.rontoking.rontolang.Main;
import com.rontoking.rontolang.interpreter.Interpreter;

import java.io.*;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "RontoLang";
		config.addIcon(Interpreter.ICON_PATH + "icon16.png", Files.FileType.Internal);
		config.addIcon(Interpreter.ICON_PATH + "icon32.png", Files.FileType.Internal);
		config.addIcon(Interpreter.ICON_PATH + "icon128.png", Files.FileType.Internal);
		config.vSyncEnabled = false;
		config.resizable = true;
		new LwjglApplication(new Main(), config);
	}

}