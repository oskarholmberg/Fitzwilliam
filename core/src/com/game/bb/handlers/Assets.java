package com.game.bb.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

/**
 * Created by erik on 27/05/16.
 */
public class Assets {
    private static HashMap<String, Texture> tex;
    private static HashMap<String, TextureRegion[]> texRegions;
    private static HashMap<String, TextureRegion> texRegion;
    private static HashMap<String, Sound> sounds;
    private static Texture background;

    public static Texture getTex(String key){
        return tex.get(key);
    }
    public static Texture getBackground(){
        return background;
    }
    public static TextureRegion[] getAnimation(String key){
        return texRegions.get(key);
    }
    public static Sound getSound(String key){
        return sounds.get(key);
    }


    public static void init(){
        tex = new HashMap<String, Texture>();
        texRegions = new HashMap<String, TextureRegion[]>();
        texRegion = new HashMap<String, TextureRegion>();
        sounds = new HashMap<String, Sound>();
        background = new Texture("images/spaceBackground.png");
        //textures
        //blue player
        tex.put("blueDeadLeft", new Texture("images/player/bluePlayerDeadLeft.png"));
        tex.put("blueDeadRight", new Texture("images/player/bluePlayerDeadRight.png"));
        tex.put("blueJumpLeft", new Texture("images/player/bluePlayerJumpLeft.png"));
        tex.put("blueJumpRight", new Texture("images/player/bluePlayerJumpRight.png"));
        tex.put("blueStandLeft", new Texture("images/player/bluePlayerStandLeft.png"));
        tex.put("blueStandRight", new Texture("images/player/bluePlayerStandRight.png"));
        tex.put("blueVictory", new Texture("images/player/bluePlayerVictory.png"));
        tex.put("blueHeart", new Texture("images/blueHeart.png"));
        tex.put("blueBlaster", new Texture("images/weapons/blueBlaster.png"));
        //red player
        tex.put("redDeadLeft", new Texture("images/player/redPlayerDeadLeft.png"));
        tex.put("redDeadRight", new Texture("images/player/redPlayerDeadRight.png"));
        tex.put("redJumpLeft", new Texture("images/player/redPlayerJumpLeft.png"));
        tex.put("redJumpRight", new Texture("images/player/redPlayerJumpRight.png"));
        tex.put("redStandLeft", new Texture("images/player/redPlayerStandLeft.png"));
        tex.put("redStandRight", new Texture("images/player/redPlayerStandRight.png"));
        tex.put("redVictory", new Texture("images/player/redPlayerVictory.png"));
        tex.put("redHeart", new Texture("images/redHeart.png"));
        tex.put("redBlaster", new Texture("images/weapons/redBlaster.png"));
        //yellow player
        tex.put("yellowDeadLeft", new Texture("images/player/yellowPlayerDeadLeft.png"));
        tex.put("yellowDeadRight", new Texture("images/player/yellowPlayerDeadRight.png"));
        tex.put("yellowJumpLeft", new Texture("images/player/yellowPlayerJumpLeft.png"));
        tex.put("yellowJumpRight", new Texture("images/player/yellowPlayerJumpRight.png"));
        tex.put("yellowStandLeft", new Texture("images/player/yellowPlayerStandLeft.png"));
        tex.put("yellowStandRight", new Texture("images/player/yellowPlayerStandRight.png"));
        tex.put("yellowVictory", new Texture("images/player/yellowPlayerVictory.png"));
        tex.put("yellowHeart", new Texture("images/yellowHeart.png"));
        tex.put("yellowBlaster", new Texture("images/weapons/yellowBlaster.png"));
        //green player
        tex.put("greenDeadLeft", new Texture("images/player/greenPlayerDeadLeft.png"));
        tex.put("greenDeadRight", new Texture("images/player/greenPlayerDeadRight.png"));
        tex.put("greenJumpLeft", new Texture("images/player/greenPlayerJumpLeft.png"));
        tex.put("greenJumpRight", new Texture("images/player/greenPlayerJumpRight.png"));
        tex.put("greenStandLeft", new Texture("images/player/greenPlayerStandLeft.png"));
        tex.put("greenStandRight", new Texture("images/player/greenPlayerStandRight.png"));
        tex.put("greenVictory", new Texture("images/player/greenPlayerVictory.png"));
        tex.put("greenHeart", new Texture("images/greenHeart.png"));
        tex.put("greenBlaster", new Texture("images/weapons/greenBlaster.png"));
        //ghost
        tex.put("ghostJumpLeft", new Texture("images/player/ghostJumpLeft.png"));
        tex.put("ghostJumpRight", new Texture("images/player/ghostJumpRight.png"));
        tex.put("ghostStandLeft", new Texture("images/player/ghostStandLeft.png"));
        tex.put("ghostStandRight", new Texture("images/player/ghostStandRight.png"));

        //bullets
        tex.put("blueBullet", new Texture("images/weapons/blueBullet.png"));
        tex.put("redBullet", new Texture("images/weapons/redBullet.png"));
        tex.put("yellowBullet", new Texture("images/weapons/yellowBullet.png"));
        tex.put("greenBullet", new Texture("images/weapons/greenBullet.png"));

        //misc
        tex.put("heart", new Texture("images/heart.png"));
        tex.put("blank", new Texture("images/blank.png"));

        //animations
        //grenades
        texRegions.put("blueGrenade", TextureRegion.split(new Texture("images/weapons/blueGrenade.png"), 30, 30)[0]);
        texRegions.put("redGrenade", TextureRegion.split(new Texture("images/weapons/redGrenade.png"), 30, 30)[0]);
        texRegions.put("greenGrenade", TextureRegion.split(new Texture("images/weapons/greenGrenade.png"), 30, 30)[0]);
        texRegions.put("yellowGrenade", TextureRegion.split(new Texture("images/weapons/yellowGrenade.png"), 30, 30)[0]);

        //power up boxes
        texRegions.put("shield", TextureRegion.split(new Texture("images/powerups/shield.png"), 28, 32)[0]);
        texRegions.put("unlimitedAmmo", TextureRegion.split(new Texture("images/powerups/unlimitedAmmoPower.png"), 17, 17)[0]);
        texRegions.put("shakeWorld", TextureRegion.split(new Texture("images/powerups/shakeWorldPower.png"), 17, 17)[0]);
        texRegions.put("ghost", TextureRegion.split(new Texture("images/powerups/ghostPower.png"), 17, 17)[0]);
        //shield
        texRegions.put("shieldPower", TextureRegion.split(new Texture("images/powerups/shieldPower.png"), 17, 17)[0]);

        //sounds
        sounds.put("lasershot", Gdx.audio.newSound(Gdx.files.internal("sfx/laser.wav")));
        sounds.put("reload", Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav")));
        sounds.put("emptyClip", Gdx.audio.newSound(Gdx.files.internal("sfx/emptyClip.wav")));
        sounds.put("grenade", Gdx.audio.newSound(Gdx.files.internal("sfx/grenade.wav")));
        sounds.put("menuSelect", Gdx.audio.newSound(Gdx.files.internal("sfx/levelselect.wav")));
        sounds.put("jetpack", Gdx.audio.newSound(Gdx.files.internal("sfx/jetpackFire.wav")));

    }

}
