package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.game.bb.handlers.B2DVars;

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

    @Override
    public void render(SpriteBatch sb){
        if(texture != null) {
            sb.begin();
            sb.draw(texture, body.getPosition().x * B2DVars.PPM - width / 2,
                    body.getPosition().y * B2DVars.PPM - height / 2, 16, 8);
            sb.end();
        }
    }
}
