package com.game.bb.handlers;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;

/**
 * Created by erik on 09/05/16.
 */
public class Content {
    private HashMap<String, Texture> textures;

    public Content(){
        textures = new HashMap<String, Texture>();
    }

    public void loadTexture(String path, String key){
        textures.put(key, new Texture(path));
    }
}
