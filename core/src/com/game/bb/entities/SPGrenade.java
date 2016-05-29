package com.game.bb.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.SPAnimation;

/**
 * Created by erik on 11/05/16.
 */
public class SPGrenade extends SPSprite {

    private int offset = 15;
    private float posYoffset = 5 / B2DVars.PPM, getPosXoffset = B2DVars.PLAYER_WIDTH + (20 / B2DVars.PPM);
    private SPAnimation animation;
    private boolean opponentGrenade;
    private CircleShape shape;
    private float lifetime = 0f;

    public SPGrenade(World world, float xPos, float yPos, float dir, boolean opponentGrenade, int ID) {
        super(world, ID);
        this.dir = dir;
        this.opponentGrenade=opponentGrenade;
        createGrenadeBody(xPos + dir * getPosXoffset, yPos - posYoffset, dir);
        Assets.getSound("grenade").play();
        animation = new SPAnimation(Assets.getAnimation(B2DVars.MY_COLOR + "Grenade"), 0.2f);

    }

    public float getDir() {
        return dir;
    }

    @Override
    public void update(float dt) {
        animation.update(dt);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        float x = body.getPosition().x * B2DVars.PPM - width / 2;
        float y = body.getPosition().y * B2DVars.PPM - height / 2;
        sb.draw(animation.getFrame(), x - offset, y - offset);
        sb.end();
    }

    public boolean lifeTimeReached(float dt) {
        lifetime+=dt;
        if (lifetime > 5f) {
            return true;
        }
        return false;
    }

    private void createGrenadeBody(float xPos, float yPos, float dir) {
        shape = new CircleShape();
        shape.setRadius(15 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.restitution = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = B2DVars.BIT_GRENADE;
        fdef.filter.maskBits = B2DVars.BIT_GROUND | B2DVars.BIT_PLAYER | B2DVars.BIT_DOME;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);
        body.setGravityScale(0f);
        if (opponentGrenade)
            body.createFixture(fdef).setUserData(B2DVars.ID_ENEMY_GRENADE);
        else
            body.createFixture(fdef).setUserData(B2DVars.ID_GRENADE);
        if (!opponentGrenade)
            body.setLinearVelocity(B2DVars.PH_GRENADE_X * dir / B2DVars.PPM, B2DVars.PH_GRENADE_Y / B2DVars.PPM);
        body.setUserData(this);
    }

    @Override
    public void dispose() {
        shape.dispose();
    }
}
