package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
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
import com.game.bb.entities.SPOpponent;
import com.game.bb.entities.SPPlayer;
import com.game.bb.entities.SPPower;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.HUD;
import com.game.bb.handlers.MapBuilder;
import com.game.bb.handlers.PowerupHandler;
import com.game.bb.handlers.PowerupSpawner;
import com.game.bb.handlers.SPContactListener;
import com.game.bb.handlers.SPInput;
import com.game.bb.handlers.Tools;
import com.game.bb.handlers.WeaponHandler;
import com.game.bb.handlers.pools.Pooler;
import com.game.bb.net.client.GameClient;
import com.game.bb.net.packets.EntityCluster;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;
import com.game.bb.net.packets.TCPEventPacket;

/**
 * TODO LIST --
 * - Try to manage the interpolation
 */
public class PlayState extends GameState {

    private Box2DDebugRenderer b2dr;
    private OrthographicCamera b2dCam;
    private IntMap<SPOpponent> opponents;
    private Array<Vector2> spawnLocations;
    private PowerupSpawner powerupSpawner;
    private WeaponHandler weapons;
    private IntMap<Integer> opponentEntitySequence;
    private IntMap<Array<String>> killedByEntity;
    private IntMap<EnemyEntity> opEntities = new IntMap<EnemyEntity>();
    private float respawnTimer = 0, sendPlayerInfo = 0f;
    private TCPEventPacket gameOverPacket;
    private HUD hud;
    private Texture backGround = Assets.getBackground();
    private float[] touchNbrs = {(cam.viewportWidth / 5), cam.viewportWidth * 4 / 5};
    private int playerPktSequence = 0;
    private boolean debugClick = false, hosting = false,
            removeMeMessageSent = false, debuggingMode = false, gameOverReceived = false,
            mapHasBeenCreated = false;

    public int currentTexture = SPOpponent.STAND_LEFT;
    public World world;
    public static PlayState playState;
    public float lastJumpDirection = 1;
    public GameClient client;
    public PowerupHandler powerHandler;
    public com.game.bb.handlers.MapBuilder map;
    public SPContactListener cl;
    public SPPlayer player;

    public PlayState(GameStateManager gsm, GameClient client) {
        super(gsm);

        playState = this;
        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());
        Tools.init();

        this.client = client;

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();
        weapons = new WeaponHandler(hud, this);

        powerHandler = new PowerupHandler(this);

        opponents = new IntMap<SPOpponent>();
        opponentEntitySequence = new IntMap<Integer>();

