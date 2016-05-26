package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private Texture[] placings, players, bullets;
    private TextureRegion[] grenades;
    private Sound sound = Gdx.audio.newSound(Gdx.files.internal("sfx/levelselect.wav"));
    private ArrayMap<String, Array<String>> killedByEntities;
    private String[] victoryOrder;


    public GameOverState(GameStateManager gsm, ArrayMap<String, Array<String>> killedByEntities, String victoryOrder) {
        super(gsm);
        this.killedByEntities = killedByEntities;
        this.victoryOrder = victoryOrder.split(":");
        world = new World(new Vector2(0, -9.81f), true);
        backbutton = new SPButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
        placings = new Texture[3];
        placings[0] = new Texture("images/font/golden1.png");
        placings[1] = new Texture("images/font/silver2.png");
        placings[2] = new Texture("images/font/bronze3.png");
        players = new Texture[2];
        players[0] = new Texture("images/player/bluePlayerStandRight.png");
        players[1] = new Texture("images/player/redPlayerStandRight.png");
        grenades = new TextureRegion[2];
        grenades[0] = TextureRegion.split(new Texture("images/weapons/blueGrenade.png"), 30, 30)[0][0];
        grenades[1] = TextureRegion.split(new Texture("images/weapons/redGrenade.png"), 30, 30)[0][0];
        bullets = new Texture[2];
        bullets[0] = new Texture("images/weapons/blueBullet.png");
        bullets[1] = new Texture("images/weapons/redBullet.png");
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
        sb.draw(gameOver, cam.viewportWidth / 4, cam.viewportHeight - 130, 350, 30);
        int i = 0;
        for (String color : victoryOrder) {
            int k = 0;
            int j = 0;
            sb.draw(placings[i], cam.viewportWidth / 5, (cam.viewportHeight - 200) - (50 * i), 38, 48);
            sb.draw(getPlayerTexture(color), cam.viewportWidth / 5 + 100, (cam.viewportHeight - 200) - (50 * i));
            for (String player : killedByEntities.keys()){
                for(String type : killedByEntities.get(player)){
                    if(type.equals("grenade")){
                        sb.draw(getGrenadeTexture(player), cam.viewportWidth/5 + 200 + 10 * k, cam.viewportHeight-180 - 50 * i, 16, 16);
                        k++;
                    } else if (type.equals("bullet")){
                        sb.draw(getBulletTexture(player), cam.viewportWidth/5 + 200 + 10 * j, cam.viewportHeight-200 - 50 * i);
                        j++;
                    }
                }
            }
            i++;
        }
        sb.end();
        backbutton.render(sb);
    }

    @Override
    public void dispose() {

    }

    private Texture getPlayerTexture(String color) {
        if (color.equals("blue")) {
            return players[0];
        }
        if (color.equals("red")) {
            return players[1];
        } else {
            return null;
        }
    }

    private TextureRegion getGrenadeTexture(String color) {
        if (color.equals("blue")) {
            return grenades[0];
        }
        if (color.equals("red")) {
            return grenades[1];
        } else {
            return null;
        }
    }

    private Texture getBulletTexture(String color) {
        if (color.equals("blue")) {
            return bullets[0];
        }
        if (color.equals("red")) {
            return bullets[1];
        } else {
            return null;
        }
    }
}
