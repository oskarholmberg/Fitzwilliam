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
import com.game.bb.handlers.*;
import com.game.bb.entities.SPPlayer;
import com.game.bb.net.PlayStateNetworkMonitor;


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
    private int amntBullets = 3;
    private float bulletRefresh, lastJumpDirection = 1;
    private Array<SPBullet> bullets;
    private float respawnTimer = 0;
    private HUD hud;
    private Texture backGround = new Texture("images/spaceBackground.png");
    private Sound reload = Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav"));
    private float[] touchNbrs = {(B2DVars.CAM_WIDTH / 5), B2DVars.CAM_WIDTH * 4 / 5};
    private PlayStateNetworkMonitor mon;
    private OrthogonalTiledMapRenderer tmr;
    private boolean clipIsEmpty = false;

    public PlayState(GameStateManager gsm, String ipAddress, int port) {
        super(gsm);

        mon = new PlayStateNetworkMonitor(this, ipAddress, port);

        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();

        bullets = new Array<SPBullet>();
        opponents = new Array<SPPlayer>();
        // create boundaries


        MapBuilder mb = new MapBuilder(world, new TmxMapLoader().load("maps/Moon.tmx"),
                new Vector2(cam.viewportWidth, cam.viewportHeight), true);
        tmr = mb.buildMap();

        //Players
        player = new SPPlayer(world, B2DVars.MY_ID, B2DVars.CAM_WIDTH / 2 / B2DVars.PPM,
                B2DVars.CAM_HEIGHT/B2DVars.PPM, B2DVars.BIT_PLAYER, B2DVars.ID_PLAYER, "blue");


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, B2DVars.CAM_WIDTH / B2DVars.PPM, B2DVars.CAM_HEIGHT / B2DVars.PPM);
    }

    public void shoot() {
        if (amntBullets > 0 && !player.isDead()) {
            Vector2 pos = player.getPosition();
            SPBullet bullet = new SPBullet(world, pos.x, pos.y, lastJumpDirection, false);
            bullets.add(bullet);
            mon.sendPlayerAction("SHOOT", 0, 0, Float.toString(lastJumpDirection), Long.toString(System.currentTimeMillis()));
            amntBullets--;
            hud.setAmountBulletsLeft(amntBullets);
            if (amntBullets == 0) {
                clipIsEmpty = true;
                bulletRefresh = 0;
            }
        }
    }

    public void opponentShot(float xPos, float yPos, float dir, long timeSync) {
        long timeDiff = System.currentTimeMillis() - timeSync;
        System.out.println("TimeDiff: " + timeDiff + " previous xPos: " + xPos);
        xPos = xPos + (B2DVars.PH_BULLET_SPEED * timeDiff/1000) / B2DVars.PPM * dir;
        System.out.println("Time synced xPos: " + xPos);
        SPBullet bullet = new SPBullet(world, xPos, yPos, dir, true);
        bullets.add(bullet);
    }

    public Vector2 playerPosition() {
        return player.getPosition();
    }


    public void handleInput() {
        if (SPInput.isPressed(SPInput.BUTTON_RIGHT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x > touchNbrs[1] && cl.canJump()) {
            SPInput.down = false;
            player.jump(B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
            mon.sendPlayerAction("MOVE", B2DVars.PH_JUMPX, B2DVars.PH_JUMPY);
            lastJumpDirection = 1;
        } else if (SPInput.isPressed(SPInput.BUTTON_LEFT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x < touchNbrs[0] && cl.canJump()) {
            SPInput.down = false;
            player.jump(-B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
            mon.sendPlayerAction("MOVE", -B2DVars.PH_JUMPX, B2DVars.PH_JUMPY);
            lastJumpDirection = -1;
        }
        if (SPInput.isPressed(SPInput.BUTTON_W) ||
                SPInput.isPressed() && SPInput.x > touchNbrs[0] && SPInput.x < touchNbrs[1]) {
            SPInput.down = false;
            shoot();
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
                opponentShot(floats[2], floats[3], Float.valueOf(action[6]), Long.valueOf(action[7]));
            else if (action[1].equals("DEATH") && opponent != null) {
                hud.setOpponentDeath(action[0], action[6]);
                opponent.kill(1);
            } else if (action[1].equals("RESPAWN") && opponent != null) {
                opponent.revive();
                opponent.jump(floats[0], floats[1],
                        floats[2], floats[3]);
            } else if (action[1].equals("CONNECT")) {
                if (getOpponent(action[0]) == null) {
                    SPPlayer newOpponent = new SPPlayer(world, action[0], floats[2], floats[3], Short.valueOf(action[6]), action[7], action[8]);
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
        player.jump(0, 0, (B2DVars.CAM_WIDTH / 2 / B2DVars.PPM) * (float) Math.random(), B2DVars.CAM_HEIGHT / B2DVars.PPM);
        mon.sendPlayerAction("RESPAWN", 0, 0);
        cl.resetJumps();
        cl.revivePlayer();
    }

    private void refreshBullets(float dt) {
        if (bulletRefresh > 3f && clipIsEmpty) {
            amntBullets = 3;
            clipIsEmpty = false;
            hud.setAmountBulletsLeft(amntBullets);
            reload.play();
        } else {
            bulletRefresh += dt;
        }
    }

    public void removeDeadBodies() {
        for (Body b : cl.getBodiesToRemove()) {
            if (b.getUserData() instanceof SPBullet) {
                bullets.removeValue((SPBullet) b.getUserData(), true);
                world.destroyBody(b);
            }
        }
        cl.clearBulletList();
    }

    private void playerHit() {
        if (!player.isDead()) {
            player.kill(1);
            hud.addPlayerDeath();
            //In this addAction add the ID of the killing bullet last
            mon.sendPlayerAction("DEATH", 0, 0, hud.getDeathCount());
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        world.step(dt, 6, 2);
        removeDeadBodies();
        player.update(dt);
        for (SPPlayer player : opponents) {
            player.update(dt);
        }
        opponentActions();
        refreshBullets(dt);
        if (cl.isPlayerHit()) {
            playerHit();
        }
        if (player.isDead()) {
            respawnTimer += dt;
            if (respawnTimer >= B2DVars.RESPAWN_TIME) {
                respawnPlayer();
            }
        }
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
        for (SPBullet b : bullets) {
            b.render(sb);
        }
        for (SPPlayer opponent : opponents) {
            opponent.render(sb);
        }
        player.render(sb);
        hud.render(sb);

        //Do this last in render
//        b2dr.render(world, b2dCam.combined); // Debug renderer. Hitboxes etc...
        sb.setProjectionMatrix(cam.combined);
    }

    @Override
    public void dispose() {

    }
}
