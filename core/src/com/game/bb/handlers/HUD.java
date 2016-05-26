package com.game.bb.handlers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.OrderedMap;
import com.game.bb.gamestates.PlayState;

import java.util.HashMap;

/**
 * Created by erik on 11/05/16.
 */
public class HUD {
    private TextureRegion[] font;
    private int playerDeaths = 1;
    private HashMap<Integer, Integer> opponentDeaths;
    private Texture playerTexture, opponentTexture, bulletTexture;
    private TextureRegion grenadeTexture;
    private int bulletsLeft = B2DVars.AMOUNT_BULLET;
    private int grenadesLeft = B2DVars.AMOUNT_GRENADE;
    private boolean gameOver = false;

    public HUD() {

        Texture hudTex = new Texture("images/hud.png");
        playerTexture = new Texture("images/player/bluePlayerStandRight.png");
        opponentTexture = new Texture("images/player/redPlayerStandRight.png");
        bulletTexture = new Texture("images/weapons/blueBullet.png");
        grenadeTexture = TextureRegion.split(new Texture("images/weapons/blueGrenade.png"), 30, 30)[0][0];
        opponentDeaths = new HashMap<Integer, Integer>();

        font = new TextureRegion[11];
        for (int i = 0; i < 6; i++) {
            font[i] = new TextureRegion(hudTex, 32 + i * 9, 16, 9, 9);
        }
        for (int i = 0; i < 5; i++) {
            font[i + 6] = new TextureRegion(hudTex, 32 + i * 9, 25, 9, 9);
        }
    }

    private void checkGameOver(){
        int amountPlayersAlive = opponentDeaths.size() + 1;
        for (Integer i : opponentDeaths.keySet()){
            if (opponentDeaths.get(i) == 0){
                amountPlayersAlive--;
            }
        }
        if (playerDeaths == 0){
            amountPlayersAlive--;
        }
        if (amountPlayersAlive <= 1 && opponentDeaths.size() > 0){
            gameOver=true;
        } else if (opponentDeaths.size() == 0 && amountPlayersAlive == 0){
            gameOver= true;
        }
    }

    public String getVictoryString(){
        return "NOT_YET_IMPLEMENTED";
    }

    public void addPlayerDeath() {
        playerDeaths--;
        checkGameOver();
    }

    public void setAmountBulletsLeft(int amount){
        bulletsLeft = amount;
    }

    public void setAmountGrenadesLeft(int amount) { grenadesLeft = amount;}

    public void setOpponentDeath(int id,int deaths) {
        opponentDeaths.put(id, deaths);
        checkGameOver();
    }

    public void removeOpponentDeathCount(int id){
        opponentDeaths.remove(id);
    }

    public void render(SpriteBatch sb) {
        float camHeight = PlayState.playState.cam.viewportHeight;
        float camWidth = PlayState.playState.cam.viewportWidth;
        sb.begin();
        sb.draw(playerTexture, 50, camHeight - 70, 45, 40);
        String playerDeathString = Integer.toString(playerDeaths);
        for (int i = 0; i < playerDeathString.length(); i++) {
            sb.draw(font[Integer.valueOf(playerDeathString.substring(i, i + 1))], 100 + i * 50, camHeight - 70, 40, 40);
        }
        int offset=0;
        for (int id : opponentDeaths.keySet()) {
            String deaths = Integer.toString(opponentDeaths.get(id));
            for (int i = 0; i < deaths.length(); i++) {
                sb.draw(opponentTexture, camWidth - 200-offset, camHeight - 70, 45, 40);
                sb.draw(font[Integer.valueOf(deaths.substring(i, i + 1))], camWidth - (150+offset) + i * 50, camHeight - 70, 40, 40);
            }
            offset+=200;
        }
        for (int i = 0; i < bulletsLeft; i++) {
            sb.draw(bulletTexture, 50 + i*50, camHeight-90, 25, 10);
        }
        for (int i = 0; i < grenadesLeft; i++) {
            sb.draw(grenadeTexture, 50 + i*50, camHeight-120, 20, 20);
        }
        sb.end();
    }

    public int getPlayerDeathCount() {
        return playerDeaths;
    }

    public boolean gameOver() {
        return gameOver;
    }
}
