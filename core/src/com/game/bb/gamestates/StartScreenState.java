package com.game.bb.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.game.bb.handlers.Assets;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.GameStateManager;

/**
 * Created by erik on 12/05/16.
 */
public class StartScreenState extends GameState {

    private SPButton playButton;
    private World world;
    private Texture background = Assets.getBackground();
    private Array<FallingBody> itRains;
    private float newFallingBody = 0f;


    public StartScreenState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, -9.81f), true);
        itRains = new Array<FallingBody>();


        playButton = new SPButton(new Texture("images/button/playButton.png"),
                cam.viewportWidth / 2, cam.viewportHeight / 2, 304f, 80f, cam);
        fallingBody();
    }

    public void fallingBody(){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(20 / B2DVars.PPM, 20 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape=shape;
        BodyDef bdef = new BodyDef();
        bdef.position.set( (float) (cam.viewportWidth*Math.random()) , cam.viewportHeight);
        bdef.type= BodyDef.BodyType.DynamicBody;
        Body body = world.createBody(bdef);
        itRains.add(new FallingBody(body));
        shape.dispose();
    }


    @Override
    public void handleInput() {
        if (playButton.isClicked()) {
            dispose();
            Sound sound = Gdx.audio.newSound(Gdx.files.internal("sfx/levelselect.wav"));
            sound.play();
            gsm.setState(GameStateManager.CONNECT);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        if (newFallingBody > 1f){
            fallingBody();
            newFallingBody = 0f;
        } else {
            newFallingBody+=dt;
        }
        world.step(dt, 6, 2);
        playButton.update(dt);
    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(background, 0, 0);
        sb.end();
        for (FallingBody body : itRains)
            body.render(sb);
        playButton.render(sb);
    }

    @Override
    public void dispose() {
        for (FallingBody b : itRains){
            b.dispose();
        }
    }

    public class FallingBody implements Disposable{
        private Texture texture = Assets.getTex("blueJumpRight");
        private Body body;
        public FallingBody(Body body){
            this.body=body;
        }

        public void render(SpriteBatch sb){
            sb.begin();
            sb.draw(texture, body.getPosition().x, body.getPosition().y, 50, 44);
            sb.end();
        }

        @Override
        public void dispose() {
        }
    }
}
