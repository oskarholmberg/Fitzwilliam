package com.game.bb.handlers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by erik on 11/05/16.
 */
public class HUD {
    private TextureRegion[] font;
    private String playerDeaths = "0", opponentDeaths = "0";
    private Texture playerTexture;

    public HUD(){

        Texture hudTex = new Texture("images/hud.png");

        playerTexture = new Texture("images/bluePlayerStandRight.png");

        font = new TextureRegion[11];
        for(int i = 0; i < 6; i++) {
            font[i] = new TextureRegion(hudTex, 32 + i * 9, 16, 9, 9);
        }
        for(int i = 0; i < 5; i++) {
            font[i + 6] = new TextureRegion(hudTex, 32 + i * 9, 25, 9, 9);
        }
    }

    public void addPlayerDeath(){
        int temp = Integer.valueOf(playerDeaths) + 1;
        playerDeaths = Integer.toString(temp);
    }

    public void render(SpriteBatch sb){
        sb.begin();
        for (int i = 0; i < playerDeaths.length(); i++){
            sb.draw(playerTexture, 50, B2DVars.CAM_HEIGHT - 70, 45, 40);
            sb.draw(font[Integer.valueOf(playerDeaths.substring(i, i+1))], 100 + i*50, B2DVars.CAM_HEIGHT - 70, 40, 40);
        }
        sb.end();
    }
}
