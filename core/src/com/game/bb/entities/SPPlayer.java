package com.game.bb.entities;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by erik on 09/05/16.
 */
public class SPPlayer extends SPSprite {

    public SPPlayer(Body[] bodies) {
        super(bodies[0]);
    }

    public void movePaddle(float xForce, float yForce, float xPos, float yPos){
        body.setTransform(xPos, yPos, 0);
        body.setLinearVelocity(0, 0);
        body.applyForceToCenter(xForce, yForce, true);
    }


    @Override
    public void update(float dt){
    }
}
