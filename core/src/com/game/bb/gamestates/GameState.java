package com.game.bb.gamestates;


import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.game.bb.main.Game;
import com.game.bb.net.NetworkMonitor;

/**
 * Created by erik on 06/05/16.
 */
public abstract class GameState {
    protected com.game.bb.handlers.GameStateManager gsm;
    protected Game game;
    protected SpriteBatch sb;
    protected OrthographicCamera cam;
    protected OrthographicCamera hudCam;
    protected NetworkMonitor mon;

    protected GameState(com.game.bb.handlers.GameStateManager gsm, NetworkMonitor mon){
        this.gsm=gsm;
        this.mon=mon;
        game=gsm.game();
        sb = game.getBatch();
        cam = game.getCam();
        hudCam = game.getHudCam();

    }

    public abstract void handleInput();
    public abstract void update(float dt);
    public abstract void render();
    public abstract void dispose();

}
