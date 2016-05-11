package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 09/05/16.
 */
public class SPPlayer extends SPSprite {

    private Sound sound;
    private Texture onGroundRight, inAirRight, onGroundLeft, inAirLeft;
    private boolean onGround = true;
    private float textureTimer = 0;
    private int offset = 2;

    public SPPlayer(Body body)
    {
        super(body);
        sound = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.wav"));
        onGroundRight = new Texture("images/bluePlayerStandRight.png");
        inAirRight = new Texture("images/bluePlayerJumpRight.png");
        onGroundLeft = new Texture("images/bluePlayerStandLeft.png");
        inAirLeft = new Texture("images/bluePlayerJumpLeft.png");
        setTexture(onGroundRight);
    }

    public void jump(float xForce, float yForce, float xPos, float yPos){
        body.setTransform(xPos, yPos, 0);
        body.setLinearVelocity(0, 0);
        body.applyForceToCenter(xForce, yForce, true);
        textureTimer = 0;
        onGround = false;
        if(xForce<0){
            setTexture(inAirLeft);
        }else {
            setTexture(inAirRight);
        }
        sound.play();
    }

    @Override
    public void render(SpriteBatch sb){
        if(texture != null) {
            sb.begin();
            float x = body.getPosition().x * B2DVars.PPM - B2DVars.PLAYER_WIDTH;
            float y = body.getPosition().y * B2DVars.PPM - B2DVars.PLAYER_HEIGHT;
            if(textureTimer>=0.5f && !onGround){
                if(texture.equals(inAirLeft)){
                    setTexture(onGroundLeft);
                    offset = 12;
                }
                else{
                    setTexture(onGroundRight);
                    offset = 2;
                }
                onGround=true;
            }
            sb.draw(texture, x-offset, y, 54, 48);
            sb.end();
        }
    }

    @Override
    public void update(float dt){
        textureTimer+=dt;
    }
}
