package com.game.bb.sprites;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 10/05/16.
 */
public class PongBall extends PongSprite {
    public PongBall(Body body) {
        super(body);
    }


    public void bounceBall(float xDirection, float yDirection){
        Vector2 currentPath = body.getLinearVelocity();
        body.setLinearVelocity(currentPath.x*xDirection, currentPath.y*yDirection);
        System.out.println(currentPath.x + " : "  + currentPath.y);
    }

    public void checkAngle(){
        Vector2 currentPath = body.getLinearVelocity();
        System.out.println("Ball x-angle: " + currentPath.x);
        if (  currentPath.x >= 0 && currentPath.x <  50f / B2DVars.PPM){
            body.setLinearVelocity(100f / B2DVars.PPM, currentPath.y);
        } else if ( currentPath.x <= 0 && currentPath.x > -50f / B2DVars.PPM ){
            body.setLinearVelocity(-100f / B2DVars.PPM, currentPath.y);
        }
    }
}
