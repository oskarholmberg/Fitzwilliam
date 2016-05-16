package com.game.bb.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.SPAnimation;

/**
 * Created by erik on 15/05/16.
 */
public class SPPower extends SPSprite{
    private SPAnimation animation;
    private int offset = 32;
    public SPPower(World world, float xPos, float yPos, String ID) {
        super(world, ID);
        createPowerBody(xPos, yPos);

        animation = new SPAnimation(TextureRegion.split(new Texture("images/powerUpBox.png"), 32, 32)[0], 0.2f);
    }

    private void createPowerBody(float xPos, float yPos){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(32 / B2DVars.PPM, 32 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_POWERUP;
        fdef.filter.maskBits =  B2DVars.BIT_PLAYER | B2DVars.BIT_GROUND;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.gravityScale=0.2f;
        bdef.type = BodyDef.BodyType.DynamicBody; // Should be dynamic
        body = world.createBody(bdef);
        body.createFixture(fdef).setUserData(B2DVars.ID_POWERUP);
        body.setUserData(this);
    }

    @Override
    public void update(float dt){
        animation.update(dt);
    }

    @Override
    public void render(SpriteBatch sb){
        float x = body.getPosition().x * B2DVars.PPM - width / 2;
        float y = body.getPosition().y * B2DVars.PPM - height / 2;
        sb.begin();
        sb.draw(animation.getFrame(), x - offset, y - offset);
        sb.end();
    }
}
