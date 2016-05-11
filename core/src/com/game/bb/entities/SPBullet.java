package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by erik on 11/05/16.
 */
public class SPBullet extends SPSprite {

    public SPBullet(Body body, boolean harmful, float dir) {
        super(body);
        Sound sound = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.wav"));
        sound.play();
        if (harmful) {
            // set enemy color texture
            setTexture(new Texture("images/enemyBullet.png"));
        } else {
            // set friendly color texture
            setTexture(new Texture("images/friendlyBullet.png"));
        }
    }

    public float getXPos(){
        return body.getPosition().x;
    }
}
