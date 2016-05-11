package com.game.bb.main;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 06/05/16.
 */
public class BBDesktop {

    public static void main(String[] args){
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.title= Game.TITLE;
        config.width= B2DVars.CAM_WIDTH*2;
        config.height=B2DVars.CAM_HEIGHT*2;

        new LwjglApplication(new Game(), config);
    }
}
