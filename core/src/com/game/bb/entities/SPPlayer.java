package com.game.bb.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 09/05/16.
 */
public class SPPlayer extends SPSprite {

    private Sound sound;
    private Texture[] textures;
    private boolean onGround = true, isDead = false;
    private float textureTimer = 0;
    private int xOffset = 23, yOffset = 25;
    private PolygonShape shape;
    public static int STAND_RIGHT = 0, STAND_LEFT = 1, JUMP_RIGHT = 2, JUMP_LEFT = 3,
            DEAD_RIGHT = 4, DEAD_LEFT = 5;

    public SPPlayer(World world, float xPos, float yPos, int ID) {
        super(world, ID);
        createPlayerBody(xPos, yPos);
        dir = 0;
        sound = Gdx.audio.newSound(Gdx.files.internal("sfx/jetpackFire.wav"));
        loadTexture("blue");
        setTexture(textures[STAND_RIGHT]);
    }

    public void jump(float xForce, float yForce, float xPos, float yPos) {
        if (!isDead) {
            body.setTransform(xPos, yPos, 0);
            body.setLinearVelocity(0, 0);
            body.applyForceToCenter(xForce, yForce, true);
            textureTimer = 0;
            onGround = false;
            if (xForce < 0) {
                setTexture(textures[JUMP_LEFT]);
            } else {
                setTexture(textures[JUMP_RIGHT]);
            }
            sound.play();
        }
    }

    /**
     * Kills the Player and sets it status to dead.
     *
     * @param dir, the direction of the bullet causing the killing blow.
     */
    public void kill(float dir) {
        isDead = true;
        if (dir < 0) {
            setTexture(textures[DEAD_RIGHT]);
        } else {
            setTexture(textures[DEAD_LEFT]);
        }
    }

    public void revive() {
        isDead = false;
        setTexture(textures[STAND_RIGHT]);
    }

    @Override
    public void render(SpriteBatch sb) {
        if (texture != null) {
            sb.begin();
            float x = body.getPosition().x * B2DVars.PPM - B2DVars.PLAYER_WIDTH;
            float y = body.getPosition().y * B2DVars.PPM - B2DVars.PLAYER_HEIGHT;
            if (!isDead) {
                if (textureTimer >= 0.5f && !onGround) {
                    if (texture.equals(textures[JUMP_LEFT])) {
                        setTexture(textures[STAND_LEFT]);
                        xOffset = 30;
                    } else {
                        setTexture(textures[STAND_RIGHT]);
                        xOffset = 23;
                    }
                    onGround = true;
                }
            }
            sb.draw(texture, x - xOffset, y - yOffset, 54, 48);
            sb.end();
        }
    }

    @Override
    public void update(float dt) {
        textureTimer += dt;
    }

    public boolean isDead() {
        return isDead;
    }

    private void createPlayerBody(float xPos, float yPos) {
        shape = new PolygonShape();
        shape.setAsBox(B2DVars.PLAYER_WIDTH, B2DVars.PLAYER_HEIGHT);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
        fdef.filter.maskBits = B2DVars.BIT_GROUND | B2DVars.BIT_BULLET | B2DVars.BIT_GRENADE
                | B2DVars.BIT_POWERUP;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);
        body.createFixture(fdef).setUserData(B2DVars.ID_PLAYER);
        body.setUserData(this);

        //add foot
        shape.setAsBox(B2DVars.PLAYER_WIDTH - 2 / B2DVars.PPM, 2 / B2DVars.PPM, new Vector2(0, -B2DVars.PLAYER_HEIGHT), 0);
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
        fdef.filter.maskBits = B2DVars.BIT_GROUND;
        fdef.isSensor = true;
        body.createFixture(fdef).setUserData(B2DVars.ID_FOOT);
    }

    public void loadTexture(String color) {
        textures = new Texture[6];
        if (color.equals("blue")) {
            textures[STAND_RIGHT] = new Texture("images/player/bluePlayerStandRight.png");
            textures[STAND_LEFT] = new Texture("images/player/bluePlayerStandLeft.png");
            textures[JUMP_RIGHT] = new Texture("images/player/bluePlayerJumpRight.png");
            textures[JUMP_LEFT] = new Texture("images/player/bluePlayerJumpLeft.png");
            textures[DEAD_RIGHT] = new Texture("images/player/bluePlayerDeadRight.png");
            textures[DEAD_LEFT] = new Texture("images/player/bluePlayerDeadLeft.png");
        } else {
            textures[STAND_RIGHT] = new Texture("images/player/redPlayerStandRight.png");
            textures[STAND_LEFT] = new Texture("images/player/redPlayerStandLeft.png");
            textures[JUMP_RIGHT] = new Texture("images/player/redPlayerJumpRight.png");
            textures[JUMP_LEFT] = new Texture("images/player/redPlayerJumpLeft.png");
            textures[DEAD_RIGHT] = new Texture("images/player/redPlayerDeadRight.png");
            textures[DEAD_LEFT] = new Texture("images/player/redPlayerDeadLeft.png");
        }
    }

    @Override
    public void dispose() {
        texture.dispose();
        shape.dispose();
    }
}
