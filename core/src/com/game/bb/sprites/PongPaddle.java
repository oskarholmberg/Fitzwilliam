package com.game.bb.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by erik on 09/05/16.
 */
public class PongPaddle extends PongSprite {

    public PongPaddle(Body[] bodies) {
        super(bodies[0]);
        setTexture(new Texture("playerShip.png"));

    }

    public void movePaddle(float xForce, float yForce, float xPos, float yPos){
        body.setTransform(xPos, yPos, 0);
        body.applyForceToCenter(xForce, yForce, true);
    }


    @Override
    public void update(float dt){
    }
}
