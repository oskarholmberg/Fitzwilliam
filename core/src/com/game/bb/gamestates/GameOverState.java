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
public class GameOverState extends GameState {

    private SPButton backbutton;
    private World world;
    private Texture background = new Texture("images/spaceBackground.png");
    private Sound sound = Gdx.audio.newSound(Gdx.files.internal("sfx/levelselect.wav"));
    private Texture gameOver = new Texture("images/font/gameOver.png");



    public GameOverState(GameStateManager gsm) {
        super(gsm);
        world = new World(new Vector2(0, -9.81f), true);
        backbutton = new SPButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
    }

    @Override
    public void handleInput() {
        if (backbutton.isClicked()) {
            sound.play();
            gsm.setState(GameStateManager.CONNECT);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        world.step(dt, 6, 2);
        backbutton.update(dt);
    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(background, 0, 0);
        sb.draw(gameOver, 100, cam.viewportHeight-120, 600, 30);
        sb.end();
        backbutton.render(sb);
    }

    @Override
    public void dispose() {

    }
}
