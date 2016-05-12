package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.game.bb.entities.SPBullet;
import com.game.bb.handlers.*;
import com.game.bb.entities.SPPlayer;


/**
 * TODO LIST --
 * --Remove dead bullet textures/bodies (check how big the bullets list gets)
 * --Add different game screens with options to host or join
 * --CLEAN UP CODE ( LOL XD )
 * --Add textures to entities
 * --Add background
 * --Improve map?
 * --Add music \o/
 * --?
 */
public class PlayState extends GameState {

    private World world;
    private Box2DDebugRenderer b2dr;
    private OrthographicCamera b2dCam;
    private SPContactListener cl;
    private SPPlayer player;
    private Array<SPPlayer> opponents;
    private int amntBullets = 5;
    private float bulletRefresh, lastJumpDirection = 1;
    private Array<SPBullet> bullets;
    private float respawnTimer = 0;
    private HUD hud;
    private Texture backGround = new Texture("images/spaceBackground.png");

    public PlayState(GameStateManager gsm){
        super(gsm);
        
        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();

        bullets = new Array<SPBullet>();
        opponents = new Array<SPPlayer>();
        // create boundaries
        createBoundary(cam.viewportWidth / 2, cam.viewportHeight, cam.viewportWidth / 2, 5); //top
        createBoundary(cam.viewportWidth / 2, 0, cam.viewportWidth / 2, 5); //bottom
        createBoundary(0, cam.viewportHeight / 2, 5, cam.viewportHeight / 2); // left
        createBoundary(cam.viewportWidth, cam.viewportHeight / 2, 5, cam.viewportHeight / 2); // right

        //build random platforms


        //Players
        player = new SPPlayer(world, cam.viewportWidth / 2, cam.viewportHeight / 2, B2DVars.BIT_PLAYER, B2DVars.ID_PLAYER, B2DVars.MY_ID, "blue");


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, B2DVars.CAM_WIDTH / B2DVars.PPM, B2DVars.CAM_HEIGHT / B2DVars.PPM);
    }

    private void createBoundary(float xPos, float yPos, float width, float height) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos / B2DVars.PPM, yPos / B2DVars.PPM);
        bdef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / B2DVars.PPM, height / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        // set bits to collide with
        fdef.filter.categoryBits = B2DVars.BIT_GROUND;
        fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_OPPONENT;
        body.createFixture(fdef).setUserData(B2DVars.ID_GROUND);
    }

    public void shoot() {
        if (amntBullets > 0) {
            Vector2 pos = player.getPosition();
            SPBullet bullet = new SPBullet(world, pos.x, pos.y, lastJumpDirection, false);
            bullets.add(bullet);
            gsm.addAction(B2DVars.MY_ID + ":SHOOT:" + pos.x + ":" + pos.y + ":" + lastJumpDirection);
            amntBullets--;
        }
    }

    public void opponentShot(float xPos, float yPos, float dir) {
        SPBullet bullet = new SPBullet(world, xPos, yPos, dir, true);
        bullets.add(bullet);
    }


    public void handleInput() {

        if (SPInput.isPressed(SPInput.BUTTON_RIGHT) && cl.canJump()) {
            Vector2 temp = player.getPosition();
            player.jump(B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, temp.x, temp.y);
            gsm.addAction(B2DVars.MY_ID + ":MOVE:" + B2DVars.PH_JUMPX + ":" + B2DVars.PH_JUMPY + ":" + temp.x + ":" + temp.y);
            lastJumpDirection = 1;
        }
        if (SPInput.isPressed(SPInput.BUTTON_LEFT) && cl.canJump()) {
            Vector2 temp = player.getPosition();
            player.jump(-B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, temp.x, temp.y);
            gsm.addAction(B2DVars.MY_ID + ":MOVE:" + -B2DVars.PH_JUMPX + ":" + B2DVars.PH_JUMPY + ":" + temp.x + ":" + temp.y);
            lastJumpDirection = -1;
        }
        if (SPInput.isPressed(SPInput.BUTTON_W)) {
            shoot();
        }

    }

    private void opponentActions() {
        String[] action = gsm.getOpponentAction().split(":");
        SPPlayer opponent = getOpponent(action[0]);
        if (validOpponentAction(action)) {
            if (action[1].equals("MOVE") && opponent != null)
                opponent.jump(Float.valueOf(action[2]), Float.valueOf(action[3]),
                        Float.valueOf(action[4]), Float.valueOf(action[5]));
            else if (action[1].equals("SHOOT") && opponent != null)
                opponentShot(Float.valueOf(action[2]), Float.valueOf(action[3]), Float.valueOf(action[4]));
            else if (action[1].equals("DEATH") && opponent != null) {
                hud.setOpponentDeath(action[1],action[2]);
                opponent.kill();
            } else if (action[1].equals("RESPAWN") && opponent != null) {
                opponent.revive();
                opponent.jump(Float.valueOf(action[2]), Float.valueOf(action[3]),
                        Float.valueOf(action[4]), Float.valueOf(action[5]));
            } else if(action[1].equals("CONNECT")){
                opponents.add(new SPPlayer(world, Float.valueOf(action[2]), Float.valueOf(action[3]), Short.valueOf(action[4]), action[5], action[0], action[7]));
            } else if(action[1].equals("DISCONNECT")){
                opponent = getOpponent(action[0]);
                opponents.removeValue(opponent, false);
                world.destroyBody(opponent.getBody());
            }
        }
    }

    private boolean validOpponentAction(String[] split) {
        if (split.length >= 2 && !split[0].equals(B2DVars.MY_ID))
            return true;
        return false;
    }

    private SPPlayer getOpponent(String id) {
        for (SPPlayer player : opponents) {
            if (player.getId().equals(id)) {
                return player;
            }
        }
        return null;
    }

    private void respawnPlayer() {
        respawnTimer = 0;
        player.revive();
        player.jump(0, 0, 100 / B2DVars.PPM, 100 / B2DVars.PPM);
        gsm.addAction(B2DVars.MY_ID + ":RESPAWN:0:0:" + player.getPosition().x + ":" + player.getPosition().y);
        cl.revive();
    }

    private void refreshBullets(float dt) {
        if (bulletRefresh > 5f) {
            amntBullets = 5;
            bulletRefresh = 0;
        } else {
            bulletRefresh += dt;
        }
    }

    public void removeDeadBodes() {
        for (Body b : cl.getBodiesToRemove()) {
            bullets.removeValue((SPBullet) b.getUserData(), true);
            world.destroyBody(b);
        }
        for (SPBullet spb : bullets) {
            if (spb.getXPos() < 0 || spb.getXPos() > cam.viewportWidth / B2DVars.PPM) {
                bullets.removeValue(spb, true);
                world.destroyBody(spb.getBody());
            }
        }
    }


    @Override
    public void update(float dt) {
        handleInput();
        world.step(dt, 6, 2);
        player.update(dt);
        for (SPPlayer player : opponents) {
            player.update(dt);
        }
        opponentActions();
        refreshBullets(dt);
        if (cl.amIHit()) {
            if (cl.getKillingBullet() != null) {
                cl.getKillingBullet().getBody().setTransform(cam.viewportWidth * 2, cam.viewportHeight * 2, 0);
            }
            player.kill();
            hud.addPlayerDeath();
            gsm.addAction(B2DVars.MY_ID + ":DEATH:" + hud.getDeathCount() + ":0:" + player.getPosition().x + ":" + player.getPosition().y);
        }
        if (player.isDead()) {
            respawnTimer += dt;
            if (respawnTimer >= B2DVars.RESPAWN_TIME) {
                respawnPlayer();
            }
        }
        removeDeadBodes();
    }

    @Override
    public void render() {
        //Clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(backGround, 0, 0);
        sb.end();
        b2dr.render(world, b2dCam.combined); // Debug renderer. Hitboxes etc...
        for (SPBullet b : bullets) {
            b.render(sb);
        }
        for (SPPlayer opponent : opponents) {
            opponent.render(sb);
        }
        player.render(sb);
        hud.render(sb);

        //Do this last in render
        sb.setProjectionMatrix(cam.combined);
    }

    @Override
    public void dispose() {

    }
}
