package com.game.bb.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.game.bb.handlers.EntityInterpolator;
import com.game.bb.handlers.SPAnimation;
import com.game.bb.net.packets.EntityPacket;

/**
 * Created by erik on 21/05/16.
 */
public abstract class EnemyEntity implements Disposable, Pool.Poolable{

    protected Body body;
    protected int id;
    protected SPAnimation animation;
    protected float textureOffset = 0, textureWidth, textureHeight;
    protected EntityInterpolator interpolator;

    public EnemyEntity(){
        interpolator = new EntityInterpolator(this);
    }

    public void applyInterpolation(EntityPacket pkt){
        body.setTransform(interpolator.getTargetVelocity(pkt), 0);
    }

    public void initInterpolator(){
        interpolator.init();
    }

    public abstract void render(SpriteBatch sb);
    public abstract void update(float dt);

    public Body getBody(){
        return body;
    }

    public int getId(){
        return id;
    }

}
