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

    private int offset = 0;

    public SPBullet(Body body, boolean harmful) {
        super(body);
        Sound sound = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.wav"));
        sound.play();
        if (harmful) {
            // set enemy color texture
            setTexture(new Texture("images/redBullet.png"));
        } else {
            // set friendly color texture
            setTexture(new Texture("images/blueBullet.png"));
        }
    }

    public float getXPos(){
        return body.getPosition().x;
    }

    @Override
    public void render(SpriteBatch sb){
        if(texture != null) {
            sb.begin();
            float x =  body.getPosition().x * B2DVars.PPM - width / 2;
            float y = body.getPosition().y * B2DVars.PPM - height / 2;
            sb.draw(texture, x-offset, y-offset, 24, 7);
            sb.end();
        }
    }
}
