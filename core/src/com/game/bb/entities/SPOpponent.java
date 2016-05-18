package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.B2DVars;

/**
 * Created by oskar on 5/18/16.
 */
public class SPOpponent extends SPPlayer {

    private Sound sound;
    private Texture[] textures;
    private int xOffset = 23, yOffset = 25;
    private PolygonShape shape;

    public SPOpponent(World world, float xPos, float yPos, int ID) {
        super(world, xPos, yPos, ID);
        createOpponentBody(xPos, yPos);
        dir = 0;
        sound = Gdx.audio.newSound(Gdx.files.internal("sfx/jetpackFire.wav"));
        loadTexture("red");
        setTexture(textures[STAND_RIGHT]);
    }

    /**
     * Moves the opponent to position xPos, yPos, with velocities xV, yV
     * sets texture according to texture and plays jump sound if jump == 1.
     * @param xPos, x position to move to.
     * @param yPos, y position to move to.
     * @param xV, velocity in horizontal direction.
     * @param yV, velocity in vertical direction.
     * @param texture, sets the texture according to value.
     * @param jump, if 1 will play jump sound, else nothing happens.
     */
    public void move(float xPos, float yPos, float xV, float yV, int texture, int jump) {
        body.setTransform(xPos, yPos, 0);
        body.setLinearVelocity(xV, yV);
        setTexture(textures[texture]);
        if(jump == 1){
            sound.play();
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (texture != null) {
            sb.begin();
            float x = body.getPosition().x * B2DVars.PPM - B2DVars.PLAYER_WIDTH;
            float y = body.getPosition().y * B2DVars.PPM - B2DVars.PLAYER_HEIGHT;
            sb.draw(texture, x - xOffset, y - yOffset, 54, 48);
            sb.end();
        }
    }

    private void createOpponentBody(float xPos, float yPos) {
        shape = new PolygonShape();
        shape.setAsBox(B2DVars.PLAYER_WIDTH, B2DVars.PLAYER_HEIGHT);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_OPPONENT;
        fdef.filter.maskBits = B2DVars.BIT_GROUND;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);
        body.createFixture(fdef).setUserData(B2DVars.ID_OPPONENT);
        body.setUserData(this);
    }
}
