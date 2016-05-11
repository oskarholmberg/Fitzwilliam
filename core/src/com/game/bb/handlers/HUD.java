package com.game.bb.handlers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by erik on 11/05/16.
 */
public class HUD {
    private TextureRegion[] font;

    public HUD(){

        Texture hudTex = new Texture("images/hud.png");

        font = new TextureRegion[11];
        for(int i = 0; i < 6; i++) {
            font[i] = new TextureRegion(hudTex, 32 + i * 9, 16, 9, 9);
        }
        for(int i = 0; i < 5; i++) {
            font[i + 6] = new TextureRegion(hudTex, 32 + i * 9, 25, 9, 9);
        }
    }

    public void render(SpriteBatch sb, String playerDeaths, String opponentDeaths){
        sb.begin();
        for (int i = 0; i < playerDeaths.length(); i++){
            sb.draw(font[9], 50, B2DVars.CAM_HEIGHT - 20);
        }
        sb.end();
    }
}
