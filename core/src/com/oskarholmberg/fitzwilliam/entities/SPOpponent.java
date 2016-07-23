package com.oskarholmberg.fitzwilliam.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.oskarholmberg.fitzwilliam.handlers.Assets;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;
import com.oskarholmberg.fitzwilliam.handlers.SPAnimation;

/**
 * Created by oskar on 5/18/16.
 */
public class SPOpponent extends com.oskarholmberg.fitzwilliam.entities.SPPlayer {
    private SPAnimation shield;
    private boolean shielded = false, lost = false;
    private int lastMovementSeq = 0;
    private float invulnerableTimer = 0, invulnerableTime = 0;

    public SPOpponent(World world, float xPos, float yPos, int id, String color) {
        super(world, xPos, yPos, id, color);
        shield = new SPAnimation(Assets.getAnimation("shield"), 0.2f);
    }

    public void applyShield(){
        shielded = true;
    }

    public void removeShield(){
        shielded = false;
    }

    public void setInvulnerable(float time){
        invulnerableTimer = time + B2DVars.RESPAWN_TIME;
        invulnerableTime = time;
    }

    /**
     * Moves the opponent to position xPos, yPos, with velocities xV, yV
     * sets texture according to texture and plays sound jetpackSound if sound == 1.
     * @param xPos, x position to move to.
     * @param yPos, y position to move to.
     * @param xV, velocity in horizontal direction.
     * @param yV, velocity in vertical direction.
     * @param texture, sets the texture according to value.
     * @param sound, if sound == 1, jetpackSound will play. Else nothing happens.
     */
    public void move(float xPos, float yPos, float xV, float yV, int texture, int sound, int seq) {
        if (seq > lastMovementSeq) {
            lastMovementSeq = seq;
            body.setTransform(xPos, yPos, 0);
            body.setLinearVelocity(xV, yV);
            setTexture(textures[texture]);
            if (sound == 1) {
                jetpackSound.play();
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (texture != null) {
            sb.begin();
            float x = body.getPosition().x * B2DVars.PPM - B2DVars.PLAYER_WIDTH;
            float y = body.getPosition().y * B2DVars.PPM - B2DVars.PLAYER_HEIGHT;
            if (shielded && !texture.equals(textures[BLANK]))
                sb.draw(shield.getFrame(), x - 25, y - 30, 50, 60);
            if(invulnerableTimer <= invulnerableTime && invulnerableTimer > 0){
                if (invulnerableBlink < 5){
                    sb.draw(texture, x - xOffset, y - yOffset, 54, 48);
                    invulnerableBlink++;
                } else {
                    sb.draw(textures[BLANK], x - xOffset, y - yOffset, 54, 48);
                    invulnerableBlink = 0;
                }
            }
            else
                sb.draw(texture, x - xOffset, y - yOffset, 54, 48);
            sb.end();
        }
    }

    public void update(float dt){
        shield.update(dt);
        if(invulnerableTimer > 0){
            invulnerableTimer -= dt;
        }
    }

    protected void createPlayerBody(float xPos, float yPos) {
        PolygonShape shape = new PolygonShape();
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
        shape.dispose();
    }
}
