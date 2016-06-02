package com.game.bb.handlers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.IntMap;
import com.game.bb.gamestates.PlayState;

import java.util.HashMap;

/**
 * Created by erik on 11/05/16.
 */
public class HUD {
    private TextureRegion[] font;
    private int playerDeaths = B2DVars.AMOUNT_LIVES, playerKills = 0;
    private HashMap<Integer, Integer> opponentDeaths;
    private IntMap<String> idsToColors;
    private Texture bulletTexture, heart, blaster;
    private TextureRegion grenadeTexture;
    private Array<Integer> victoryOrder;
    private ArrayMap<String, Texture> textureFromColor;
    private int bulletsLeft = B2DVars.AMOUNT_BULLET;
    private int grenadesLeft = B2DVars.AMOUNT_GRENADE;
    private boolean gameOver = false;

    public HUD() {

        Texture hudTex = new Texture("images/hud.png");
        textureFromColor = new ArrayMap<String, Texture>();
        textureFromColor.put(B2DVars.COLOR_BLUE, Assets.getTex("blueStandRight"));
        textureFromColor.put(B2DVars.COLOR_RED, Assets.getTex("redStandRight"));
        textureFromColor.put(B2DVars.COLOR_YELLOW, Assets.getTex("yellowStandRight"));
        textureFromColor.put(B2DVars.COLOR_GREEN, Assets.getTex("greenStandRight"));
        heart = Assets.getTex(B2DVars.MY_COLOR + "Heart");
        blaster = Assets.getTex(B2DVars.MY_COLOR + "Blaster");
        bulletTexture = Assets.getTex(B2DVars.MY_COLOR + "Bullet");
        grenadeTexture = Assets.getAnimation(B2DVars.MY_COLOR + "Grenade")[0];
        opponentDeaths = new HashMap<Integer, Integer>();
        victoryOrder = new Array<Integer>();
        idsToColors = new IntMap<String>();

        font = new TextureRegion[11];
        for (int i = 0; i < 6; i++) {
            font[i] = new TextureRegion(hudTex, 32 + i * 9, 16, 9, 9);
        }
        for (int i = 0; i < 5; i++) {
            font[i + 6] = new TextureRegion(hudTex, 32 + i * 9, 25, 9, 9);
        }
    }

    public void setMyNewColor(String color){
        bulletTexture = Assets.getTex(color + "Bullet");
        grenadeTexture = Assets.getAnimation(color + "Grenade")[0];
        heart = Assets.getTex(color + "Heart");
        blaster = Assets.getTex(color + "Blaster");
    }

    public void setColorToId(int id, String color){
        idsToColors.put(id, color);
    }

    private void checkGameOver(){
        int amountPlayersAlive = opponentDeaths.size() + 1;
        for (Integer id : opponentDeaths.keySet()){
            if (opponentDeaths.get(id) == 0){
                amountPlayersAlive--;
                if (!victoryOrder.contains(id, true)){
                    victoryOrder.add(id);
                }
            }
        }
        if (playerDeaths == 0){
            amountPlayersAlive--;
            if (!victoryOrder.contains(B2DVars.MY_ID, true)){
                victoryOrder.add(B2DVars.MY_ID);
            }
        }
        if (amountPlayersAlive <= 1 && opponentDeaths.size() > 0){
            if (!victoryOrder.contains(B2DVars.MY_ID, true)){
                victoryOrder.add(B2DVars.MY_ID);
            }
            for (Integer id : opponentDeaths.keySet()){
                if (!victoryOrder.contains(id, true)){
                    victoryOrder.add(id);
                }
            }
            gameOver=true;
        } else if (opponentDeaths.size() == 0 && amountPlayersAlive == 0){
            if (!victoryOrder.contains(B2DVars.MY_ID, true)){
                victoryOrder.add(B2DVars.MY_ID);
            }
            for (Integer id : opponentDeaths.keySet()){
                if (!victoryOrder.contains(id, true)){
                    victoryOrder.add(id);
                }
            }
            gameOver= true;
        }
    }

    public Array<Integer> getVictoryOrder(){

        return victoryOrder;
    }

    public void addPlayerDeath() {
        playerDeaths--;
        checkGameOver();
    }

    public void addPlayerKill() {
        playerKills++;
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
        sb.draw(heart, 43, camHeight - 70, 40, 40);
        String playerDeathString = Integer.toString(playerDeaths);
        int j = 0;
        for (int i = 0; i < playerDeathString.length(); i++) {
            sb.draw(font[Integer.valueOf(playerDeathString.substring(i, i + 1))], 100 + i * 50, camHeight - 70, 40, 40);
            j++;
        }
        sb.draw(blaster, 140 + 50*j, camHeight-55, 40, 13);
        String playerKillString = Integer.toString(playerKills);
        for (int i = 0; i < playerKillString.length(); i++) {
            sb.draw(font[Integer.valueOf(playerKillString.substring(i, i + 1))], 200 + j * 50 + i * 50,
                    camHeight - 70, 40, 40);
        }
        int offset=0;
        for (int id : opponentDeaths.keySet()) {
            String deaths = Integer.toString(opponentDeaths.get(id));
            for (int i = 0; i < deaths.length(); i++) {
                sb.draw(textureFromColor.get(idsToColors.get(id)), camWidth - 200-offset, camHeight - 70, 45, 40);
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
