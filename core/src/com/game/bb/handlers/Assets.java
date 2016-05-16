package com.game.bb.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

/**
 * Created by erik on 16/05/16.
 */
public class Assets {
    public static final Texture TEX_BACKGROUND = new Texture("images/spaceBackground.png"),
    TEX_POWER = new Texture("images/powerUpBox.png");
    public static final Sound SOUND_RELOAD = Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav")),
        SOUND_EMPTY_CLIP = Gdx.audio.newSound(Gdx.files.internal("sfx/emptyClip.wav"));
}
