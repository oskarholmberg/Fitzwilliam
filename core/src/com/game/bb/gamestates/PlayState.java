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
import com.badlogic.gdx.utils.ArrayMap;
import com.game.bb.entities.SPBullet;
import com.game.bb.entities.SPGrenade;
import com.game.bb.entities.SPPower;
import com.game.bb.entities.SPSprite;
import com.game.bb.handlers.*;
import com.game.bb.entities.SPPlayer;
import com.game.bb.net.PlayStateNetworkMonitor;
import com.game.bb.net.client.GameClientNew;
import com.game.bb.net.packets.EntityPacket;


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
    private Array<Vector2> spawnLocations;
    private int amntBullets = B2DVars.AMOUNT_BULLET, amntGrenades = B2DVars.AMOUNT_GRENADE;
    private float bulletRefresh, lastJumpDirection = 1, grenadeRefresh, powerReload = 30f;
    private String entityID = B2DVars.MY_ID + "%0";
    private Array<SPSprite> worldEntities;
    private ArrayMap<String, SPGrenade> enemyGrenades = new ArrayMap<String, SPGrenade>();
    private ArrayMap<String, SPGrenade> myGrenades = new ArrayMap<String, SPGrenade>();
    private float respawnTimer = 0;
    private GameClientNew client;
    private HUD hud;
    private Texture backGround = new Texture("images/spaceBackground.png");
    private Sound reloadSound = Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav"));
    private Sound emptyClipSound = Gdx.audio.newSound(Gdx.files.internal("sfx/emptyClip.wav"));
    private float[] touchNbrs = {(B2DVars.CAM_WIDTH / 5), B2DVars.CAM_WIDTH * 4 / 5};
    private PlayStateNetworkMonitor mon;
    private OrthogonalTiledMapRenderer tmr;
    private boolean clipIsEmpty = false, grenadesIsEmpty = false;
    private float sendNetworkInfo = 0f;

    public PlayState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        client = new GameClientNew();

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();

        worldEntities = new Array<SPSprite>();
        opponents = new Array<SPPlayer>();
        // create boundaries

        String[] layers = {"moonBlocks", "domeBlocks"};
        MapBuilder mb = new MapBuilder(world, new TmxMapLoader().load("maps/moonSpawnDome.tmx"),
                new Vector2(cam.viewportWidth, cam.viewportHeight), layers, true);
        tmr = mb.buildMap();
        spawnLocations = mb.getSpawnLocations();

        //Players
        Vector2 spawn = spawnLocations.random();
        String tempEntityID = newEntityID();
        player = new SPPlayer(world, B2DVars.MY_ID, spawn.x,
                spawn.y, B2DVars.BIT_PLAYER, B2DVars.ID_PLAYER, "blue", tempEntityID);
        EntityPacket packet = new EntityPacket();
        packet.action = "CONNECT";
        packet.pos = spawn;
        packet.myID = B2DVars.MY_ID;
        packet.entityID = tempEntityID;
        client.sendTCP(packet);

        worldEntities.add(new SPPower(world, 600 / B2DVars.PPM, 500 / B2DVars.PPM,
                "GET_THIS_ID_FROM_SERVER"));


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, B2DVars.CAM_WIDTH / B2DVars.PPM, B2DVars.CAM_HEIGHT / B2DVars.PPM);
    }

    public void shoot() {
        if (clipIsEmpty)
            emptyClipSound.play();
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
            myGrenades.put(ID, new SPGrenade(world, pos.x, pos.y, lastJumpDirection, false, ID));
            amntGrenades--;
            hud.setAmountGrenadesLeft(amntGrenades);
            if (amntGrenades == 0) {
                grenadesIsEmpty = true;
                grenadeRefresh = 0;
            }
        }
    }

    public ArrayMap<String, SPGrenade> getMyEntities() {
        return myGrenades;
    }

    public String newEntityID() {
        String[] split = entityID.split("%");
        entityID = split[0] + "%" + (Integer.valueOf(split[1]) + 1);
        return entityID;
    }

    private void enemyGrenade(float xPos, float yPos, float dir, long timeSync, String ID) {
        long timeDiff = System.currentTimeMillis() - timeSync;
        xPos = xPos + (B2DVars.PH_GRENADE_X * timeDiff / 1000) / B2DVars.PPM * dir;
        yPos = yPos + (B2DVars.PH_GRENADE_Y * timeDiff / 1000) / B2DVars.PPM;
        enemyGrenades.put(ID, new SPGrenade(world, xPos, yPos, dir, true, ID));
    }

    public void opponentShot(float xPos, float yPos, float dir, long timeSync, String bulletID) {
        long timeDiff = System.currentTimeMillis() - timeSync;
        xPos = xPos + (B2DVars.PH_BULLET_SPEED * timeDiff / 1000) / B2DVars.PPM * dir;
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

    private void opponentActionsNew() {
        Object packet = client.getReceivedPacket();
        if (packet instanceof EntityPacket) {
            System.out.println("Entity packet found! :D ");
            EntityPacket pkt = (EntityPacket) packet;
            if (pkt.action.equals("CONNECT")) {
                SPPlayer newOpponent = new SPPlayer(world, pkt.myID, pkt.pos.x / B2DVars.PPM, pkt.pos.y / B2DVars.PPM
                        , B2DVars.BIT_OPPONENT, B2DVars.ID_OPPONENT, "red", pkt.entityID);
                opponents.add(newOpponent);
                System.out.println("Opponent added!");
                //hud.setOpponentDeath(action[0], action[9]);
            }
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
                opponent.getBody().applyForceToCenter(50 * Float.valueOf(action[8]), 50, true);
            } else if (action[1].equals("RESPAWN") && opponent != null) {
                opponent.revive();
                opponent.jump(floats[0], floats[1],
                        floats[2], floats[3]);
            } else if (action[1].equals("CONNECT")) {
                if (getOpponent(action[0]) == null) {
                    SPPlayer newOpponent = new SPPlayer(world, action[0], floats[2], floats[3],
                            Short.valueOf(action[6]), action[7], action[8], action[10]);
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
            } else if (action[1].equals("UPDATE_GRENADE")) {
                updateEnemyGrenade(floats[0], floats[1], Float.valueOf(action[7]),
                        Float.valueOf(action[8]), action[6]);
            }
        }
    }

    private void removeKillingEntity(String killerID) {
        if (enemyGrenades.containsKey(killerID)) {
            world.destroyBody(enemyGrenades.removeKey(killerID).getBody());
        } else {
            for (SPSprite s : worldEntities) {
                if (s.getID().equals(killerID)) {
                    worldEntities.removeValue(s, true);
                    world.destroyBody(s.getBody());
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
        Vector2 spawnLoc = spawnLocations.random();
        respawnTimer = 0;
        player.revive();
        player.jump(0, 0, spawnLoc.x, spawnLoc.y);
        mon.sendPlayerAction("RESPAWN", 0, 0);
        amntBullets = B2DVars.AMOUNT_BULLET;
        amntGrenades = B2DVars.AMOUNT_GRENADE;
        hud.setAmountBulletsLeft(amntBullets);
        hud.setAmountGrenadesLeft(amntGrenades);
        cl.resetJumps();
        cl.revivePlayer();
    }

    private void refreshAmmo(float dt) {
        if ((powerReload < 3f || bulletRefresh > 3f) && clipIsEmpty) {
            amntBullets = B2DVars.AMOUNT_BULLET;
            clipIsEmpty = false;
            hud.setAmountBulletsLeft(amntBullets);
            reloadSound.play();
        } else {
            bulletRefresh += dt;
        }
        if ((powerReload < 3f || grenadeRefresh > 8f) && grenadesIsEmpty) {
            amntGrenades = B2DVars.AMOUNT_GRENADE;
            grenadesIsEmpty = false;
            hud.setAmountGrenadesLeft(amntGrenades);
        } else {
            grenadeRefresh += dt;
        }
        if (powerReload < 20f) {
            powerReload += dt;
        }
    }

    private void removeDeadBodies() {
        for (Body b : cl.getBodiesToRemove()) {
            if (b.getUserData() instanceof SPSprite) {
                worldEntities.removeValue((SPSprite) b.getUserData(), true);
                world.destroyBody(b);
            }
        }
        cl.clearBulletList();
    }

    private void grenadeBounces(float dt) {
        for (Body b : cl.getFriendlyGrenadeBounces()) {
            if (b.getUserData() instanceof SPGrenade) {
                mon.sendPlayerAction("UPDATE_GRENADE", b.getLinearVelocity().x,
                        b.getLinearVelocity().y, ((SPGrenade) b.getUserData()).getID(), Float.toString(b.getPosition().x),
                        Float.toString(b.getPosition().y));
            }
        }
        for (String s : enemyGrenades.keys()) {
            if (enemyGrenades.get(s) != null && enemyGrenades.get(s).lifeTimeReached(dt)) {
                world.destroyBody(enemyGrenades.removeKey(s).getBody());
            }
        }
        for (String s : myGrenades.keys()) {
            if (myGrenades.get(s) != null && myGrenades.get(s).lifeTimeReached(dt)) {
                world.destroyBody(myGrenades.removeKey(s).getBody());
            }
        }
        cl.clearGrenadeList();
    }

    private void updateEnemyGrenade(float xForce, float yForce, float xPos, float yPos, String ID) {
        if (enemyGrenades.get(ID) != null) {
            enemyGrenades.get(ID).getBody().setTransform(xPos, yPos, 0);
            enemyGrenades.get(ID).getBody().setLinearVelocity(xForce, yForce);
        }
    }

    private void playerHit() {
        if (!player.isDead()) {
            player.kill(1);
            hud.addPlayerDeath();
            SPSprite temp = (SPSprite) cl.getKillingEntity().getBody().getUserData();
            if (temp instanceof SPGrenade) {
                if (enemyGrenades.containsKey(temp.getID())) {
                    world.destroyBody(enemyGrenades.removeKey(temp.getID()).getBody());
                } else {
                    world.destroyBody(myGrenades.removeKey(temp.getID()).getBody());
                }
            } else {
                //In this addAction add the ID of the killing bullet last
                mon.sendPlayerAction("DEATH", 0, 0, hud.getDeathCount(),
                        cl.getKillingEntity().getID(), Float.toString(cl.getKillingEntity().getDirection()));
            }
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
        for (SPSprite sprite : worldEntities) {
            sprite.update(dt);
        }
        for (String id : enemyGrenades.keys()) {
            enemyGrenades.get(id).update(dt);
        }
        for (String id : myGrenades.keys()) {
            myGrenades.get(id).update(dt);
        }
        opponentActionsNew();
        opponentActions();
        refreshAmmo(dt);
        if (cl.isPlayerHit()) {
            playerHit();
        }
        if (cl.powerTaken()) {
            powerReload = 0f;
        }
        if (player.isDead()) {
            respawnTimer += dt;
            if (respawnTimer >= B2DVars.RESPAWN_TIME) {
                respawnPlayer();
            }
        }
        grenadeBounces(dt);
        //removeDeadBodies should always be last in update
        removeDeadBodies();
        if (sendNetworkInfo >= 1 / 30f) {
            sendNetworkInfo = 0f;
            mon.sendInterpolationPoints();
        } else {
            sendNetworkInfo += dt;
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
        for (SPSprite b : worldEntities) {
            b.render(sb);
        }
        for (SPPlayer opponent : opponents) {
            opponent.render(sb);
        }
        for (String id : enemyGrenades.keys()) {
            enemyGrenades.get(id).render(sb);
        }
        for (String id : myGrenades.keys()) {
            myGrenades.get(id).render(sb);
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
