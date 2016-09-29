package com.oskarholmberg.fitzwilliam.gamestates;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.oskarholmberg.fitzwilliam.handlers.Assets;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;

/**
 * Created by erik on 12/05/16.
 */
public class HostOrJoinState extends GameState {

    private MenuButton hostButton, joinButton;
    private World world;
    private Texture background = Assets.getBackground();
    private Array<FallingBody> itRains;
    private float newFallingBody = 0f;


    public HostOrJoinState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, -9.81f), true);
        itRains = new Array<FallingBody>();
        hostButton = new MenuButton(new Texture("images/button/hostGameButton.png"), cam.viewportWidth/2, (cam.viewportHeight/2)+100, 289f, 28f, cam);
        joinButton = new MenuButton(new Texture("images/button/joinGameButton.png"), cam.viewportWidth/2, (cam.viewportHeight/2), 289f, 28f, cam);
        fallingBody();
    }

    public void fallingBody(){
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(20 / B2DVars.PPM, 20 / B2DVars.PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape=shape;
        BodyDef bdef = new BodyDef();
        bdef.position.set( (float) (cam.viewportWidth*Math.random()) , cam.viewportHeight );
        bdef.type= BodyDef.BodyType.DynamicBody;
        Body body = world.createBody(bdef);
        itRains.add(new FallingBody(body));
        shape.dispose();
    }


    @Override
    public void handleInput() {
        if (hostButton.isClicked()) {
            Assets.getSound("menuSelect").play();
            gsm.hostGame(true);
            gsm.setState(GameStateManager.MAP_SELECTION);
        }
        if(joinButton.isClicked()){
            Assets.getSound("menuSelect").play();
            gsm.hostGame(false);
            gsm.setState(GameStateManager.JOIN_SERVER);
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
        hostButton.update(dt);
        joinButton.update(dt);
    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(background, 0, 0);
        sb.end();
        for (FallingBody body : itRains)
            body.render(sb);
        hostButton.render(sb);
        joinButton.render(sb);
    }

    @Override
    public void dispose() {
        for(FallingBody b : itRains){
            b.dispose();
        }
        hostButton.dispose();
        joinButton.dispose();
    }

    public class FallingBody implements Disposable{
        private Texture texture;
        private Body body;
        public FallingBody(Body body){
            this.body=body;
            int randomColor = MathUtils.random(1,4);
            if (randomColor == 1)
                texture = Assets.getTex("blueJumpLeft");
            else if (randomColor == 2)
                texture = Assets.getTex("redJumpLeft");
            else if (randomColor == 3)
                texture = Assets.getTex("greenJumpRight");
            else
                texture = Assets.getTex("yellowJumpRight");
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
