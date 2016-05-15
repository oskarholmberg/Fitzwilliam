package com.game.bb.entities;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 15/05/16.
 */
public class SPPower extends SPSprite{
    public SPPower(World world, String ID) {
        super(world, ID);
    }

    private void createBody(float xPos, float yPos){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8 / B2DVars.PPM, 4 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_GRENADE;
        fdef.filter.maskBits = B2DVars.BIT_OPPONENT | B2DVars.BIT_PLAYER;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.DynamicBody; // Should be dynamic
        body = world.createBody(bdef);
        body.setGravityScale(0f);
        body.createFixture(fdef).setUserData(B2DVars.ID_BULLET);
        body.setUserData(this);
    }
}
