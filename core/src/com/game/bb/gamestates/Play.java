package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.handlers.*;
import com.game.bb.main.Game;
import com.game.bb.network.NetworkMonitor;
import com.game.bb.sprites.PongBall;
import com.game.bb.sprites.PongPaddle;

/**
 * Created by erik on 06/05/16.
 */
public class Play extends GameState {

    private World world;
    private Box2DDebugRenderer b2dr;
    private OrthographicCamera b2dCam;
    private PongContactListener cl;
    private NetworkMonitor mon;
    private PongPaddle playerPaddle, opponentPaddle;

    public Play(GameStateManager gsm){
        super(gsm);

        mon = gsm.getMonitor();
        world = new World(new Vector2(0, -7.81f), true);
        world.setContactListener(cl = new PongContactListener(mon));
        world.setVelocityThreshold(0.1f);

        b2dr = new Box2DDebugRenderer();

        // create boundaries
        createBoundary(cam.viewportWidth/2, cam.viewportHeight, cam.viewportWidth/2, 5); //top
        createBoundary(cam.viewportWidth/2, 0, cam.viewportWidth/2, 5); //bottom
        createBoundary(0, cam.viewportHeight/2, 5, cam.viewportHeight/2); // left
        createBoundary(cam.viewportWidth, cam.viewportHeight/2, 5, cam.viewportHeight/2); // right

        //Players

        playerPaddle = new PongPaddle(createPaddle("Player", 40, 120, 8, 8, B2DVars.BIT_PLAYER));
        opponentPaddle = new PongPaddle(createPaddle("Opponent", cam.viewportWidth - 40, 120, 8, 8, B2DVars.BIT_OPPONENT));



        // set up box2d cam
        b2dCam = new OrthographicCamera();
        b2dCam.setToOrtho(false, Game.WIDTH/ B2DVars.PPM, Game.HEIGHT/ B2DVars.PPM);
    }

    private void createBoundary(float xPos, float yPos, float width, float height){
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos/ B2DVars.PPM, yPos/ B2DVars.PPM);
        bdef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width/ B2DVars.PPM, height/ B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape=shape;
        // set bits to collide with
        fdef.filter.categoryBits = B2DVars.BIT_GROUND;
        fdef.friction = 1f;
        fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_OPPONENT;
        body.createFixture(fdef).setUserData("Ground");
    }

    private Body[] createPaddle(String name, float xPos, float yPos, float width, float height, short bodyCategory){
        Body[] bodies = new Body[2];
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / B2DVars.PPM, height / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape=shape;
        fdef.filter.categoryBits = bodyCategory;
        fdef.filter.maskBits = B2DVars.BIT_GROUND;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos / B2DVars.PPM, yPos / B2DVars.PPM);
        bdef.type= BodyDef.BodyType.DynamicBody;
        bodies[0] = world.createBody(bdef);
        bodies[0].createFixture(fdef).setUserData(name);

        //add foot
        shape.setAsBox((width-2)/B2DVars.PPM, 2/B2DVars.PPM, new Vector2(0, -height/B2DVars.PPM), 0);
        fdef.shape=shape;
        fdef.filter.categoryBits=B2DVars.BIT_PLAYER;
        fdef.filter.maskBits=B2DVars.BIT_GROUND;
        fdef.isSensor=true;
        bodies[0].createFixture(fdef).setUserData(name + "Foot");

        return bodies;
    }

    @Override
    public void handleInput() {
    }

    public void shoot(){
        Vector2 pos = playerPaddle.getPosition();
        bullet(pos.x, pos.y, 1, false);
    }

    public void bullet(float xPos, float yPos, float dir, boolean harmFul) {
        PolygonShape shape = new PolygonShape();
        Vector2 pos = playerPaddle.getPosition();
        shape.setAsBox(4 / B2DVars.PPM, 2 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        if (harmFul) fdef.filter.categoryBits = B2DVars.BIT_BULLET;
        BodyDef bdef = new BodyDef();
        bdef.position.set(xPos, yPos);
        bdef.type = BodyDef.BodyType.KinematicBody;
        Body body = world.createBody(bdef);
        body.createFixture(fdef).setUserData("Bullet");
        body.setLinearVelocity(100f * dir / B2DVars.PPM, 0);
        if (!harmFul) {
            mon.addAction(B2DVars.MY_ID + ":SHOOT:" + pos.x + ":" + pos.y + ":" + dir);
        }
    }

    public void opponentShot(float xPos, float yPos, float dir){
        bullet(xPos, yPos, dir, true);
    }


    public void handlePongInput(float dt){

        if(PongInput.isPressed(PongInput.BUTTON_UP) && cl.canJump()) {
            Vector2 temp = playerPaddle.getPosition();
            playerPaddle.movePaddle(50, 150, temp.x, temp.y);
            mon.addAction(B2DVars.MY_ID + ":MOVE:50:150:"+temp.x+":"+temp.y);
        }
        if(PongInput.isPressed(PongInput.BUTTON_DOWN) && cl.canJump()) {
            Vector2 temp = playerPaddle.getPosition();
            playerPaddle.movePaddle(-50, 150, temp.x, temp.y);
            mon.addAction(B2DVars.MY_ID + ":MOVE:-50:150:"+temp.x+":"+temp.y);
        }
        if(PongInput.isPressed(PongInput.BUTTON_W)) {
            shoot();
        }

    }

    private void opponentActions(){
        String[] action = mon.getOpponentAction().split(":");
        if (validOpponentAction(action)) {
            if(action[1].equals("MOVE"))
                opponentPaddle.movePaddle(Float.valueOf(action[2]), Float.valueOf(action[3]),
                        Float.valueOf(action[4]), Float.valueOf(action[5]));
            else if (action[1].equals("SHOOT"))
                opponentShot(Float.valueOf(action[2]), Float.valueOf(action[3]), Float.valueOf(action[4]));
        }
    }
    private boolean validOpponentAction(String[] split){
        if(split.length>=4 && !split[0].equals(B2DVars.MY_ID))
            return true;
        return false;
    }

    private void respawnPlayer(){
        playerPaddle.movePaddle(0, 0, 100/B2DVars.PPM, 100/B2DVars.PPM);
        cl.revive();
    }

    @Override
    public void update(float dt) {
        handlePongInput(dt);
        opponentActions();
        if (cl.amIHit())
            respawnPlayer();
        world.step(dt, 6, 2);
    }

    @Override
    public void render() {
        //Clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        b2dr.render(world, b2dCam.combined);
        sb.setProjectionMatrix(cam.combined);
    }

    @Override
    public void dispose() {

    }
}