        killedByEntity = new IntMap<Array<String>>();


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, cam.viewportWidth / B2DVars.PPM, cam.viewportHeight / B2DVars.PPM);

    }

    private void createMap(){
        map = new MapBuilder(world, B2DVars.MAP_NBR, false);
        map.buildMap();
        spawnLocations = map.getSpawnLocations();
        if (gsm.isHosting()) {
            hosting = true;
            powerupSpawner = new PowerupSpawner(world, client, map.getMapWidth(), powerHandler);
        }
        mapHasBeenCreated = true;
    }

    private void createPlayer() {
        Vector2 spawn = spawnLocations.random();
        player = new SPPlayer(world, spawn.x, spawn.y, B2DVars.MY_ID, B2DVars.MY_COLOR);
        TCPEventPacket packet = new TCPEventPacket();
        packet.action = B2DVars.NET_CONNECT;
        packet.pos = spawn;
        packet.miscString = player.getColor();
        packet.id = player.getId();
        client.sendTCP(packet);
        killedByEntity.put(player.getId(), new Array<String>());
        float camX = player.getPosition().x * B2DVars.PPM;
        if ((camX + cam.viewportWidth / 2) > map.getMapWidth()) {
            cam.position.x = map.getMapWidth() - cam.viewportWidth / 2;
        } else if ((camX - cam.viewportWidth / 2) < 0) {
            cam.position.x = 0 + cam.viewportWidth / 2;
        } else {
            cam.position.x = camX;
        }
        hud.setMyNewColor(B2DVars.MY_COLOR);
    }

    public void handleInput() {
        if (SPInput.isPressed(SPInput.BUTTON_RIGHT)  ||
                SPInput.isPressed() && SPInput.x > touchNbrs[1] ) {
            SPInput.down = false;
            if (!player.isDead()) {
                    lastJumpDirection = 1;
                if (cl.canJump()) {
                    player.jump(B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
                } else {
                    currentTexture = SPPlayer.STAND_RIGHT;
                }
            }
        }
        if (SPInput.isPressed(SPInput.BUTTON_LEFT) || SPInput.isPressed() && SPInput.x < touchNbrs[0] ) {
            SPInput.down = false;
            if (!player.isDead()) {
                    lastJumpDirection = -1;
                if (cl.canJump()) {
                    player.jump(-B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
                } else {
                    currentTexture = SPPlayer.STAND_LEFT;
                }
            }
        }
        if (SPInput.isPressed(SPInput.BUTTON_W) ||
                SPInput.isPressed() && SPInput.x > touchNbrs[0] && SPInput.x < touchNbrs[1]) {
            SPInput.down = false;
            if (!player.isDead())
                weapons.shoot();
        }
        if (SPInput.isPressed(SPInput.BUTTON_E)) {
            if (!player.isDead())
                weapons.throwGrenade();
        }
        if (SPInput.isPressed(SPInput.BUTTON_Y)) {
            System.out.println("Debug is clicked! printing the next debug event.");
            System.out.println("CurrentTime: " + TimeUtils.millis() + "\t" + System.currentTimeMillis());
            debugClick = true;
            debuggingMode = !debuggingMode;
            System.out.println("CURRENT TIME: " + TimeUtils.millis() + " SystemCurrentTime: " + System.currentTimeMillis());
            System.out.println("HudCam pos: " + hudCam.position.x + " cam pos: " + cam.position.x);
        }
    }

    private void opponentTCPEvents() {
        for (TCPEventPacket pkt : client.getTCPEventPackets()) {
            switch (pkt.action) {
                case B2DVars.NET_SERVER_INFO:
                    B2DVars.setMyId(pkt.id);
                    B2DVars.setMyColor(pkt.color);
                    B2DVars.setMapNbr(pkt.misc);
                    createMap();
                    createPlayer();
                    break;
                case B2DVars.NET_CONNECT:
                    if (!opponents.containsKey(pkt.id)) {
                        SPOpponent opponent = new SPOpponent(world, pkt.pos.x, pkt.pos.y, pkt.id, pkt.miscString);
                        opponents.put(pkt.id, opponent);
                        hud.setColorToId(pkt.id, pkt.miscString);
                        killedByEntity.put(pkt.id, new Array<String>());
                        opponentEntitySequence.put(pkt.id, 0);
                        TCPEventPacket packet = new TCPEventPacket();
                        packet.action = B2DVars.NET_CONNECT;
                        packet.pos = player.getPosition();
                        packet.id = player.getId();
                        packet.miscString = player.getColor();
                        hud.setOpponentDeath(pkt.id, B2DVars.AMOUNT_LIVES);
                        client.sendTCP(packet);
                    }
                    break;
                case B2DVars.NET_DISCONNECT:
                    if (opponents.containsKey(pkt.id)) {
                        //remove opponent
                        SPOpponent opponent = opponents.remove(pkt.id);
                        world.destroyBody(opponent.getBody());
                        //clear opponents score
                        killedByEntity.remove(pkt.id);
                        //remove all entities belonging to that opponent
                        for (IntMap.Keys it = opEntities.keys(); it.hasNext;){
                            int id = it.next();
                            if(pkt.id == Tools.getPlayerId(id)){
                                EnemyEntity e = opEntities.get(id);
                                if(e instanceof EnemyGrenade){
                                    Pooler.free((EnemyGrenade) e);
                                }
                                else if(e instanceof EnemyBullet){
                                    Pooler.free((EnemyBullet) e);
                                }
                            }

                        }
                        opponent.dispose();
                        //remove from hud
                        hud.removeOpponentDeathCount(pkt.id);
                    }
                    break;
                case B2DVars.NET_NEW_ENTITY:
                    if (pkt.miscString.equals("grenade")) {
                        EnemyGrenade grenade = Pooler.enemyGrenade();
                        grenade.setAnimation(pkt.color);
                        grenade.setId(pkt.id);
                        grenade.getBody().setTransform(pkt.pos, 0);
                        grenade.getBody().setLinearVelocity(pkt.force);
                        grenade.initInterpolator();
                        opEntities.put(pkt.id, grenade);
                    } else if (pkt.miscString.equals("bullet")) {
                        EnemyBullet bullet = Pooler.enemyBullet();
                        bullet.setAnimation(pkt.color);
                        bullet.setId(pkt.id);
                        bullet.getBody().setTransform(pkt.pos, 0);
                        bullet.getBody().setLinearVelocity(pkt.force);
                        bullet.initInterpolator();
                        opEntities.put(pkt.id, bullet);
                        Assets.getSound("lasershot").play();
                    }
                    break;
                case B2DVars.NET_DESTROY_BODY:
                    if (opEntities.containsKey(pkt.id)) {
                        EnemyEntity opEntity = opEntities.remove(pkt.id);
                        if (opEntity instanceof EnemyBullet)
                            Pooler.free((EnemyBullet) opEntity); // return it to the pool
                        else if (opEntity instanceof EnemyGrenade)
                            Pooler.free((EnemyGrenade) opEntity); // return it to the pool
                    } else if (weapons.isMyWeapon(pkt.id)) {
                        weapons.removeGrenade(pkt.id);
                    } else if (powerHandler.containsPower(pkt.id)) {
                        powerHandler.removePower(pkt.id);
                    }
                    break;
                case B2DVars.NET_DEATH:
                    if (opponents.containsKey(pkt.id)) {
                        if(pkt.misc3 == 0) {
                            opponents.get(pkt.id).setInvulnerable(2f);
                            hud.setOpponentDeath(pkt.id, pkt.misc);
                            if (pkt.misc2 == B2DVars.MY_ID) {
                                hud.addPlayerKill();
                            }
                        }
                    }
                    break;
                case B2DVars.NET_SPAWN_POWER:
                    powerHandler.addPower(pkt.id, new SPPower(world, pkt.pos.x, pkt.pos.y, pkt.id, pkt.misc));
                    break;
                case B2DVars.NET_APPLY_ANTIPOWER:
                    if (pkt.misc == B2DVars.POWERTYPE_TILTSCREEN) {
                        powerHandler.applyPowerup(pkt.misc);
                    } else if (pkt.misc == B2DVars.POWERTYPE_SHIELD) {
                        if (pkt.miscString.equals("applyShield")) {
                            opponents.get(pkt.id).applyShield();
                        } else if (pkt.miscString.equals("removeShield")) {
                            opponents.get(pkt.id).removeShield();
                        }
                    }
                    break;
                case B2DVars.NET_GAME_OVER:
                    gameOverPacket = pkt;
                    gameOverReceived = true;
                    break;
                case B2DVars.NET_REMOVE_ME:
                    opponents.get(pkt.id).getBody().setTransform(B2DVars.VOID_X, B2DVars.VOID_Y, 0);
                    break;
            }
        }
    }

    private void opponentEntityEvents() {
        Array<EntityCluster> packets = client.getEntityClusters();
        for (EntityCluster cluster : packets) {
            if (opponentEntitySequence.get(cluster.id) != null && cluster.seq > opponentEntitySequence.get(cluster.id)) {
                opponentEntitySequence.put(cluster.id, cluster.seq);
                for (EntityPacket pkt : cluster.pkts) {
                    if (opEntities.containsKey(pkt.id)) {
                        opEntities.get(pkt.id).addEntityPacket(pkt);
                    }
                }
            }
        }
    }

    private void opponentMovementEvents() {
        Array<PlayerMovementPacket> packets = client.getOpponentMovements();
        for (PlayerMovementPacket pkt : packets) {
            if (opponents.containsKey(pkt.id))
                opponents.get(pkt.id).move(pkt.xp, pkt.yp, pkt.xv, pkt.yv, pkt.tex, pkt.sound, pkt.seq);
        }
    }

    private void respawnPlayer() {
        if (hud.getPlayerDeathCount() != 0) {
            Vector2 spawnLoc = spawnLocations.random();
            respawnTimer = 0;
            player.revive(spawnLoc,lastJumpDirection);
            weapons.refresh();
            cl.resetJumps();
            cl.revivePlayer();
            float camX = player.getPosition().x * B2DVars.PPM;
            if ((camX + cam.viewportWidth / 2) > map.getMapWidth()) {
                cam.position.x = map.getMapWidth() - cam.viewportWidth / 2;
            } else if ((camX - cam.viewportWidth / 2) < 0) {
                cam.position.x = 0 + cam.viewportWidth / 2;
            } else {
                cam.position.x = camX;
            }
        } else if (!removeMeMessageSent) {
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

    private void sendPlayerInfo() {
        PlayerMovementPacket pkt = Pooler.playerMovementPacket(); //grab it from the pool
        pkt.xp = player.getPosition().x;
        pkt.yp = player.getPosition().y;
        pkt.xv = player.getBody().getLinearVelocity().x;
        pkt.yv = player.getBody().getLinearVelocity().y;
        pkt.seq = playerPktSequence++;
        pkt.sound = 0;
        if (!powerHandler.isGhosted()) {
            pkt.tex = currentTexture;
        } else {
            pkt.tex = SPPlayer.BLANK;
        }
        pkt.id = player.getId();
        pkt.time = TimeUtils.millis();
        client.sendUDP(pkt);
        Pooler.free(pkt); //return it to the pool
    }

    private void playerHit() {
        if (!player.isInvulnerable()) {
            if (!player.isDead()) {
                int id = cl.getKillingEntityID();
                if (!powerHandler.isShielded()) {
                    player.kill(1);
                    hud.addPlayerDeath();
                    powerHandler.removeGhost();
                }
                TCPEventPacket pkt = Pooler.tcpEventPacket(); // grab it from the pool
                pkt.action = B2DVars.NET_DESTROY_BODY;
                pkt.id = id;
                client.sendTCP(pkt);
                pkt.action = B2DVars.NET_DEATH;
                pkt.id = player.getId();
                pkt.misc = hud.getPlayerDeathCount();
                pkt.misc2 = Tools.getPlayerId(id);
                if(powerHandler.isShielded()) pkt.misc3 = 1;
                client.sendTCP(pkt);
                Pooler.free(pkt); //return it to the pool
                if (weapons.isMyWeapon(id)) {
                    weapons.removeGrenade(id);
                    if (!powerHandler.isShielded())
                        killedByEntity.get(B2DVars.MY_ID).add("grenade");
                } else if (opEntities.containsKey(id)) {
                    EnemyEntity entity = opEntities.remove(id);
                    if (entity instanceof EnemyGrenade) {
                        entity.getBody().setTransform(400f, 400f, 0); //Was not moved when freed, so done manually.
                        Pooler.free((EnemyGrenade) entity);
                        if (!powerHandler.isShielded())
                            killedByEntity.get(Tools.getPlayerId(id)).add("grenade");
                    } else if (entity instanceof EnemyBullet) {
                        entity.getBody().setTransform(400f, 400f, 0);
                        Pooler.free((EnemyBullet) entity);
                        if (!powerHandler.isShielded())
                            killedByEntity.get(Tools.getPlayerId(id)).add("bullet");
                    }
                }
            }
            if (powerHandler.isShielded())
                powerHandler.removeShield();
        }
    }

    private void updateCamPosition() {
        //if the player moves far right
        float camX = cam.position.x;
        if ((player.getPosition().x * B2DVars.PPM) > (camX + 200f)) {
            if ((camX + cam.viewportWidth / 2) < map.getMapWidth()) {
                cam.position.x = camX + 2f;
            }
            //if the player moves far left
        } else if ((player.getPosition().x * B2DVars.PPM) < (camX - 200f)) {
            if ((camX - cam.viewportWidth / 2) > 0) {
                cam.position.x = camX - 2f;
            }
        }
        cam.update();
    }

    @Override
    public void update(float dt) {
        world.step(dt, 6, 2);
        if (player != null) {
            player.update(dt);
            if (player.isDead()) {
                respawnTimer += dt;
                if (respawnTimer >= B2DVars.RESPAWN_TIME) {
                    respawnPlayer();
                }
            }
            if (sendPlayerInfo > B2DVars.MOVEMENT_UPDATE_FREQ) {
                sendPlayerInfo = 0f;
                if (hud.getPlayerDeathCount() != 0) {
                    sendPlayerInfo();
                }
            } else {
                sendPlayerInfo += dt;
            }
            updateCamPosition();
        }
        for (IntMap.Keys it = opponents.keys(); it.hasNext; ) {
            opponents.get(it.next()).update(dt);
        }
        for (IntMap.Keys it = opEntities.keys(); it.hasNext; ) {
            opEntities.get(it.next()).update(dt);
        }
        opponentTCPEvents();
        opponentEntityEvents();
        opponentMovementEvents();

        weapons.update(dt);
        if (cl.isPlayerHit()) {
            playerHit();
        }
        if (cl.bouncePlayer()) {
            player.bouncePlayer();
        }
        powerHandler.update(dt);
        if (!client.isConnected()) {
            gsm.setState(GameStateManager.HOST_OFFLINE);
        }
        if (hosting) {
            powerupSpawner.update(dt);
            if (hud.gameOver()) {
                TCPEventPacket pkt = Pooler.tcpEventPacket();
                pkt.action = B2DVars.NET_GAME_OVER;
                pkt.miscString = constructVictoryOrderString();
                client.sendTCP(pkt);
                gameOver(pkt);
                Pooler.free(pkt);
            }
        }
        if (gameOverReceived) {
            gameOver(gameOverPacket);
        }
        handleInput();
    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(backGround, cam.position.x - cam.viewportWidth / 2, 0);
        sb.end();
        if (mapHasBeenCreated) {
            map.render();
        }
        for (IntMap.Keys it = opEntities.keys(); it.hasNext; ) {
            opEntities.get(it.next()).render(sb);
        }
        for (IntMap.Keys it = opponents.keys(); it.hasNext; ) {
            opponents.get(it.next()).render(sb);
        }
        powerHandler.render(sb);
        weapons.render(sb);
        if (player != null) {
            player.render(sb);
        }
        sb.setProjectionMatrix(hudCam.combined);
        hud.render(sb);

        //below is debuggingMode stuff for hitboxes etc...
        if (debuggingMode) {
            b2dr.render(world, b2dCam.combined); // Debug renderer. Hitboxes etc...
        }
    }


    @Override
    public void dispose() {
        //Array<Body> bodies = new Array<Body>();
        //world.getBodies(bodies);
        //System.out.println("Amount of bodies in world: " + bodies.size + " disposing...");
        //for (Body b : bodies){
        //    world.destroyBody(b);
        //}
        //world.clearForces();
        Pooler.reset();
        world.dispose();
    }

    private void gameOver(TCPEventPacket pkt) {
        ArrayMap<String, Array<String>> temp = new ArrayMap<String, Array<String>>();
        for (IntMap.Keys it = killedByEntity.keys(); it.hasNext; ) {
            int id = it.next();
            if (id == player.getId()) {
                temp.put(B2DVars.MY_COLOR, new Array<String>());
                for (String weapon : killedByEntity.get(id)) {
                    temp.get(B2DVars.MY_COLOR).add(weapon);
                }
            } else {
                String color = opponents.get(id).getColor();
                temp.put(color, new Array<String>());
                for (String weapon : killedByEntity.get(id)) {
                    temp.get(color).add(weapon);
                }
            }
        }
        cam.setToOrtho(false);
        gsm.setVictoryOrder(pkt.miscString);
        gsm.setKilledByEntities(temp);
        gsm.setState(GameStateManager.GAME_OVER);
    }

    private String constructVictoryOrderString() {
        Array<Integer> temp = hud.getVictoryOrder();
        String victoryOrder = "";
        for (Integer id : temp) {
            if (id == player.getId()) {
                victoryOrder = player.getColor() + ":" + victoryOrder;
            } else {
                victoryOrder = opponents.get(id).getColor() + ":" + victoryOrder;
            }
        }
        return victoryOrder.substring(0, victoryOrder.length() - 1);
    }

    // following methods are various contains-checks and getters
    public boolean containsOpponentEntity(int id) {
        return opEntities.containsKey(id);
    }

    public IntMap<EnemyEntity> getOpponentEntities() {
        return opEntities;
    }
}
