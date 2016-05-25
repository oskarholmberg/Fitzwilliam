package com.game.bb.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 09/05/16.
 */
public abstract class SPSprite implements Disposable {
    protected World world;
    protected Body body;
    protected float height, width;
    protected Texture texture;
    protected int ID;
    protected float dir;
    // maybe add animation, check the tutorial
    // protected SPAnimation animation;

    public SPSprite(World world, int ID){
        this.ID=ID;
        this.world=world;
    }

    public float getDirection(){
        return dir;
    }

    public void setTexture(Texture texture){
        this.texture = texture;
        width = texture.getWidth();
        height = texture.getHeight();
    }

    public void update(float dt){

    }

    public int getId(){return ID;}

    public void render(SpriteBatch sb){

    }

    public Body getBody(){
        return body;
    }
    public Vector2 getPosition(){ return body.getPosition(); }
    public float getWidth(){ return width; }
    public float getHeight(){ return height; }
}
