package com.game.bb.handlers;

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

    public SPAnimation(TextureRegion[] textureRegions, float frequency) {
        this.textureRegions = textureRegions;
        this.frequency = frequency;
        time = 0;
        currentFrame = 0;
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
        return textureRegions[currentFrame];
    }

    @Override
    public void dispose() {
        for(int i = 0; i < textureRegions.length; i++){
            textureRegions[i].getTexture().dispose();
        }
    }
}
