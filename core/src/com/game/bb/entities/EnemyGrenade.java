package com.game.bb.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.SPAnimation;


public class EnemyGrenade extends EnemyEntity{

    public EnemyGrenade(){
        super();
        createGrenadeBody();
    }

    public void setAnimation(String color){
        animation = new SPAnimation(Assets.getAnimation(color + "Grenade"), 0.2f);
        textureOffset = 15;
    }

    private void createGrenadeBody(){
        CircleShape shape = new CircleShape();
        shape.setRadius(15 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_GRENADE;
        fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_GROUND;
        fdef.restitution = 1f;
        fdef.friction = 0f;
        BodyDef bdef = new BodyDef();
        bdef.position.set(B2DVars.VOID_X, B2DVars.VOID_Y);
        bdef.type = BodyDef.BodyType.DynamicBody; // Should be dynamic
        body = PlayState.playState.world.createBody(bdef);
        body.setGravityScale(0f);
        body.createFixture(fdef).setUserData(B2DVars.ID_ENEMY_ENTITY);
        body.setUserData(this);
        shape.dispose();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reset() {
        id=-1;
        body.setLinearVelocity(0,0);
        body.setTransform(B2DVars.VOID_X, B2DVars.VOID_Y, 0);
        System.out.println("Grenade removed.");
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        float x = body.getPosition().x * B2DVars.PPM;
        float y = body.getPosition().y * B2DVars.PPM;
        sb.draw(animation.getFrame(), x - textureOffset, y - textureOffset);
        sb.end();
    }

    @Override
    public void update(float dt) {
        animation.update(dt);
    }
}
