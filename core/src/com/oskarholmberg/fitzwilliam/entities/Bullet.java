package com.oskarholmberg.fitzwilliam.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.oskarholmberg.fitzwilliam.handlers.Assets;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;


public class Bullet extends Sprite {

    private int offset = 0;
    private float posYoffset = 5/ B2DVars.PPM, posXoffset = B2DVars.PLAYER_WIDTH+(20/ B2DVars.PPM);
    private PolygonShape shape;

    public Bullet(World world, float xPos, float yPos, float dir, boolean harmful, int ID) {
        super(world, ID);
        this.dir=dir;
        createBullet(xPos+dir* posXoffset, yPos-posYoffset, dir, harmful);
        Assets.getSound("lasershot").play();
        setTexture(Assets.getTex(B2DVars.MY_COLOR + "Bullet"));
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
        fdef.filter.categoryBits = B2DVars.BIT_BULLET;
        if (harmful) {
            fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_GROUND | B2DVars.BIT_DOME;
        } else {
            fdef.filter.maskBits =  B2DVars.BIT_GROUND | B2DVars.BIT_DOME;
        }
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.DynamicBody; // Should be dynamic
        body = world.createBody(bdef);
        body.setGravityScale(0f);
        body.createFixture(fdef).setUserData(B2DVars.ID_BULLET);
        body.setLinearVelocity(B2DVars.PH_BULLET_SPEED * dir / B2DVars.PPM, 0);
        body.setUserData(this);
        shape.dispose();
    }

    @Override
    public void dispose() {

    }
}
