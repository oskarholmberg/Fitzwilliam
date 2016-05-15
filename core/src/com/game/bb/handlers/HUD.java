package com.game.bb.handlers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;

/**
 * Created by erik on 11/05/16.
 */
public class HUD {
    private TextureRegion[] font;
    private String playerDeaths = "0";
    private HashMap<String, String> opponentDeaths;
    private Texture playerTexture, opponentTexture, bulletTexture;
    private int bulletsLeft = 3;

    public HUD() {

        Texture hudTex = new Texture("images/hud.png");
        playerTexture = new Texture("images/player/bluePlayerStandRight.png");
        opponentTexture = new Texture("images/player/redPlayerStandRight.png");
        bulletTexture = new Texture("images/weapons/blueBullet.png");
        opponentDeaths = new HashMap<String, String>();

        font = new TextureRegion[11];
        for (int i = 0; i < 6; i++) {
            font[i] = new TextureRegion(hudTex, 32 + i * 9, 16, 9, 9);
        }
        for (int i = 0; i < 5; i++) {
            font[i + 6] = new TextureRegion(hudTex, 32 + i * 9, 25, 9, 9);
        }
    }

    public void addPlayerDeath() {
        int temp = Integer.valueOf(playerDeaths) + 1;
        playerDeaths = Integer.toString(temp);
    }

    public void setAmountBulletsLeft(int amount){
        bulletsLeft = amount;
    }

    public void setOpponentDeath(String id,String deaths) {
        opponentDeaths.put(id, deaths);
    }

    public void removeOpponentDeathCount(String id){
        opponentDeaths.remove(id);
    }

    public void render(SpriteBatch sb) {
        sb.begin();
        sb.draw(playerTexture, 50, B2DVars.CAM_HEIGHT - 70, 45, 40);
        for (int i = 0; i < playerDeaths.length(); i++) {
            sb.draw(font[Integer.valueOf(playerDeaths.substring(i, i + 1))], 100 + i * 50, B2DVars.CAM_HEIGHT - 70, 40, 40);
        }
        int offset=0;
        for (String id : opponentDeaths.keySet()) {
            String deaths = opponentDeaths.get(id);
            for (int i = 0; i < deaths.length(); i++) {
                sb.draw(opponentTexture, B2DVars.CAM_WIDTH - 200-offset, B2DVars.CAM_HEIGHT - 70, 45, 40);
                sb.draw(font[Integer.valueOf(deaths.substring(i, i + 1))], B2DVars.CAM_WIDTH - (150+offset) + i * 50, B2DVars.CAM_HEIGHT - 70, 40, 40);
            }
            offset+=200;
        }
        for (int i = 0; i < bulletsLeft; i++) {
            sb.draw(bulletTexture, 50 + i*50, B2DVars.CAM_HEIGHT-120, 25, 15);
        }
        sb.end();
    }

    public String getDeathCount() {
        return playerDeaths;
    }
}
