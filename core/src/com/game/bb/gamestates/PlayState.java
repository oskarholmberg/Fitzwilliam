package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
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
    private float[] touchNbrs = {(B2DVars.CAM_WIDTH / 5), B2DVars.CAM_WIDTH * 4 / 5};

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tmr;

    public PlayState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new SPContactListener());

        b2dr = new Box2DDebugRenderer();

        hud = new HUD();
        System.out.println("touchnbrs0 " + touchNbrs[0] + " touchnbrs1 " + touchNbrs[1]);

        bullets = new Array<SPBullet>();
        opponents = new Array<SPPlayer>();
        // create boundaries
        createBoundary(cam.viewportWidth / 2, cam.viewportHeight, cam.viewportWidth / 2, 5); //top
        createBoundary(cam.viewportWidth / 2, 0, cam.viewportWidth / 2, 5); //bottom
        createBoundary(0, cam.viewportHeight / 2, 5, cam.viewportHeight / 2); // left
        createBoundary(cam.viewportWidth, cam.viewportHeight / 2, 5, cam.viewportHeight / 2); // right

        //Vectors for handling android touch input
        buildMap();

        //Players
        player = new SPPlayer(world, B2DVars.MY_ID, B2DVars.CAM_WIDTH / 2 / B2DVars.PPM, B2DVars.CAM_HEIGHT/B2DVars.PPM, B2DVars.BIT_PLAYER, B2DVars.ID_PLAYER, "blue");


        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, B2DVars.CAM_WIDTH / B2DVars.PPM, B2DVars.CAM_HEIGHT / B2DVars.PPM);
    }

    private void buildMap(){
        tiledMap = new TmxMapLoader().load("maps/Moon.tmx");
        tmr = new OrthogonalTiledMapRenderer(tiledMap);
        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get("moonBlocks");

        float ts = layer.getTileWidth();
        for(int row = 0; row < layer.getHeight(); row++) {
            for(int col = 0; col < layer.getWidth(); col++) {
                // get cell
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);

                // check that there is a cell
                if(cell == null) continue;
                if(cell.getTile() == null) continue;

                // create body from cell
                BodyDef bdef = new BodyDef();
                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set((col + 0.5f) * ts / B2DVars.PPM, (row + 0.5f) * ts / B2DVars.PPM);
                ChainShape cs = new ChainShape();
                Vector2[] v = new Vector2[3];
                v[0] = new Vector2(-ts / 2 / B2DVars.PPM, -ts / 2 / B2DVars.PPM);
                v[1] = new Vector2(-ts / 2 / B2DVars.PPM, ts / 2 / B2DVars.PPM);
                v[2] = new Vector2(ts / 2 / B2DVars.PPM, ts / 2 / B2DVars.PPM);
                cs.createChain(v);
                FixtureDef fd = new FixtureDef();
                fd.shape = cs;
                fd.filter.categoryBits = B2DVars.BIT_GROUND;
                fd.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_BULLET | B2DVars.BIT_OPPONENT;
                world.createBody(bdef).createFixture(fd).setUserData(B2DVars.ID_GROUND);
                cs.dispose();

            }
        }
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
        fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_OPPONENT | B2DVars.BIT_BULLET;
        body.createFixture(fdef).setUserData(B2DVars.ID_GROUND);
    }

    public void shoot() {
        if (amntBullets > 0 && !player.isDead()) {
            Vector2 pos = player.getPosition();
            SPBullet bullet = new SPBullet(world, pos.x, pos.y, lastJumpDirection, false);
            bullets.add(bullet);
            gsm.addAction(B2DVars.MY_ID + ":SHOOT:0:0:" + pos.x + ":" + pos.y + ":" + lastJumpDirection);
            amntBullets--;
        }
    }

    public void opponentShot(float xPos, float yPos, float dir) {
        SPBullet bullet = new SPBullet(world, xPos, yPos, dir, true);
        bullets.add(bullet);
    }


    public void handleInput() {

        if (SPInput.isPressed(SPInput.BUTTON_RIGHT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x > touchNbrs[1] && cl.canJump()) {
            SPInput.down = false;
            player.jump(B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
            gsm.addAction(B2DVars.MY_ID + ":MOVE:" + B2DVars.PH_JUMPX + ":" + B2DVars.PH_JUMPY + ":" + player.getPosition().x + ":" + player.getPosition().y);
            lastJumpDirection = 1;
        }
        if (SPInput.isPressed(SPInput.BUTTON_LEFT) && cl.canJump() ||
                SPInput.isPressed() && SPInput.x < touchNbrs[0] && cl.canJump()) {
            SPInput.down = false;
            player.jump(-B2DVars.PH_JUMPX, B2DVars.PH_JUMPY, player.getPosition().x, player.getPosition().y);
            gsm.addAction(B2DVars.MY_ID + ":MOVE:" + -B2DVars.PH_JUMPX + ":" + B2DVars.PH_JUMPY + ":" + player.getPosition().x + ":" + player.getPosition().y);
            lastJumpDirection = -1;
        }
        if (SPInput.isPressed(SPInput.BUTTON_W) ||
                SPInput.isPressed() && SPInput.x > touchNbrs[0] && SPInput.x < touchNbrs[1]) {
            SPInput.down = false;
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
                opponentShot(Float.valueOf(action[4]), Float.valueOf(action[5]), Float.valueOf(action[6]));
            else if (action[1].equals("DEATH") && opponent != null) {
                hud.setOpponentDeath(action[1], action[2]);
                opponent.kill();
            } else if (action[1].equals("RESPAWN") && opponent != null) {
                opponent.revive();
                opponent.jump(Float.valueOf(action[2]), Float.valueOf(action[3]),
                        Float.valueOf(action[4]), Float.valueOf(action[5]));
            } else if (action[1].equals("CONNECT")) {
                if (getOpponent(action[0]) == null) {
                    System.out.println("Opponent added at: " + action[4] + ":" + action[5]);
                    opponents.add(new SPPlayer(world, action[0], Float.valueOf(action[4]), Float.valueOf(action[5]), Short.valueOf(action[6]), action[7], action[8]));
                }
            } else if (action[1].equals("DISCONNECT")) {
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
        player.jump(0, 0, B2DVars.CAM_WIDTH / 2 / B2DVars.PPM, B2DVars.CAM_HEIGHT/B2DVars.PPM);
        gsm.addAction(B2DVars.MY_ID + ":RESPAWN:0:0:" + player.getPosition().x + ":" + player.getPosition().y);
        cl.resetJumps();
        cl.revivePlayer();
    }

    private void refreshBullets(float dt) {
        if (bulletRefresh > 5f) {
            amntBullets = 5;
            bulletRefresh = 0;
        } else {
            bulletRefresh += dt;
        }
    }

    public void removeDeadBodies() {
        for (Body b : cl.getBodiesToRemove()) {
            bullets.removeValue((SPBullet) b.getUserData(), true);
            world.destroyBody(b);
        }
        cl.clearBulletList();
    }

    private void playerHit() {
        if (!player.isDead()) {
            player.kill();
            hud.addPlayerDeath();

            //In this addAction add the ID of the killing bullet last
            gsm.addAction(B2DVars.MY_ID + ":DEATH:" + hud.getDeathCount() + ":0:" + player.getPosition().x + ":" + player.getPosition().y);
        }
    }


    @Override
    public void update(float dt) {
        handleInput();
        world.step(dt, 6, 2);
        //removeDeadBodies();
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
        //b2dr.render(world, b2dCam.combined); // Debug renderer. Hitboxes etc...
        //sb.setProjectionMatrix(cam.combined);
    }

    @Override
    public void dispose() {

    }
}
