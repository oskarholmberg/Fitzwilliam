package com.oskarholmberg.fitzwilliam.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.oskarholmberg.fitzwilliam.handlers.Assets;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;
import com.oskarholmberg.fitzwilliam.gamestates.PlayState;

public class SPPlayer extends com.oskarholmberg.fitzwilliam.entities.SPSprite {

    protected Sound jetpackSound;
    protected Texture[] textures, ghostTex;
    protected int xOffset = 23, yOffset = 25, invulnerableBlink = 0;
    private boolean onGround = true, isDead = false, invulnerable = false, ghosted = false,
            spectateMode = false;
    private float textureTimer = 0, invulnerabilityTimer = 8f;
    private String color;
    private PolygonShape shape;
    public static int STAND_RIGHT = 0, STAND_LEFT = 1, JUMP_RIGHT = 2, JUMP_LEFT = 3,
            DEAD_RIGHT = 4, DEAD_LEFT = 5, BLANK=6;

    public SPPlayer(World world, float xPos, float yPos, int id, String color) {
        super(world, id);
        this.color=color;
        createPlayerBody(xPos, yPos);
        dir = 0;
        jetpackSound = Assets.getSound("jetpack");
        loadTexture(color);
        PlayState.playState.currentTexture = STAND_RIGHT;
    }

    public void jump(float xForce, float yForce, float xPos, float yPos) {
        body.setTransform(xPos, yPos, 0);
        body.setLinearVelocity(0, 0);
        body.applyForceToCenter(xForce, yForce, true);
        textureTimer = 0;
        onGround = false;
        if (xForce < 0) {
            PlayState.playState.currentTexture=JUMP_LEFT;
        } else {
            PlayState.playState.currentTexture=JUMP_RIGHT;
        }
        jetpackSound.play();

    }

    /**
     * Kills the Player and sets it status to dead.
     *
     * @param dir, the direction of the bullet causing the killing blow.
     */
    public void kill(float dir) {
        isDead = true;
        if (dir < 0) {
            PlayState.playState.currentTexture=DEAD_RIGHT;
        } else {
            PlayState.playState.currentTexture=DEAD_LEFT;
        }
    }

    public void revive(Vector2 pos, float dir) {
        isDead = false;
        body.setTransform(pos.x, pos.y, 0);
        body.setAwake(true);
        invulnerabilityTimer = 0f;
        if(dir < 0) {
            PlayState.playState.currentTexture = STAND_LEFT;
        }else{
            PlayState.playState.currentTexture = STAND_RIGHT;
        }
    }


    public String getColor(){
        return color;
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.begin();
        float x = body.getPosition().x * B2DVars.PPM - B2DVars.PLAYER_WIDTH;
        float y = body.getPosition().y * B2DVars.PPM - B2DVars.PLAYER_HEIGHT;
        if (!isDead) {
            if (textureTimer >= 0.5f && !onGround) {
                if (PlayState.playState.currentTexture == JUMP_LEFT) {
                    PlayState.playState.currentTexture=STAND_LEFT;
                    xOffset = 30;
                } else if (PlayState.playState.currentTexture == JUMP_RIGHT){
                    PlayState.playState.currentTexture=STAND_RIGHT;
                    xOffset = 23;
                }
                onGround = true;
            }
        }
        if (!invulnerable || spectateMode) {
            if((ghosted || spectateMode) && PlayState.playState.currentTexture <= 3) {
                sb.draw(ghostTex[PlayState.playState.currentTexture], x-xOffset, y-yOffset, 54, 48);
            } else {
                sb.draw(textures[PlayState.playState.currentTexture], x - xOffset, y - yOffset, 54, 48);
            }
        } else {
            if (invulnerableBlink < 5){
                sb.draw(textures[PlayState.playState.currentTexture], x - xOffset, y - yOffset, 54, 48);
                invulnerableBlink++;
            } else {
                sb.draw(textures[BLANK], x - xOffset, y - yOffset, 54, 48);
                invulnerableBlink = 0;
            }
        }
        sb.end();
    }

    @Override
    public void update(float dt) {
        textureTimer += dt;
        if (invulnerabilityTimer < 2f ) {
            invulnerable = true;
            invulnerabilityTimer += dt;
        } else {
            invulnerable = false;
        }
    }

    public boolean isInvulnerable(){
        return invulnerable;
    }

    public void setGhost(boolean ghost){
        ghosted = ghost;
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
        shape.setAsBox(B2DVars.PLAYER_WIDTH - 1 / B2DVars.PPM, 2 / B2DVars.PPM, new Vector2(0, -B2DVars.PLAYER_HEIGHT), 0);
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
        textures = new Texture[7];

        textures[STAND_RIGHT] = Assets.getTex(color + "StandRight");
        textures[STAND_LEFT] = Assets.getTex(color + "StandLeft");
        textures[JUMP_RIGHT] = Assets.getTex(color + "JumpRight");
        textures[JUMP_LEFT] = Assets.getTex(color + "JumpLeft");
        textures[DEAD_RIGHT] = Assets.getTex(color + "DeadRight");
        textures[DEAD_LEFT] = Assets.getTex(color + "DeadLeft");
        textures[BLANK] = Assets.getTex("blank");

        ghostTex = new Texture[4];
        ghostTex[STAND_RIGHT] = Assets.getTex("ghostStandRight");
        ghostTex[STAND_LEFT] = Assets.getTex("ghostStandLeft");
        ghostTex[JUMP_RIGHT] = Assets.getTex("ghostJumpRight");
        ghostTex[JUMP_LEFT] = Assets.getTex("ghostJumpLeft");
    }

    @Override
    public void dispose() {
    }

    public void setSpectateMode() {
        for (Fixture f : body.getFixtureList()){
            Filter filter = f.getFilterData();
            filter.maskBits = B2DVars.BIT_GROUND | B2DVars.BIT_BOUNCE;
            f.setFilterData(filter);
        }
        spectateMode = true;
    }

    public boolean spectateMode(){
        return spectateMode;
    }
}
