package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 11/05/16.
 */
public class SPGrenade extends SPSprite {

    private int offset = 0, amountBounces = 0;
    private float posYoffset = 5/B2DVars.PPM, getPosXoffset = B2DVars.PLAYER_WIDTH+(20/B2DVars.PPM);
    private float dir;

    public SPGrenade(World world, float xPos, float yPos, float dir, String ID) {
        super(world, ID);
        this.dir=dir;
        createBullet(xPos+dir*getPosXoffset, yPos-posYoffset, dir);
        setTexture(new Texture("images/redBullet.png"));
    }

    public float getDir(){
        return dir;
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
    public boolean finishedBouncing(){
        System.out.println("I've bounced: " + amountBounces + " times");
        if (amountBounces > 5)
            return true;
        amountBounces++;
        return false;
    }

    private void createBullet(float xPos, float yPos, float dir){
        CircleShape shape = new CircleShape();
        shape.setRadius(8 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.restitution=1f;
        fdef.friction=0f;
        fdef.filter.categoryBits = B2DVars.BIT_GRENADE;
        fdef.filter.maskBits =  B2DVars.BIT_GROUND | B2DVars.BIT_PLAYER;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);
        body.setGravityScale(0f);
        body.createFixture(fdef).setUserData(B2DVars.ID_GRENADE);
        body.setLinearVelocity(B2DVars.PH_GRENADE_X * dir / B2DVars.PPM, B2DVars.PH_GRENADE_Y / B2DVars.PPM);
        body.setUserData(this);
    }
}
