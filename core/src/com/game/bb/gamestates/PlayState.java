package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.game.bb.entities.EnemyBullet;
import com.game.bb.entities.EnemyEntity;
import com.game.bb.entities.EnemyGrenade;
import com.game.bb.entities.SPBullet;
import com.game.bb.entities.SPGrenade;
import com.game.bb.entities.SPOpponent;
import com.game.bb.entities.SPSprite;
import com.game.bb.handlers.*;
import com.game.bb.entities.SPPlayer;
import com.game.bb.handlers.pools.Pooler;
import com.game.bb.net.client.GameClient;
import com.game.bb.net.packets.EntityCluster;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;
import com.game.bb.net.packets.TCPEventPacket;


/**
 * TODO LIST --
 *  - Try to manage the interpolation
 *
 */
public class PlayState extends GameState {

    private Box2DDebugRenderer b2dr;
    private OrthographicCamera b2dCam;
    private SPContactListener cl;
    private SPPlayer player;
    private ArrayMap<Integer, SPOpponent> opponents;
    private Array<Vector2> spawnLocations;
    private int entityAccum = 0;
    private int amntBullets = B2DVars.AMOUNT_BULLET, amntGrenades = B2DVars.AMOUNT_GRENADE;
    private float bulletRefresh, lastJumpDirection = 1, grenadeRefresh;
    private ArrayMap<Integer, SPSprite> myEntities = new ArrayMap<Integer, SPSprite>();
    private ArrayMap<Integer, EnemyEntity> opEntities = new ArrayMap<Integer, EnemyEntity>();
    private float respawnTimer = 0;
    private GameClient client;
    private HUD hud;
    private MapBuilder map;
    private Texture backGround = new Texture("images/spaceBackground.png");
    private Sound reloadSound = Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav"));
    private Sound emptyClipSound = Gdx.audio.newSound(Gdx.files.internal("sfx/emptyClip.wav"));
    private float[] touchNbrs = {(cam.viewportWidth/ 5), cam.viewportWidth * 4 / 5};
    private int entityPktSequence = 0, playerPktSequence = 0;
    private boolean  grenadesIsEmpty = false;
    private float sendNetworkInfo = 0f, sendPlayerInfo = 0f;

    public int currentTexture = SPOpponent.STAND_LEFT;
    public World world;
    public static PlayState playState;

