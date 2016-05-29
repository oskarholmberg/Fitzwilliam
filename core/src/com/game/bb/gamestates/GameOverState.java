package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.GameStateManager;

/**
 * Created by erik on 12/05/16.
 */
public class GameOverState extends GameState {

    private SPButton backbutton;
    private World world;
    private Texture background = new Texture("images/spaceBackground.png");
    private Texture gameOver = new Texture("images/font/gameOver.png");
    private Texture killedBy = new Texture("images/font/killedBy.png");
    private Texture[] placings;
    private ArrayMap<String, Array<String>> killedByEntities;
    private ArrayMap<String, Integer> colorGrenades;
    private ArrayMap<String, Integer> colorBullets;
    private String[] victoryOrder;


    public GameOverState(GameStateManager gsm, ArrayMap<String, Array<String>> killedByEntities, String victoryOrder) {
        super(gsm);
        this.killedByEntities = killedByEntities;
        System.out.println(killedByEntities);
        this.victoryOrder = victoryOrder.split(":");
        world = new World(new Vector2(0, -9.81f), true);
        backbutton = new SPButton(new Texture("images/button/backButton.png"), cam.viewportWidth - 100,
                cam.viewportHeight - 100, 40f, 40f, cam);
        placings = new Texture[3];
        placings[0] = new Texture("images/font/golden1.png");
        placings[1] = new Texture("images/font/silver2.png");
        placings[2] = new Texture("images/font/bronze3.png");
        splitKillingEntities();
    }

    private void splitKillingEntities() {
        colorBullets = new ArrayMap<String, Integer>();
        colorGrenades = new ArrayMap<String, Integer>();
        for (String color : killedByEntities.keys()) {
            colorBullets.put(color, 0);
            colorGrenades.put(color, 0);
        }
        for (String color : killedByEntities.keys()) {
            for (int i = 0; i < killedByEntities.get(color).size; i++) {
                if (killedByEntities.get(color).get(i).equals("grenade")) {
                    colorGrenades.put(color, colorGrenades.get(color) + 1);
                } else if (killedByEntities.get(color).get(i).equals("bullet")) {
                    colorBullets.put(color, colorBullets.get(color) + 1);
                }
            }
        }
        System.out.println("colorgrenades" + colorGrenades + "colorbullets" + colorBullets);
    }

    @Override
    public void handleInput() {
        if (backbutton.isClicked()) {
            Assets.getSound("menuSelect").play();
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
        //draw victory order
        for (int i = 0; i < victoryOrder.length; i++) {
            sb.draw(placings[i], cam.viewportWidth / 5 + 200 * i, cam.viewportHeight - 230, 38, 48);
            if (i == 0) {
                sb.draw(Assets.getTex(victoryOrder[i] + "Victory"), cam.viewportWidth / 5 + 60 + 200 * i,
                        cam.viewportHeight - 230, 48, 48);
            } else {
                sb.draw(Assets.getTex(victoryOrder[i] + "StandRight"), cam.viewportWidth / 5 + 60 + 200 * i,
                        cam.viewportHeight - 230, 48, 48);
            }
        }
        int i = 0;
        sb.draw(killedBy, cam.viewportWidth / 4, cam.viewportHeight - 300, 350, 24);
        for (String color : killedByEntities.keys()) {
            sb.draw(Assets.getTex(color + "StandRight"), cam.viewportWidth / 5 + 50, cam.viewportHeight - 350 - 70 * i,
                    48, 48);
            for (int j = 0; j < colorBullets.get(color); j++) {
                sb.draw(Assets.getTex(color + "Bullet"), cam.viewportWidth / 5 + 110 + j * 30,
                        cam.viewportHeight - 315 - 70 * i);
            }
            for (int j = 0; j < colorGrenades.get(color); j++) {
                sb.draw(Assets.getAnimation(color + "Grenade")[0], cam.viewportWidth / 5 + 110 + j * 30,
                        cam.viewportHeight - 350 - 70 * i, 25, 25);
            }
            i++;
        }
        sb.end();
        backbutton.render(sb);
    }

    @Override
    public void dispose() {

    }
}
