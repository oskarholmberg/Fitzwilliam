package com.game.bb.gamestates;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.IntMap;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.GameStateManager;

/**
 * Created by erik on 02/06/16.
 */
public class SelectMapState extends GameState {

    private World world;
    private SPButton backButton;
    private Texture background = Assets.getBackground();
    private IntMap<SPButton> mapButtons;

    protected SelectMapState(GameStateManager gsm) {
        super(gsm);
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {

    }

    @Override
    public void dispose() {

    }
}
