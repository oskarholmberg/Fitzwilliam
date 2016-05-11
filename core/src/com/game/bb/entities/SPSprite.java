package com.game.bb.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 09/05/16.
 */
public class SPSprite {
    protected Body body;
    protected float height, width;
    protected Texture texture;
    // maybe add animation, check the tutorial
    // protected Animation animation;

    public SPSprite(Body body){
        this.body=body;
    }

    public void setTexture(Texture texture){
        this.texture = texture;
        width = texture.getWidth();
        height = texture.getHeight();
    }

    public void update(float dt){

    }

    public void render(SpriteBatch sb){
        if(texture != null) {
            sb.begin();
            sb.draw(texture, body.getPosition().x * B2DVars.PPM - width / 2, body.getPosition().y * B2DVars.PPM - height / 2);
            sb.end();
        }
    }

    public Body getBody(){
        return body;
    }
    public Vector2 getPosition(){ return body.getPosition(); }
    public float getWidth(){ return width; }
    public float getHeight(){ return height; }
}