    public PlayState(GameStateManager gsm) {
        super(gsm);

        playState = this;
        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        client = new GameClient();
        client.connectToServer(0);

        Pooler.init();

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();

        opponents = new ArrayMap<Integer, SPOpponent>();
        // create boundaries

        map = new MapBuilder(world, 1);
        map.buildMap();
        spawnLocations = map.getSpawnLocations();

        //Players
        Vector2 spawn = spawnLocations.random();
        int tempEntityID = newEntityID();
        player = new SPPlayer(world, spawn.x, spawn.y, tempEntityID, "blue");
        TCPEventPacket packet = new TCPEventPacket();
        packet.action = B2DVars.NET_CONNECT;
        packet.pos = spawn;
        packet.id = tempEntityID;
        client.sendTCP(packet);


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, cam.viewportWidth / B2DVars.PPM, cam.viewportHeight / B2DVars.PPM);
    }


    public void shoot(){
        if(!player.isDead()){
            if(amntBullets==0){
                emptyClipSound.play();
            } else {
                int id = newEntityID();
                Vector2 pos = player.getPosition();
                SPBullet bullet = new SPBullet(world, pos.x, pos.y, lastJumpDirection, false, id);
                myEntities.put(id, bullet);
                amntBullets--;
                hud.setAmountBulletsLeft(amntBullets);
            }
        }
    }

    private void throwGrenade() {
        if (amntGrenades > 0 && !player.isDead()) {
            int ID = newEntityID();
            Vector2 pos = player.getPosition();
            SPGrenade spg = new SPGrenade(world, pos.x, pos.y, lastJumpDirection, false, ID);
            myEntities.put(ID, spg);
            amntGrenades--;
            hud.setAmountGrenadesLeft(amntGrenades);
            if (amntGrenades == 0) {
                grenadesIsEmpty = true;
                grenadeRefresh = 0;
            }
        }
    }

    public int newEntityID() {
        entityAccum++;
        String temp = B2DVars.MY_ID + entityAccum;
        return Integer.valueOf(temp);
    }


    public void handleInput() {
        if (SPInput.isPressed(SPInput.BUTTON_RIGHT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x > touchNbrs[1] && cl.canJump()) {
            SPInput.down = false;
            player.jump(B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
            lastJumpDirection = 1;
        } else if (SPInput.isPressed(SPInput.BUTTON_LEFT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x < touchNbrs[0] && cl.canJump()) {
            SPInput.down = false;
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
                    SPOpponent opponent = new SPOpponent(world, pkt.pos.x, pkt.pos.y, pkt.id, "red");
                    opponents.put(pkt.id, opponent);
                    TCPEventPacket packet = new TCPEventPacket();
                    packet.action=B2DVars.NET_CONNECT;
                    packet.pos=player.getPosition();
                    packet.id=player.getID();
                    client.sendTCP(packet);
                }
                break;
            case B2DVars.NET_DESTROY_BODY:
                if(opEntities.containsKey(pkt.id)){
                    System.out.println("Destroy oponent entity: " + pkt.id);
                    EnemyEntity opEntity = opEntities.removeKey(pkt.id);
                    if (opEntity instanceof EnemyBullet)
                        Pooler.free((EnemyBullet) opEntity); // return it to the pool
                    else if (opEntity instanceof EnemyGrenade)
                        Pooler.free((EnemyGrenade) opEntity); // return it to the pool
                } else if (myEntities.containsKey(pkt.id)){
                    System.out.println("Destroy my entity: " + pkt.id);
                    SPSprite myEntity = myEntities.removeKey(pkt.id);
                    world.destroyBody(myEntity.getBody());
                    myEntity.dispose();
                }
                break;
            case B2DVars.NET_DEATH:
                if (opponents.containsKey(pkt.id)){
                    hud.setOpponentDeath(pkt.id, pkt.misc);
                }
        }
    }

    private void opponentEntityEvents(){
        Array<EntityCluster> packets = client.getEntityClusters();
        for (EntityCluster cluster : packets) {
            for (EntityPacket pkt : cluster.pkts) {
                if (opEntities.containsKey(pkt.id)) {
                    Body b = opEntities.get(pkt.id).getBody();
                    b.setTransform(pkt.xp, pkt.yp, 0);
                    b.setLinearVelocity(pkt.xf, pkt.yf);
                } else {
                    if (pkt.type == B2DVars.TYPE_GRENADE) {
                        EnemyGrenade enemyGrenade = Pooler.enemyGrenade(); // get it from the pool
                        enemyGrenade.setAnimation("red");
                        enemyGrenade.setId(pkt.id);
                        enemyGrenade.getBody().setTransform(pkt.xp, pkt.yp, 0);
                        enemyGrenade.getBody().setLinearVelocity(pkt.xf, pkt.yf);
                        opEntities.put(pkt.id, enemyGrenade);
                    } else if (pkt.type == B2DVars.TYPE_BULLET) {
                        EnemyBullet enemyBullet = Pooler.enemyBullet(); // get it from the pool
                        enemyBullet.setAnimation("red");
                        enemyBullet.setId(pkt.id);
                        enemyBullet.getBody().setTransform(pkt.xp, pkt.yp, 0);
                        enemyBullet.getBody().setLinearVelocity(pkt.xf, pkt.yf);
                        opEntities.put(pkt.id, enemyBullet);
                    }
                }
            }
        }
    }

    private void opponentMovementEvents(){
        Array<PlayerMovementPacket> packets = client.getOpponentMovements();
        for (PlayerMovementPacket pkt : packets){
            if(opponents.containsKey(pkt.id))
            opponents.get(pkt.id).move(pkt.xp, pkt.yp, pkt.xv, pkt.yv, pkt.tex, pkt.sound);
        }
    }

    private void respawnPlayer() {
        Vector2 spawnLoc = spawnLocations.random();
        respawnTimer = 0;
        player.revive();
        player.jump(0, 0, spawnLoc.x, spawnLoc.y);
        amntBullets = B2DVars.AMOUNT_BULLET;
        amntGrenades = B2DVars.AMOUNT_GRENADE;
        hud.setAmountBulletsLeft(amntBullets);
        hud.setAmountGrenadesLeft(amntGrenades);
        cl.resetJumps();
        cl.revivePlayer();
    }

    private void refreshAmmo(float dt) {
        if (bulletRefresh > 3f && amntBullets==0) {
            amntBullets = B2DVars.AMOUNT_BULLET;
            bulletRefresh = 0;
            hud.setAmountBulletsLeft(amntBullets);
            reloadSound.play();
        } else if (amntBullets == 0){
            bulletRefresh += dt;
        }
        if (grenadeRefresh > 8f && grenadesIsEmpty) {
            amntGrenades = B2DVars.AMOUNT_GRENADE;
            grenadesIsEmpty = false;
            hud.setAmountGrenadesLeft(amntGrenades);
        } else if (amntGrenades == 0){
            grenadeRefresh += dt;
        }
    }

    private void bulletsHittingWall() {
        for (int id : cl.getIdsToRemove()){
            if (myEntities.containsKey(id)){
                TCPEventPacket packet = Pooler.tcpEventPacket(); //grab it from the pool
                packet.action=B2DVars.NET_DESTROY_BODY;
                packet.id=id;
                client.sendTCP(packet);
                Pooler.free(packet); // return it to the pool
                SPSprite bullet = myEntities.removeKey(id);
                world.destroyBody(bullet.getBody());
                System.out.println("Removing: EnemyBullet-wall");
                bullet.dispose();
            }
        }
        cl.clearIdList();
    }

    private void sendEntityEvents(){
        if (myEntities.size>0) {
            EntityPacket[] packets = new EntityPacket[myEntities.size];
            int index = 0;
            for (int id : myEntities.keys()) {
                EntityPacket pkt = Pooler.entityPacket(); // grab it from the pool
                Body b = myEntities.get(id).getBody();
                pkt.xp = b.getPosition().x;
                pkt.yp = b.getPosition().y;
                pkt.xf = b.getLinearVelocity().x;
                pkt.yf = b.getLinearVelocity().y;
                pkt.id = id;
                pkt.alive = 1;
                if (myEntities.get(id) instanceof SPGrenade)
                    pkt.type = B2DVars.TYPE_GRENADE;
                else if (myEntities.get(id) instanceof SPBullet)
                    pkt.type = B2DVars.TYPE_BULLET;
                packets[index] = pkt;
                index++;
            }
            EntityCluster cluster = Pooler.entityCluster(); // grab it from the pool
            cluster.seq = entityPktSequence++;
            cluster.pkts = packets;
            client.sendUDP(cluster);
            Pooler.free(packets); //return them to the pool
            Pooler.free(cluster);
        }
    }

    private void sendPlayerInfo(){
        PlayerMovementPacket pkt = Pooler.playerMovementPacket(); //grab it from the pool
        pkt.xp = player.getPosition().x;
        pkt.yp = player.getPosition().y;
        pkt.xv = player.getBody().getLinearVelocity().x;
        pkt.yv = player.getBody().getLinearVelocity().y;
        pkt.seq = playerPktSequence++;
        pkt.sound=0;
        pkt.tex = currentTexture;
        pkt.id=player.getID();
        client.sendUDP(pkt);
        Pooler.free(pkt); //return it to the pool
    }

    private void checkGrenadeTimer(float dt) {
        for (int id : myEntities.keys()) {
            if (myEntities.get(id) instanceof SPGrenade) {
                if (myEntities.get(id) != null && ((SPGrenade) myEntities.get(id)).lifeTimeReached(dt)) {
                    TCPEventPacket pkt = Pooler.tcpEventPacket(); // grab it from the pool
                    pkt.id = id;
                    pkt.action = B2DVars.NET_DESTROY_BODY;
                    client.sendTCP(pkt);
                    Pooler.free(pkt); // return it to the pool
                    SPSprite grenade = myEntities.removeKey(id);
                    grenade.dispose();
                    System.out.println("Removing: grenade-timer");
                    world.destroyBody(grenade.getBody());
                }
            }
        }
    }

    private void playerHit() {
        if (!player.isDead()) {
            int id = cl.getKillingEntityID();
            player.kill(1);
            hud.addPlayerDeath();
            TCPEventPacket pkt = Pooler.tcpEventPacket(); // grab it from the pool
            pkt.action=B2DVars.NET_DESTROY_BODY;
            pkt.id= id;
            client.sendTCP(pkt);
            pkt.action=B2DVars.NET_DEATH;
            pkt.id = player.getID();
            pkt.misc = hud.getPlayerDeathCount();
            client.sendTCP(pkt);
            Pooler.free(pkt); //return it to the pool
            if (myEntities.containsKey(id)){
                SPSprite entity = myEntities.removeKey(id);
                world.destroyBody(entity.getBody());
                entity.dispose();
                System.out.println("Removing: entity - hit");
            } else if (opEntities.containsKey(id)){
                EnemyEntity entity = opEntities.get(id);
                if (entity instanceof EnemyGrenade)
                    Pooler.free((EnemyGrenade) entity);
                else if (entity instanceof EnemyBullet)
                    Pooler.free((EnemyBullet) entity);
            }
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        world.step(dt, 6, 2);
        player.update(dt);
        for (int id : opponents.keys()) {
            opponents.get(id).update(dt);
        }
        for (int id : myEntities.keys()) {
            myEntities.get(id).update(dt);
        }
        for (int id : opEntities.keys()){
            opEntities.get(id).update(dt);
        }
        opponentTCPEvents();
        opponentEntityEvents();
        opponentMovementEvents();
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
        if (sendNetworkInfo > 1/5f){
            sendNetworkInfo=0f;
            sendEntityEvents();
        } else {
            sendNetworkInfo+=dt;
        }
        if (sendPlayerInfo > 1/30f){
            sendPlayerInfo = 0f;
            sendPlayerInfo();
        } else {
            sendPlayerInfo+=dt;
        }
        checkGrenadeTimer(dt);
        bulletsHittingWall();
    }

    @Override
    public void render() {
        //Clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(backGround, 0, 0);
        sb.end();
        map.render();
        for (int id : myEntities.keys()) {
            myEntities.get(id).render(sb);
        }
        for (int id : opEntities.keys()){
            opEntities.get(id).render(sb);
        }
        for (int id : opponents.keys()){
            opponents.get(id).render(sb);
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
