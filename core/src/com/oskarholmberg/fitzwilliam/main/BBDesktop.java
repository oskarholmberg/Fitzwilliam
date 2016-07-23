package com.oskarholmberg.fitzwilliam.main;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;

/**
 * Created by erik on 06/05/16.
 */
public class BBDesktop {

    public static void main(String[] args){
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.title= Game.TITLE;
        config.width= B2DVars.DEFAULT_GAME_WIDTH;
        config.height= B2DVars.DEFAULT_GAME_HEIGHT;

        new LwjglApplication(new Game(), config);
    }
}
