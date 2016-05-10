package com.game.bb.main;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * Created by erik on 06/05/16.
 */
public class BBDesktop {

    public static void main(String[] args){
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.title= Game.TITLE;
        config.width=Game.WIDTH*2;
        config.height=Game.HEIGHT*2;

        new LwjglApplication(new Game(), config);
    }
}
