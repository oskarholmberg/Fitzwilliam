package com.oskarholmberg.fitzwilliam.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.oskarholmberg.fitzwilliam.handlers.Assets;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;
import com.oskarholmberg.fitzwilliam.handlers.SPAnimation;
import com.oskarholmberg.fitzwilliam.gamestates.PlayState;
import com.oskarholmberg.fitzwilliam.net.packets.PlayerMovementPacket;

/**
 * Created by erik on 24/05/16.
 */
public class EnemyPlayer extends com.oskarholmberg.fitzwilliam.entities.EnemyEntity {
    public static int STAND_RIGHT = 0, STAND_LEFT = 1, JUMP_RIGHT = 2, JUMP_LEFT = 3,
            DEAD_RIGHT = 4, DEAD_LEFT = 5;
    private int currentTexture;
    protected int xOffset = 23, yOffset = 25;
    private Sound jetPackSound;


    public EnemyPlayer(float xPos, float yPos){
        super();
        createPlayerBody(xPos, yPos);
        jetPackSound = Assets.getSound("jetpack");
    }



    @Override
    public void setAnimation(String color) {
        Texture[] textures = new Texture[6];
        if (color.equals("red")){
            textures[STAND_RIGHT] = new Texture("images/player/redPlayerStandRight.png");
            textures[STAND_LEFT] = new Texture("images/player/redPlayerStandLeft.png");
            textures[JUMP_RIGHT] = new Texture("images/player/redPlayerJumpRight.png");
            textures[JUMP_LEFT] = new Texture("images/player/redPlayerJumpLeft.png");
            textures[DEAD_RIGHT] = new Texture("images/player/redPlayerDeadRight.png");
            textures[DEAD_LEFT] = new Texture("images/player/redPlayerDeadLeft.png");
        }
        TextureRegion[] texRegions = new TextureRegion[6];
        for (int i = 0; i < textures.length; i++){
            texRegions[i] = new TextureRegion(textures[i]);
        }
        animation = new SPAnimation(texRegions, 0.2f);
    }

    public void moveOpponent(PlayerMovementPacket pkt){
        body.setTransform(interpolator.getPlayerPosition(pkt), 0);
        currentTexture=pkt.tex;
        if (pkt.sound == 1){
            jetPackSound.play();
        }
    }

    private void createPlayerBody(float xPos, float yPos){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(B2DVars.PLAYER_WIDTH, B2DVars.PLAYER_HEIGHT);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_OPPONENT;
        fdef.filter.maskBits = B2DVars.BIT_GROUND;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = PlayState.playState.world.createBody(bdef);
        body.createFixture(fdef).setUserData(B2DVars.ID_OPPONENT);
        body.setUserData(this);
        shape.dispose();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        float x = body.getPosition().x * B2DVars.PPM - B2DVars.PLAYER_WIDTH;
        float y = body.getPosition().y * B2DVars.PPM - B2DVars.PLAYER_HEIGHT;
        sb.draw(animation.getOpponentPlayerFrame(currentTexture), x - xOffset, y - yOffset, 54, 48);
        sb.end();
    }

    @Override
    public void update(float dt) {
        animation.update(dt);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void reset() {
        dispose();
        id = -1;
        body.setLinearVelocity(0, 0);
        body.setTransform(B2DVars.VOID_X, B2DVars.VOID_Y, 0);
    }
}
