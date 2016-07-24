package com.oskarholmberg.fitzwilliam.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;
import com.oskarholmberg.fitzwilliam.main.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title= Game.TITLE;
		config.width= B2DVars.DEFAULT_GAME_WIDTH;
		config.height= B2DVars.DEFAULT_GAME_HEIGHT;

		new LwjglApplication(new Game(), config);
	}
}
