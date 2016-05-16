package com.game.bb.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.SPAnimation;

/**
 * Created by erik on 11/05/16.
 */
public class SPGrenade extends SPSprite {

    private int offset = 15, amountBounces = 0;
    private float posYoffset = 5 / B2DVars.PPM, getPosXoffset = B2DVars.PLAYER_WIDTH + (20 / B2DVars.PPM);
    private SPAnimation animation;
    private TextureRegion[] regions;
    private Texture grenade;
    private boolean opponentGrenade;

    public SPGrenade(World world, float xPos, float yPos, float dir, boolean opponentGrenade, String ID) {
        super(world, ID);
        this.dir = dir;
        this.opponentGrenade=opponentGrenade;
        createGrenadeBody(xPos + dir * getPosXoffset, yPos - posYoffset, dir);
        if(opponentGrenade){
            grenade = new Texture("images/weapons/redGrenade.png");
        }
        else{
            grenade = new Texture("images/weapons/blueGrenade.png");
        }
        regions = new TextureRegion[4];
        for (int i = 0; i < regions.length; i++) {
            regions[i] = new TextureRegion(grenade, i * 30, 0, 30, 30);
        }
        animation = new SPAnimation(regions, 0.2f);
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

    public boolean finishedBouncing() {
        if (amountBounces > 5)
            return true;
        amountBounces++;
        return false;
    }

    private void createGrenadeBody(float xPos, float yPos, float dir) {
        CircleShape shape = new CircleShape();
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
        body.setLinearVelocity(B2DVars.PH_GRENADE_X * dir / B2DVars.PPM, B2DVars.PH_GRENADE_Y / B2DVars.PPM);
        body.setUserData(this);
    }
}
