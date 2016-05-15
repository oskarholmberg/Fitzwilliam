package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.game.bb.entities.SPBullet;
import com.game.bb.entities.SPGrenade;
import com.game.bb.entities.SPSprite;
import com.game.bb.handlers.*;
import com.game.bb.entities.SPPlayer;
import com.game.bb.net.PlayStateNetworkMonitor;


/**
 * TODO LIST --
 * --Remove dead bullet textures/bodies (check how big the worldEntities list gets)
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
    private int amntBullets = 3, amntGrenades = 1;
    private float bulletRefresh, lastJumpDirection = 1, grenadeRefresh;
    private String entityID = B2DVars.MY_ID + "%0";
    private Array<SPSprite> worldEntities;
    private float respawnTimer = 0;
    private HUD hud;
    private Texture backGround = new Texture("images/spaceBackground.png");
    private Sound reload = Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav"));
    private float[] touchNbrs = {(B2DVars.CAM_WIDTH / 5), B2DVars.CAM_WIDTH * 4 / 5};
    private PlayStateNetworkMonitor mon;
    private OrthogonalTiledMapRenderer tmr;
    private boolean clipIsEmpty = false, grenadesIsEmpty = false;

    public PlayState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();

        worldEntities = new Array<SPSprite>();
        opponents = new Array<SPPlayer>();
        // create boundaries


        MapBuilder mb = new MapBuilder(world, new TmxMapLoader().load("maps/Moon.tmx"),
                new Vector2(cam.viewportWidth, cam.viewportHeight), true);
        tmr = mb.buildMap();

        //Players
        player = new SPPlayer(world, B2DVars.MY_ID, B2DVars.CAM_WIDTH / 2 / B2DVars.PPM,
                B2DVars.CAM_HEIGHT / B2DVars.PPM, B2DVars.BIT_PLAYER, B2DVars.ID_PLAYER, "blue", newEntityID());


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, B2DVars.CAM_WIDTH / B2DVars.PPM, B2DVars.CAM_HEIGHT / B2DVars.PPM);
    }

    public void shoot() {
        if (amntBullets > 0 && !player.isDead()) {
            String ID = newEntityID();
            mon.sendPlayerAction("SHOOT", 0, 0, Float.toString(lastJumpDirection),
                    Long.toString(System.currentTimeMillis()), ID);
            Vector2 pos = player.getPosition();
            SPBullet bullet = new SPBullet(world, pos.x, pos.y, lastJumpDirection, false, ID);
            worldEntities.add(bullet);
            amntBullets--;
            hud.setAmountBulletsLeft(amntBullets);
            if (amntBullets == 0) {
                clipIsEmpty = true;
                bulletRefresh = 0;
            }
        }
    }

    private void throwGrenade() {
        if (amntGrenades > 0 && !player.isDead()) {
            String ID = newEntityID();
            mon.sendPlayerAction("GRENADE", 0, 0, Float.toString(lastJumpDirection),
                    Long.toString(System.currentTimeMillis()), ID);
            Vector2 pos = player.getPosition();
            worldEntities.add(new SPGrenade(world, pos.x, pos.y, lastJumpDirection, ID));
            amntGrenades--;
            if (amntGrenades == 0) {
                grenadesIsEmpty = true;
                grenadeRefresh = 0;
            }
        }
    }
    public String newEntityID(){
        String[] split = entityID.split("%");
        entityID = split[0] + "%" +(Integer.valueOf(split[1]) + 1);
        return entityID;
    }

    private void enemyGrenade(float xPos, float yPos, float dir, long timeSync, String ID){
        long timeDiff = System.currentTimeMillis() - timeSync;
        xPos = xPos + (B2DVars.PH_GRENADE_X * timeDiff/1000) / B2DVars.PPM * dir;
        yPos = yPos + (B2DVars.PH_GRENADE_Y * timeDiff/1000) / B2DVars.PPM;
        worldEntities.add(new SPGrenade(world, xPos, yPos, dir, ID));
    }

    public void opponentShot(float xPos, float yPos, float dir, long timeSync, String bulletID) {
        long timeDiff = System.currentTimeMillis() - timeSync;
        xPos = xPos + (B2DVars.PH_BULLET_SPEED * timeDiff/1000) / B2DVars.PPM * dir;
        SPBullet bullet = new SPBullet(world, xPos, yPos, dir, true, bulletID);
        worldEntities.add(bullet);
    }

    public Vector2 playerPosition() {
        return player.getPosition();
    }


    public void handleInput() {
        if (SPInput.isPressed(SPInput.BUTTON_RIGHT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x > touchNbrs[1] && cl.canJump()) {
            SPInput.down = false;
            mon.sendPlayerAction("MOVE", B2DVars.PH_JUMPX, B2DVars.PH_JUMPY);
            player.jump(B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
            lastJumpDirection = 1;
        } else if (SPInput.isPressed(SPInput.BUTTON_LEFT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x < touchNbrs[0] && cl.canJump()) {
            SPInput.down = false;
            mon.sendPlayerAction("MOVE", -B2DVars.PH_JUMPX, B2DVars.PH_JUMPY);
            player.jump(-B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
            lastJumpDirection = -1;
        }
        if (SPInput.isPressed(SPInput.BUTTON_W) ||
                SPInput.isPressed() && SPInput.x > touchNbrs[0] && SPInput.x < touchNbrs[1]) {
            SPInput.down = false;
            shoot();
        }
        if (SPInput.isPressed(SPInput.BUTTON_E)) {

            throwGrenade();
        }

    }

    private void opponentActions() {
        String[] action = mon.getOpponentAction().split(":");
        float[] floats = mon.getPacketFloats(action);
        SPPlayer opponent = getOpponent(action[0]);
        if (validOpponentAction(action)) {
            if (action[1].equals("MOVE") && opponent != null)
                opponent.jump(floats[0], floats[1],
                        floats[2], floats[3]);
            else if (action[1].equals("SHOOT") && opponent != null)
                opponentShot(floats[2], floats[3], Float.valueOf(action[6]), Long.valueOf(action[7]),
                        action[8]);
            else if (action[1].equals("DEATH") && opponent != null) {
                hud.setOpponentDeath(action[0], action[6]);
                opponent.kill(1);
                removeKillingEntity(action[7]);
                //In order for body to fly through the air when killed.
                opponent.getBody().applyForceToCenter(50*Float.valueOf(action[8]), 50, true);
            } else if (action[1].equals("RESPAWN") && opponent != null) {
                opponent.revive();
                opponent.jump(floats[0], floats[1],
                        floats[2], floats[3]);
            } else if (action[1].equals("CONNECT")) {
                if (getOpponent(action[0]) == null) {
                    SPPlayer newOpponent = new SPPlayer(world, action[0], floats[2], floats[3],
                            Short.valueOf(action[6]), action[7], action[8], action[9]);
                    opponents.add(newOpponent);
                    hud.setOpponentDeath(action[0], action[9]);
                    newOpponent.jump(floats[0], floats[1], floats[2], floats[3]);
                }
            } else if (action[1].equals("DISCONNECT")) {
                opponent = getOpponent(action[0]);
                if (opponent != null) {
                    opponents.removeValue(opponent, false);
                    hud.removeOpponentDeathCount(action[0]);
                    world.destroyBody(opponent.getBody());
                }
            } else if (action[1].equals("GRENADE")) {
                enemyGrenade(floats[2], floats[3], Float.valueOf(action[6]), Long.valueOf(action[7]), action[8]);
            }
        }
    }

    private void removeKillingEntity(String killerID){
        for(SPSprite s : worldEntities){
            if (s.getID().equals(killerID))
                worldEntities.removeValue(s, true);
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
        player.jump(0, 0, ((((B2DVars.CAM_WIDTH - 100) / B2DVars.PPM) * (float) Math.random() + 50) / B2DVars.PPM),
                (B2DVars.CAM_HEIGHT / B2DVars.PPM) - B2DVars.PLAYER_HEIGHT / 2);
        mon.sendPlayerAction("RESPAWN", 0, 0);
        cl.resetJumps();
        cl.revivePlayer();
    }

    private void refreshAmmo(float dt) {
        if (bulletRefresh > 3f && clipIsEmpty) {
            amntBullets = 3;
            clipIsEmpty = false;
            hud.setAmountBulletsLeft(amntBullets);
            reload.play();
        } else {
            bulletRefresh += dt;
        }
        if (grenadeRefresh > 8f && grenadesIsEmpty) {
            amntGrenades = 1;
            grenadesIsEmpty = false;
        } else {
            grenadeRefresh += dt;
        }
    }

    private void removeDeadBodies() {
        for (Body b : cl.getBodiesToRemove()) {
            if (b.getUserData() instanceof SPBullet) {
                worldEntities.removeValue((SPSprite) b.getUserData(), true);
                world.destroyBody(b);
            } else if (b.getUserData() instanceof SPGrenade){
                if (((SPGrenade) b.getUserData()).finishedBouncing()){
                    worldEntities.removeValue((SPSprite) b.getUserData(), true);
                    world.destroyBody(b);
                }
            }
        }
        cl.clearBulletList();
    }

    private void grenadeBounces() {
        for (Body b : cl.getGrenadeBounces()) {
            if ( ((SPGrenade) b.getUserData()).finishedBouncing()){
                worldEntities.removeValue((SPSprite) b.getUserData(), true);
                world.destroyBody(b);
            }
        }
        cl.clearGrenadeList();
    }

    private void playerHit() {
        if (!player.isDead()) {
            player.kill(1);
            hud.addPlayerDeath();
            //In this addAction add the ID of the killing bullet last
            mon.sendPlayerAction("DEATH", 0, 0, hud.getDeathCount(),
                    cl.getKillingEntity().getID(), Float.toString(cl.getKillingEntity().getDirection()));
        }
    }

    public void setNetworkMonitor(PlayStateNetworkMonitor mon) {
        this.mon = mon;
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
        refreshAmmo(dt);
        if (cl.isPlayerHit()) {
            playerHit();
        }
        if (player.isDead()) {
            respawnTimer += dt;
            if (respawnTimer >= B2DVars.RESPAWN_TIME) {
                respawnPlayer();
            }
        }
        grenadeBounces();
        removeDeadBodies();
    }

    @Override
    public void render() {
        //Clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(backGround, 0, 0);
        sb.end();
        tmr.setView(cam);
        tmr.render();
        for (SPSprite b : worldEntities) {
            b.render(sb);
        }
        for (SPPlayer opponent : opponents) {
            opponent.render(sb);
        }
        player.render(sb);
        hud.render(sb);

        //Do this last in render
        b2dr.render(world, b2dCam.combined); // Debug renderer. Hitboxes etc...
        sb.setProjectionMatrix(cam.combined);
    }

    @Override
    public void dispose() {

    }
}
