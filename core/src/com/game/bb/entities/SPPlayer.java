package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 09/05/16.
 */
public class SPPlayer extends SPSprite {

    Sound sound;

    public SPPlayer(Body[] bodies)
    {
        super(bodies[0]);
        sound = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.wav"));
    }

    public void movePaddle(float xForce, float yForce, float xPos, float yPos){
        body.setTransform(xPos, yPos, 0);
        body.setLinearVelocity(0, 0);
        body.applyForceToCenter(xForce, yForce, true);
        sound.play();
    }

    @Override
    public void render(SpriteBatch sb){
        if(texture != null) {
            sb.begin();
            sb.draw(texture, body.getPosition().x * B2DVars.PPM,
                    body.getPosition().y * B2DVars.PPM, 40, 20);
            sb.end();
        }
    }


    @Override
    public void update(float dt){
    }
}
