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
import com.game.bb.entities.SPSprite;
import com.game.bb.handlers.*;
import com.game.bb.entities.SPPlayer;
import com.game.bb.net.PlayStateNetworkMonitor;
import com.game.bb.net.client.GameClientNew;
import com.game.bb.net.packets.EntityCluster;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.TCPEventPacket;


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
    private ArrayMap<Integer, SPPlayer> opponents;
    private Array<Vector2> spawnLocations;
    private int entityAccum = 0;
    private int amntBullets = B2DVars.AMOUNT_BULLET, amntGrenades = B2DVars.AMOUNT_GRENADE;
    private float bulletRefresh, lastJumpDirection = 1, grenadeRefresh, powerReload = 30f;
    private Array<SPSprite> worldEntities;
    private ArrayMap<Integer, SPGrenade> enemyGrenades = new ArrayMap<Integer, SPGrenade>();
    private ArrayMap<Integer, SPGrenade> myGrenades = new ArrayMap<Integer, SPGrenade>();
    private ArrayMap<Integer, SPSprite> opEntities = new ArrayMap<Integer, SPSprite>();
    private float respawnTimer = 0;
    private GameClientNew client;
    private HUD hud;
    private Texture backGround = new Texture("images/spaceBackground.png");
    private Sound reloadSound = Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav"));
    private Sound emptyClipSound = Gdx.audio.newSound(Gdx.files.internal("sfx/emptyClip.wav"));
    private float[] touchNbrs = {(B2DVars.CAM_WIDTH / 5), B2DVars.CAM_WIDTH * 4 / 5};
    private PlayStateNetworkMonitor mon;
    private OrthogonalTiledMapRenderer tmr;
    private int pktSequence = 0;
    private boolean clipIsEmpty = false, grenadesIsEmpty = false;
    private float sendNetworkInfo = 0f;

    public PlayState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        client = new GameClientNew();
        client.connectToServer(0);

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();

        worldEntities = new Array<SPSprite>();
        opponents = new ArrayMap<Integer, SPPlayer>();
        // create boundaries

        String[] layers = {"moonBlocks", "domeBlocks"};
        MapBuilder mb = new MapBuilder(world, new TmxMapLoader().load("maps/moonSpawnDome.tmx"),
                new Vector2(cam.viewportWidth, cam.viewportHeight), layers, true);
        tmr = mb.buildMap();
        spawnLocations = mb.getSpawnLocations();

        //Players
        Vector2 spawn = spawnLocations.random();
        int tempEntityID = newEntityID();
        player = new SPPlayer(world, spawn.x,
                spawn.y, B2DVars.BIT_PLAYER, B2DVars.ID_PLAYER, "blue", tempEntityID);
        TCPEventPacket packet = new TCPEventPacket();
        packet.action = B2DVars.NET_CONNECT;
        packet.pos = spawn;
        packet.id = tempEntityID;
        client.sendTCP(packet);


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, B2DVars.CAM_WIDTH / B2DVars.PPM, B2DVars.CAM_HEIGHT / B2DVars.PPM);
    }

    public void shoot() {
        if (clipIsEmpty)
            emptyClipSound.play();
        if (amntBullets > 0 && !player.isDead()) {
            int ID = newEntityID();
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
            int ID = newEntityID();
            Vector2 pos = player.getPosition();
            SPGrenade spg = new SPGrenade(world, pos.x, pos.y, lastJumpDirection, false, ID);
            myGrenades.put(ID, spg);
            System.out.println("Grenade created: " + ID);
            amntGrenades--;
            hud.setAmountGrenadesLeft(amntGrenades);
            if (amntGrenades == 0) {
                grenadesIsEmpty = true;
                grenadeRefresh = 0;
            }
        }
    }

    public ArrayMap<Integer, SPGrenade> getMyEntities() {
        return myGrenades;
    }

    public int newEntityID() {
        entityAccum++;
        String temp = B2DVars.MY_ID + entityAccum;
        return Integer.valueOf(temp);
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

    private void opponentTCPEvents() {
        TCPEventPacket pkt = client.getTCPEventPackets();
        if (pkt==null) return;
        switch (pkt.action) {
            case B2DVars.NET_CONNECT:
                if (!opponents.containsKey(pkt.id)) {
                    SPPlayer newOpponent = new SPPlayer(world, pkt.pos.x, pkt.pos.y
                            , B2DVars.BIT_OPPONENT, B2DVars.ID_OPPONENT, "red", pkt.id);
                    opponents.put(pkt.id, newOpponent);
                    TCPEventPacket packet = new TCPEventPacket();
                    packet.action=B2DVars.NET_CONNECT;
                    packet.pos=player.getPosition();
                    packet.id=player.getID();
                    client.sendTCP(packet);
                }
                break;
            case B2DVars.NET_DESTROY_BODY:
                if(opEntities.containsKey(pkt.id)){
                    System.out.println("TCPEvent destroy body: " + pkt.id);
                    world.destroyBody(opEntities.removeKey(pkt.id).getBody());
                }
                break;
        }
    }

    private void opponentEntityEvents(){
        Array<EntityCluster> packets = client.getEntityClusters();
        for (EntityCluster cluster : packets) {
            for (EntityPacket pkt : cluster.pkts) {
                if (opEntities.containsKey(pkt.id)) {
                    Body b = opEntities.get(pkt.id).getBody();
                    if (pkt.alive == 1) {
                        b.setTransform(pkt.xp, pkt.yp, 0);
                        b.setLinearVelocity(pkt.xf, pkt.yf);
                        System.out.println("xPos: " + pkt.xp + " yPos: " + pkt.yp + " xF: " + pkt.xf + " yF: " + pkt.yf);
                    } else if (pkt.alive == 0) {
                        world.destroyBody(opEntities.removeKey(pkt.id).getBody());
                    }
                } else {
                    if (pkt.type == B2DVars.TYPE_GRENADE) {
                        opEntities.put(pkt.id, new SPGrenade(world, pkt.xp, pkt.yp, 0, true, pkt.id));
                        opEntities.get(pkt.id).getBody().setLinearVelocity(pkt.xf, pkt.yf);
                    } else if (pkt.type == B2DVars.TYPE_BULLET) {
                        opEntities.put(pkt.id, new SPBullet(world, pkt.xp, pkt.yp, 0f, true, pkt.id));
                        opEntities.get(pkt.id).getBody().setLinearVelocity(pkt.xf, pkt.yf);
                    }
                }
            }
        }
    }

    private void removeKillingEntity(int killerID) {
        if (enemyGrenades.containsKey(killerID)) {
            world.destroyBody(enemyGrenades.removeKey(killerID).getBody());
        } else {
            for (SPSprite s : worldEntities) {
                if (s.getID()==killerID) {
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

    private SPPlayer getOpponent(int id) {
        return opponents.get(id);
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

    private void sendEntityEvents(){
        if (myGrenades.size>0) {
            EntityPacket[] packets = new EntityPacket[myGrenades.size];
            int index = 0;
            for (int id : myGrenades.keys()) {
                EntityPacket pkt = new EntityPacket();
                Body b = myGrenades.get(id).getBody();
                pkt.xp = b.getPosition().x;
                pkt.yp = b.getPosition().y;
                pkt.xf = b.getLinearVelocity().x;
                pkt.yf = b.getLinearVelocity().y;
                pkt.id = id;
                pkt.alive = 1;
                pkt.type = B2DVars.TYPE_GRENADE;
                packets[index] = pkt;
                index++;
            }
            EntityCluster cluster = new EntityCluster();
            cluster.seq = pktSequence++;
            cluster.pkts = packets;
            client.sendUDP(cluster);
        }
    }

    private void grenadeStillAlive(float dt) {
        for (int id : myGrenades.keys()) {
            if (myGrenades.get(id) != null && myGrenades.get(id).lifeTimeReached(dt)) {
                TCPEventPacket pkt = new TCPEventPacket();
                pkt.id=id;
                pkt.action=B2DVars.NET_DESTROY_BODY;
                System.out.println("Destroy grenade: " + id);
                client.sendTCP(pkt);
                world.destroyBody(myGrenades.removeKey(id).getBody());
            }
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
        for (int id : opponents.keys()) {
            opponents.get(id).update(dt);
        }
        for (int id : myGrenades.keys()) {
            myGrenades.get(id).update(dt);
        }
        for (int id : opEntities.keys()){
            opEntities.get(id).update(dt);
        }
        opponentTCPEvents();
        opponentEntityEvents();
        refreshAmmo(dt);
        //if (cl.isPlayerHit()) {
        //    playerHit();
        //}
        if (player.isDead()) {
            respawnTimer += dt;
            if (respawnTimer >= B2DVars.RESPAWN_TIME) {
                respawnPlayer();
            }
        }
        if (sendNetworkInfo > 1/20f){
            sendNetworkInfo=0f;
            sendEntityEvents();
        } else {
            sendNetworkInfo+=dt;
        }
        grenadeStillAlive(dt);
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
        for (int id : opponents.keys()) {
            opponents.get(id).render(sb);
        }
        for (int id : enemyGrenades.keys()) {
            enemyGrenades.get(id).render(sb);
        }
        for (int id : myGrenades.keys()) {
            myGrenades.get(id).render(sb);
        }
        for (int id : opEntities.keys()){
            opEntities.get(id).render(sb);
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
