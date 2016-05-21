package com.game.bb.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.SPAnimation;

/**
 * Created by erik on 21/05/16.
 */
public class EnemyBullet extends EnemyEntity {


    public EnemyBullet(){
        createBulletBody();
    }

    public void setId(int id){
        this.id = id;
    }

    public void setAnimation(String color){
        if (color.equals("red")){
            Texture bullet = new Texture("images/weapons/redBullet.png");
            textureWidth = bullet.getWidth();
            textureHeight = bullet.getHeight();
            animation = new SPAnimation(new Texture("images/weapons/redBullet.png"));
        }
        textureOffset = 0;
    }

    private void createBulletBody(){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(8 / B2DVars.PPM, 4 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_BULLET;
        fdef.filter.maskBits = B2DVars.BIT_PLAYER;
        BodyDef bdef = new BodyDef();
        bdef.position.set(B2DVars.VOID_X, B2DVars.VOID_Y);
        bdef.type = BodyDef.BodyType.DynamicBody; // Should be dynamic
        body = PlayState.playState.world.createBody(bdef);
        body.setGravityScale(0f);
        body.createFixture(fdef).setUserData(B2DVars.ID_BULLET);
        body.setUserData(this);
        shape.dispose();
    }

    @Override
    public void dispose() {
        animation.dispose();
    }

    @Override
    public void reset() {
        dispose();
        id=-1;
        body.setLinearVelocity(0,0);
        body.setTransform(B2DVars.VOID_X, B2DVars.VOID_Y, 0);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        float x =  body.getPosition().x * B2DVars.PPM - textureWidth / 2;
        float y = body.getPosition().y * B2DVars.PPM - textureHeight / 2;
        sb.draw(animation.getFrame(), x - textureOffset, y - textureOffset, 24, 7);
        sb.end();
    }

    @Override
    public void update(float dt) {
    }
}
