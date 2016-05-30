package com.game.bb.gamestates;


import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.game.bb.main.MainGame;

/**
 * Created by erik on 06/05/16.
 */
public abstract class GameState {
    protected com.game.bb.handlers.GameStateManager gsm;
    protected MainGame mainGame;
    protected SpriteBatch sb;
    public OrthographicCamera cam;
    protected OrthographicCamera hudCam;

    protected GameState(com.game.bb.handlers.GameStateManager gsm){
        this.gsm=gsm;
        mainGame =gsm.game();
        sb = mainGame.getBatch();
        cam = mainGame.getCam();
        hudCam = mainGame.getHudCam();

    }

    public abstract void handleInput();
    public abstract void update(float dt);
    public abstract void render();
    public abstract void dispose();

}
