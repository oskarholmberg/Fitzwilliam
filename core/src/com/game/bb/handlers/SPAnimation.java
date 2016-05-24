package com.game.bb.handlers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by oskar on 5/15/16.
 */
public class SPAnimation implements Disposable{
    private TextureRegion[] textureRegions;
    private float frequency;
    private float time;
    private int currentFrame;
    private boolean noAnimation = false;

    public SPAnimation(TextureRegion[] textureRegions, float frequency) {
        this.textureRegions = textureRegions;
        this.frequency = frequency;
        time = 0;
        currentFrame = 0;
    }

    public SPAnimation(Texture texture){
        textureRegions = new TextureRegion[1];
        textureRegions[0] = new TextureRegion(texture);
        noAnimation=true;
    }

    public void update(float dt) {
        if (frequency <= 0) ;
        else {
            time += dt;
            while(time >= frequency){
                step();
            }
        }
    }

    private void step(){
        time -= frequency;
        currentFrame++;
        if(currentFrame == textureRegions.length){
            currentFrame=0;
        }
    }

    public TextureRegion getFrame(){
        if (noAnimation)
            return textureRegions[0];
        else
            return textureRegions[currentFrame];
    }

    public TextureRegion getOpponentPlayerFrame(int textureIndex){
        return textureRegions[textureIndex];
    }

    @Override
    public void dispose() {
        for(int i = 0; i < textureRegions.length; i++){
            textureRegions[i].getTexture().dispose();
        }
    }
}
