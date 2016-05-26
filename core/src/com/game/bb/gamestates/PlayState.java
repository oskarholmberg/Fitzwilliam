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
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.game.bb.entities.EnemyBullet;
import com.game.bb.entities.EnemyEntity;
import com.game.bb.entities.EnemyGrenade;
import com.game.bb.entities.SPBullet;
import com.game.bb.entities.SPGrenade;
import com.game.bb.entities.SPOpponent;
import com.game.bb.entities.SPPower;
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
    private IntMap<SPOpponent> opponents;
    private Array<Vector2> spawnLocations;
    private PowerupSpawner powerupSpawner;
    private IntMap<SPPower> powerups;
    private IntMap<Array<String>> killedByEntity;
    private int entityAccum = 0;
    private int amntBullets = B2DVars.AMOUNT_BULLET, amntGrenades = B2DVars.AMOUNT_GRENADE;
    private float bulletRefresh, lastJumpDirection = 1, grenadeRefresh;
    private IntMap<SPSprite> myEntities = new IntMap<SPSprite>();
    private IntMap<EnemyEntity> opEntities = new IntMap<EnemyEntity>();
    private float respawnTimer = 0;
    private GameClient client;
    private HUD hud;
    private PowerupHandler powerHandler;
    private MapBuilder map;
    private IntArray removedIds;
    private Texture backGround = new Texture("images/spaceBackground.png");
    private Sound reloadSound = Gdx.audio.newSound(Gdx.files.internal("sfx/reload.wav"));
    private Sound emptyClipSound = Gdx.audio.newSound(Gdx.files.internal("sfx/emptyClip.wav"));
    private Sound laserShot = Gdx.audio.newSound(Gdx.files.internal("sfx/laser.wav"));
    private float[] touchNbrs = {(cam.viewportWidth/ 5), cam.viewportWidth * 4 / 5};
    private int entityPktSequence = 0, playerPktSequence = 0, myEntityId;
    private boolean  grenadesIsEmpty = false, debugClick = false, hosting = false,
            removeMeMessageSent = false;
    private float sendEntityInfo = 0f, sendPlayerInfo = 0f;

    public int currentTexture = SPOpponent.STAND_LEFT;
    public World world;
    public static PlayState playState;

    public PlayState(GameStateManager gsm, GameClient client) {
        super(gsm);

        playState = this;
        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        this.client = client;

        Pooler.init();

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();

        powerups = new IntMap<SPPower>();

        if (gsm.isHosting()){
            hosting=true;
            powerupSpawner = new PowerupSpawner(world, client);
        }

        powerHandler = new PowerupHandler();


        opponents = new IntMap<SPOpponent>();
        removedIds = new IntArray();

        map = new MapBuilder(world, 1);
        map.buildMap();
        spawnLocations = map.getSpawnLocations();
        killedByEntity = new IntMap<Array<String>>();

        //Players
        Vector2 spawn = spawnLocations.random();
        player = new SPPlayer(world, spawn.x, spawn.y, Integer.valueOf(B2DVars.MY_ID), "blue");
        TCPEventPacket packet = new TCPEventPacket();
        packet.action = B2DVars.NET_CONNECT;
        packet.pos = spawn;
        packet.id = player.getId();
        client.sendTCP(packet);
        killedByEntity.put(player.getId(), new Array<String>());


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
                TCPEventPacket pkt = Pooler.tcpEventPacket();
                pkt.id=bullet.getId();
                pkt.action=B2DVars.NET_NEW_ENTITY;
                pkt.miscString="bullet";
                pkt.pos=bullet.getBody().getPosition();
                pkt.force=bullet.getBody().getLinearVelocity();
                client.sendTCP(pkt);
                Pooler.free(pkt);
            }
        }
    }

    private void throwGrenade() {
        if (amntGrenades > 0 && !player.isDead()) {
            int id = newEntityID();
            Vector2 pos = player.getPosition();
            SPGrenade spg = new SPGrenade(world, pos.x, pos.y, lastJumpDirection, false, id);
            myEntities.put(id, spg);
            amntGrenades--;
            hud.setAmountGrenadesLeft(amntGrenades);
            TCPEventPacket pkt = Pooler.tcpEventPacket();
            pkt.id=spg.getId();
            pkt.action=B2DVars.NET_NEW_ENTITY;
            pkt.miscString="grenade";
            pkt.pos=spg.getBody().getPosition();
            pkt.force=spg.getBody().getLinearVelocity();
            client.sendTCP(pkt);
            Pooler.free(pkt);
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
        }
         if (SPInput.isPressed(SPInput.BUTTON_LEFT) && cl.canJump() ||
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
        if (SPInput.isPressed(SPInput.BUTTON_Y)) {
            System.out.println("Debug is clicked! printing the next debug event.");
            debugClick = true;
            System.out.println(killedByEntity.toString());
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
                    killedByEntity.put(pkt.id, new Array<String>());
                    TCPEventPacket packet = new TCPEventPacket();
                    packet.action=B2DVars.NET_CONNECT;
                    packet.pos=player.getPosition();
                    packet.id=player.getId();
                    hud.setOpponentDeath(pkt.id, B2DVars.AMOUNT_LIVES);
                    client.sendTCP(packet);
                }
                break;
            case B2DVars.NET_DISCONNECT:
                if(opponents.containsKey(pkt.id)){
                    SPOpponent opponent = opponents.remove(pkt.id);
                    world.destroyBody(opponent.getBody());
                    opponent.dispose();
                    hud.removeOpponentDeathCount(pkt.id);
                }
                break;
            case B2DVars.NET_NEW_ENTITY:
                if (pkt.miscString.equals("grenade")){
                    EnemyGrenade grenade = Pooler.enemyGrenade();
                    grenade.setAnimation("red");
                    grenade.setId(pkt.id);
                    grenade.getBody().setTransform(pkt.pos, 0);
                    grenade.getBody().setLinearVelocity(pkt.force);
                    grenade.initInterpolator();
                    opEntities.put(pkt.id, grenade);
                } else if (pkt.miscString.equals("bullet")){
                    EnemyBullet bullet = Pooler.enemyBullet();
                    bullet.setAnimation("red");
                    bullet.setId(pkt.id);
                    bullet.getBody().setTransform(pkt.pos, 0);
                    bullet.getBody().setLinearVelocity(pkt.force);
                    bullet.initInterpolator();
                    opEntities.put(pkt.id, bullet);
                    laserShot.play();
                }
                break;
            case B2DVars.NET_DESTROY_BODY:
                if(opEntities.containsKey(pkt.id)){
                    removedIds.add(pkt.id);
                    EnemyEntity opEntity = opEntities.remove(pkt.id);
                    if (opEntity instanceof EnemyBullet)
                        Pooler.free((EnemyBullet) opEntity); // return it to the pool
                    else if (opEntity instanceof EnemyGrenade)
                        Pooler.free((EnemyGrenade) opEntity); // return it to the pool
                } else if (myEntities.containsKey(pkt.id)){
                    SPSprite myEntity = myEntities.remove(pkt.id);
                    world.destroyBody(myEntity.getBody());
                    myEntity.dispose();
                } else if (powerups.containsKey(pkt.id)){
                    SPPower powerup = powerups.remove(pkt.id);
                    world.destroyBody(powerup.getBody());
                    powerup.dispose();
                }
                break;
            case B2DVars.NET_DEATH:
                if (opponents.containsKey(pkt.id)){
                    hud.setOpponentDeath(pkt.id, pkt.misc);
                }
                break;
            case B2DVars.NET_POWER:
                switch (pkt.misc) {
                    case B2DVars.POWERTYPE_AMMO:
                        powerups.put(pkt.id, new SPPower(world, pkt.pos.x, pkt.pos.y, pkt.id, pkt.misc));
                        break;
                    case B2DVars.POWERTYPE_TILTSCREEN:
                        powerups.put(pkt.id, new SPPower(world, pkt.pos.x, pkt.pos.y, pkt.id, pkt.misc));
                        break;
                }
                break;
            case B2DVars.NET_APPLY_ANTIPOWER:
                if (pkt.misc==B2DVars.POWERTYPE_TILTSCREEN) {
                    powerHandler.applyPowerup(pkt.misc);
                }
                break;
            case B2DVars.NET_GAME_OVER:
                gameOver(pkt);
                break;
            case B2DVars.NET_REMOVE_ME:
                opponents.get(pkt.id).getBody().setTransform(B2DVars.VOID_X, B2DVars.VOID_Y, 0);
                break;
        }
    }

    private void opponentEntityEvents(){
        Array<EntityCluster> packets = client.getEntityClusters();
        for (EntityCluster cluster : packets) {
            float timeDiff = TimeUtils.millis() - cluster.time;
            if (debugClick){
                debugClick=false;
                System.out.println("The delay between the packets are: " + (TimeUtils.millis() - cluster.time) + " timeDiff: " + timeDiff);
            }
            for (EntityPacket pkt : cluster.pkts) {
                if (opEntities.containsKey(pkt.id)) {
                    opEntities.get(pkt.id).updateEntityState(pkt);
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
        if (hud.getPlayerDeathCount() != 0) {
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
        } else if (!removeMeMessageSent){
            player.dispose();
            player.getBody().setTransform(B2DVars.VOID_X, B2DVars.VOID_Y, 0);
            TCPEventPacket pkt = Pooler.tcpEventPacket();
            pkt.action = B2DVars.NET_REMOVE_ME;
            pkt.id = player.getId();
            client.sendTCP(pkt);
            Pooler.free(pkt);
            removeMeMessageSent = true;
        }
    }

    private void refreshAmmo(float dt) {
        if (powerHandler.unlimitedAmmo()){
            amntBullets = B2DVars.AMOUNT_BULLET;
            amntGrenades = B2DVars.AMOUNT_GRENADE;
            hud.setAmountBulletsLeft(amntBullets);
            hud.setAmountGrenadesLeft(amntGrenades);
        } else {
            if (bulletRefresh > 3f && amntBullets == 0) {
                amntBullets = B2DVars.AMOUNT_BULLET;
                bulletRefresh = 0;
                hud.setAmountBulletsLeft(amntBullets);
                reloadSound.play();
            } else if (amntBullets == 0) {
                bulletRefresh += dt;
            }
            if (grenadeRefresh > 8f && grenadesIsEmpty) {
                amntGrenades = B2DVars.AMOUNT_GRENADE;
                grenadesIsEmpty = false;
                hud.setAmountGrenadesLeft(amntGrenades);
            } else if (amntGrenades == 0) {
                grenadeRefresh += dt;
            }
        }
    }

    private void bulletsHittingWall() {
        for (int id : cl.getIdsToRemove()){
            if (myEntities.containsKey(id)){
                TCPEventPacket pkt = Pooler.tcpEventPacket();
                pkt.id=id;
                pkt.action=B2DVars.NET_DESTROY_BODY;
                client.sendTCP(pkt);
                Pooler.free(pkt);
                SPSprite bullet = myEntities.remove(id);
                world.destroyBody(bullet.getBody());
                bullet.dispose();
            } else if (opEntities.containsKey(id)){
                Pooler.free((EnemyBullet) opEntities.remove(id));
            }
        }
        cl.clearIdList();
    }

    private void sendEntityEvents(){
        if (myEntities.size>0) {
            EntityPacket[] packets = new EntityPacket[myEntities.size];
            int index = 0;
            for (IntMap.Keys it = myEntities.keys(); it.hasNext;) {
                int id = it.next();
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
            cluster.time = TimeUtils.millis();
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
        pkt.id=player.getId();
        pkt.time = TimeUtils.millis();
        client.sendUDP(pkt);
        Pooler.free(pkt); //return it to the pool
    }

    private void checkGrenadeTimer(float dt) {
        for (IntMap.Keys it = myEntities.keys(); it.hasNext;) {
            int id = it.next();
            if (myEntities.get(id) instanceof SPGrenade) {
                if (myEntities.get(id) != null && ((SPGrenade) myEntities.get(id)).lifeTimeReached(dt)) {
                    TCPEventPacket pkt = Pooler.tcpEventPacket(); // grab it from the pool
                    pkt.id = id;
                    pkt.action = B2DVars.NET_DESTROY_BODY;
                    client.sendTCP(pkt);
                    Pooler.free(pkt); // return it to the pool
                    SPSprite grenade = myEntities.remove(id);
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
            pkt.id = player.getId();
            pkt.misc = hud.getPlayerDeathCount();
            client.sendTCP(pkt);
            Pooler.free(pkt); //return it to the pool
            if (myEntities.containsKey(id)){
                SPSprite entity = myEntities.remove(id);
                world.destroyBody(entity.getBody());
                entity.dispose();
                killedByEntity.get(player.getId()).add("grenade");
            } else if (opEntities.containsKey(id)){
                EnemyEntity entity = opEntities.remove(id);
                removedIds.add(id);
                if (entity instanceof EnemyGrenade) {
                    entity.getBody().setTransform(400f, 400f, 0);
                    Pooler.free((EnemyGrenade) entity);
                    killedByEntity.get(Tools.getPlayerId(id)).add("grenade");
                } else if (entity instanceof EnemyBullet) {
                    entity.getBody().setTransform(400f, 400f, 0);
                    Pooler.free((EnemyBullet) entity);
                    killedByEntity.get(Tools.getPlayerId(id)).add("bullet");
                }
            }
        }
    }

    private void powerupTaken(){
        if (cl.powerTaken()){
            SPPower power = powerups.remove(cl.getLastPowerTaken().getId());
            int powerType = power.getPowerType();
            world.destroyBody(power.getBody());
            power.dispose();

            TCPEventPacket pkt = Pooler.tcpEventPacket();
            pkt.action = B2DVars.NET_DESTROY_BODY;
            pkt.id = power.getId();
            client.sendTCP(pkt);
            Pooler.free(pkt);

            switch (powerType){
                case B2DVars.POWERTYPE_AMMO:
                    powerHandler.applyPowerup(powerType);
                    break;
                case B2DVars.POWERTYPE_TILTSCREEN:
                    TCPEventPacket pkt2 = Pooler.tcpEventPacket();
                    pkt2.action = B2DVars.NET_APPLY_ANTIPOWER;
                    pkt2.misc = powerType;
                    client.sendTCP(pkt2);
                    Pooler.free(pkt2);
            }
        }
    }

    public void addPowerup(SPPower power, int id){
        powerups.put(id, power);
    }

    private void updateCamPosition(){
        //if the player moves far right
        float camX = cam.position.x;
        if ((player.getPosition().x * B2DVars.PPM) > (camX + 200f)) {
            if ((camX + cam.viewportWidth/2) < map.getMapWidth() ) {
                cam.position.x = camX + 2f;
            }
            //if the player moves far left
        } else if ((player.getPosition().x * B2DVars.PPM) < (camX - 200f)) {
            if ((camX - cam.viewportWidth / 2) > 0) {
                cam.position.x = camX - 2f;
                System.out.println(camX + cam.viewportWidth / 2);
                System.out.println("Cam position: " + cam.position.x);
            }
        }
        cam.update();
    }

    @Override
    public void update(float dt) {
        if(!client.isConnected()){
            gsm.setState(GameStateManager.HOST_OFFLINE);
        }
        world.step(dt, 6, 2);
        player.update(dt);
        for (IntMap.Keys it = opponents.keys(); it.hasNext;) {
            opponents.get(it.next()).update(dt);
        }
        for (IntMap.Keys it = myEntities.keys(); it.hasNext;) {
            myEntities.get(it.next()).update(dt);
        }
        for (IntMap.Keys it = opEntities.keys(); it.hasNext;){
            opEntities.get(it.next()).update(dt);
        }
        for (IntMap.Keys it = powerups.keys(); it.hasNext;){
            powerups.get(it.next()).update(dt);
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
        if (sendEntityInfo > B2DVars.ENTITY_UPDATE_FREQ){
            sendEntityInfo =0f;
            sendEntityEvents();
        } else {
            sendEntityInfo +=dt;
        }
        if (sendPlayerInfo > B2DVars.MOVEMENT_UPDATE_FREQ){
            sendPlayerInfo = 0f;
            if (hud.getPlayerDeathCount()!=0) {
                sendPlayerInfo();
            }
        } else {
            sendPlayerInfo+=dt;
        }
        checkGrenadeTimer(dt);
        bulletsHittingWall();
        powerupTaken();
        powerHandler.update(dt);
        if (hosting){
            powerupSpawner.update(dt);
            if (hud.gameOver()){
                TCPEventPacket pkt = Pooler.tcpEventPacket();
                pkt.action = B2DVars.NET_GAME_OVER;
                pkt.miscString = constructVictoryOrderString();
                client.sendTCP(pkt);
                gameOver(pkt);
                Pooler.free(pkt);
            }
        }
        handleInput();
        updateCamPosition();
    }

    @Override
    public void render() {
        //Clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(backGround, cam.position.x - cam.viewportWidth / 2, 0);
        sb.end();
        map.render();
        for (IntMap.Keys it = myEntities.keys(); it.hasNext;) {
            myEntities.get(it.next()).render(sb);
        }
        for (IntMap.Keys it = opEntities.keys(); it.hasNext;){
            opEntities.get(it.next()).render(sb);
        }
        for (IntMap.Keys it = opponents.keys(); it.hasNext;){
            opponents.get(it.next()).render(sb);
        }
        for (IntMap.Keys it = powerups.keys(); it.hasNext;){
            powerups.get(it.next()).render(sb);
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

    private void gameOver(TCPEventPacket pkt){
        ArrayMap<String, Array<String>> temp = new ArrayMap<String, Array<String>>();
        for (IntMap.Keys it = killedByEntity.keys(); it.hasNext;){
            int id = it.next();
            if (id == player.getId()){
                temp.put("blue", new Array<String>());
                for (String weapon : killedByEntity.get(id)){
                    temp.get("blue").add(weapon);
                }
            } else {
                String color = opponents.get(id).getColor();
                temp.put(color, new Array<String>());
                for (String weapon : killedByEntity.get(id)){
                    temp.get(color).add(weapon);
                }
            }
        }
        gsm.setVictoryOrder(pkt.miscString);
        gsm.setKilledByEntities(temp);
        gsm.setState(GameStateManager.GAME_OVER);
        dispose();
    }

    private String constructVictoryOrderString(){
        Array<Integer> temp = hud.getVictoryOrder();
        String victoryOrder = "";
        for (Integer id : temp){
            if (id == player.getId()){
                victoryOrder = player.getColor() + ":" + victoryOrder;
            } else {
                victoryOrder = opponents.get(id).getColor() + ":" + victoryOrder;
            }
        }
        return victoryOrder.substring(0, victoryOrder.length()-1);
    }

    // following methods are various contains-checks and getters
    public boolean containsOpponentEntity(int id){
        return opEntities.containsKey(id);
    }
    public IntMap<EnemyEntity> getOpponentEntities(){
        return opEntities;
    }
    public boolean containsMyEntity(int id){
        return myEntities.containsKey(id);
    }
    public IntMap<SPSprite> getMyEntities(){
        return myEntities;
    }
    public boolean containsPowerup(int id){
        return powerups.containsKey(id);
    }
    public IntMap<SPPower> getPowerups(){
        return powerups;
    }
}
