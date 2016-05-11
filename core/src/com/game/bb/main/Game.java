package com.game.bb.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.game.bb.handlers.PongInput;
import com.game.bb.handlers.BBInputProcessor;

public class Game extends ApplicationAdapter {
    public final static String TITLE = "Space Pirates";
    public final static int SCALE = 2;
    public final static int WIDTH = 240*SCALE, HEIGHT = 160*SCALE;

    private SpriteBatch batch;
    private OrthographicCamera cam;
    private OrthographicCamera hudCam;


    private com.game.bb.handlers.GameStateManager gsm;

	@Override
	public void create () {
        Gdx.input.setInputProcessor(new BBInputProcessor());

		batch = new SpriteBatch();
        cam = new OrthographicCamera();
        cam.setToOrtho(false, WIDTH, HEIGHT);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, WIDTH, HEIGHT);

        gsm = new com.game.bb.handlers.GameStateManager(this, "192.168.1.163", 8080);
	}



	@Override
	public void render () {
            gsm.update(Gdx.graphics.getDeltaTime());
            gsm.render();
            PongInput.update();
	}

	public void dispose(){

    }

    public void resize(int w, int h){

    }

    public void pause(){}
    public void resume(){}
    public SpriteBatch getBatch(){return batch;}
    public OrthographicCamera getCam(){return cam;}
    public OrthographicCamera getHudCam() {return hudCam;}
}
