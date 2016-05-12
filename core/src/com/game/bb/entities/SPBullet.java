package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 11/05/16.
 */
public class SPBullet extends SPSprite {

    private int offset = 0;

    public SPBullet(World world, float xPos, float yPos, float dir, boolean harmful) {
        super(world);
        createBullet(xPos, yPos, dir, harmful);
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

    private void createBullet(float xPos, float yPos, float dir, boolean harmful){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8 / B2DVars.PPM, 4 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        if (harmful) {
            fdef.filter.categoryBits = B2DVars.BIT_BULLET;
            fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_GROUND;
        }
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.KinematicBody;
        body = world.createBody(bdef);
        body.createFixture(fdef).setUserData(B2DVars.ID_BULLET);
        body.setLinearVelocity(200f * dir / B2DVars.PPM, 0);
    }
}
