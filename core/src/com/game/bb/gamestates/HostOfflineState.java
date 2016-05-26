package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.GameStateManager;

/**
 * Created by erik on 12/05/16.
 */
public class HostOfflineState extends GameState {

    private World world;
    private Texture background = new Texture("images/spaceBackground.png");
    private Texture hostOffline = new Texture("images/font/hostOffline.png");
    private float timeout = 0f;


    public HostOfflineState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, -9.81f), true);
    }

    @Override
    public void handleInput() {

    }


    @Override
    public void update(float dt) {
        handleInput();
        if(timeout > 5f){
            gsm.setState(GameStateManager.CONNECT);
        }
        else {
            timeout += dt;
        }
        world.step(dt, 6, 2);
    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(background, 0, 0);
        sb.draw(hostOffline, cam.viewportWidth/7, cam.viewportHeight/2);
        sb.end();
    }

    @Override
    public void dispose() {
        background.dispose();
        hostOffline.dispose();
    }
}
