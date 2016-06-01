package com.game.bb.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 09/05/16.
 */
public class SPPlayer extends SPSprite {

    protected Sound jetpackSound;
    protected Texture[] textures;
    protected int xOffset = 23, yOffset = 25;
    private boolean onGround = true, isDead = false;
    private float textureTimer = 0;
    private String color;
    private PolygonShape shape;
    public static int STAND_RIGHT = 0, STAND_LEFT = 1, JUMP_RIGHT = 2, JUMP_LEFT = 3,
            DEAD_RIGHT = 4, DEAD_LEFT = 5;

    public SPPlayer(World world, float xPos, float yPos, int id, String color) {
        super(world, id);
        this.color=color;
        createPlayerBody(xPos, yPos);
        dir = 0;
        jetpackSound = Assets.getSound("jetpack");
        loadTexture(color);
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
                PlayState.playState.currentTexture=JUMP_LEFT;
            } else {
                setTexture(textures[JUMP_RIGHT]);
                PlayState.playState.currentTexture=JUMP_RIGHT;
            }
            jetpackSound.play();
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
            PlayState.playState.currentTexture=DEAD_RIGHT;
        } else {
            setTexture(textures[DEAD_LEFT]);
            PlayState.playState.currentTexture=DEAD_LEFT;
        }
    }

    public void revive(Vector2 pos, float dir) {
        isDead = false;
        body.setTransform(pos.x, pos.y, 0);
        body.setAwake(true);
        if(dir < 0) {
            setTexture(textures[STAND_LEFT]);
            PlayState.playState.currentTexture = STAND_LEFT;
        }else{
            setTexture(textures[STAND_RIGHT]);
            PlayState.playState.currentTexture = STAND_RIGHT;
        }
    }


    public String getColor(){
        return color;
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
                        PlayState.playState.currentTexture=STAND_LEFT;
                        xOffset = 30;
                    } else {
                        setTexture(textures[STAND_RIGHT]);
                        PlayState.playState.currentTexture=STAND_RIGHT;
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

    protected void createPlayerBody(float xPos, float yPos) {
        shape = new PolygonShape();
        shape.setAsBox(B2DVars.PLAYER_WIDTH, B2DVars.PLAYER_HEIGHT);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
        fdef.filter.maskBits = B2DVars.BIT_GROUND | B2DVars.BIT_BULLET | B2DVars.BIT_GRENADE
                | B2DVars.BIT_ENEMY_ENTITY | B2DVars.BIT_BOUNCE;
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
        shape.dispose();
    }

    public void bouncePlayer(){
        body.setLinearVelocity(0, 0);
        body.applyForceToCenter(MathUtils.random(-3f, 3f) * 40, B2DVars.PH_JUMPY*1.3f, true);
    }

    public void loadTexture(String color) {
        textures = new Texture[6];

        textures[STAND_RIGHT] = Assets.getTex(color + "StandRight");
        textures[STAND_LEFT] = Assets.getTex(color + "StandLeft");
        textures[JUMP_RIGHT] = Assets.getTex(color + "JumpRight");
        textures[JUMP_LEFT] = Assets.getTex(color + "JumpLeft");
        textures[DEAD_RIGHT] = Assets.getTex(color + "DeadRight");
        textures[DEAD_LEFT] = Assets.getTex(color + "DeadLeft");
    }

    @Override
    public void dispose() {
    }
}
