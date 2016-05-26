package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.game.bb.handlers.GameStateManager;

/**
 * Created by erik on 12/05/16.
 */
public class GameOverState extends GameState {

    private SPButton backbutton;
    private World world;
    private Texture background = new Texture("images/spaceBackground.png");
    private Texture gameOver = new Texture("images/font/gameOver.png");
    private Texture[] placings, players;
    private Sound sound = Gdx.audio.newSound(Gdx.files.internal("sfx/levelselect.wav"));
    private ArrayMap<String, Array<String>> killedByEntities;



    public GameOverState(GameStateManager gsm, ArrayMap<String, Array<String>> killedByEntities) {
        super(gsm);
        this.killedByEntities = killedByEntities;
        world = new World(new Vector2(0, -9.81f), true);
        backbutton = new SPButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
        placings = new Texture[3];
        placings[0] = new Texture("images/font/golden1.png");
        placings[1] = new Texture("images/font/silver2.png");
        placings[2] = new Texture("images/font/bronze3.png");
        players = new Texture[2];
        players[0] = new Texture("images/players/bluePlayerStandRight.png");
        players[2] = new Texture("images/players/redPlayerStandRight.png");
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
        sb.draw(gameOver, cam.viewportWidth/4, cam.viewportHeight-130, 350, 30);
        for (String color : killedByEntities.keys()) {
            int i = 0;
            sb.draw();
        }
        sb.end();
        backbutton.render(sb);
    }

    @Override
    public void dispose() {

    }
}
