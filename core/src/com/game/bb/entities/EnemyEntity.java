package com.game.bb.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.game.bb.handlers.SPAnimation;

/**
 * Created by erik on 21/05/16.
 */
public abstract class EnemyEntity implements Disposable, Pool.Poolable{

    protected Body body;
    protected int id;
    protected SPAnimation animation;
    protected float textureOffset = 0, textureWidth, textureHeight;

    public abstract void render(SpriteBatch sb);
    public abstract void update(float dt);

    public Body getBody(){
        return body;
    }

    public int getId(){
        return id;
    }

}
