package com.oskarholmberg.fitzwilliam.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;
import com.oskarholmberg.fitzwilliam.handlers.SPInput;
import com.oskarholmberg.fitzwilliam.handlers.SPInputProcessor;
import com.oskarholmberg.fitzwilliam.gamestates.GameStateManager;

public class Game extends ApplicationAdapter {
    public final static String TITLE = "Fitzwilliam";

    private SpriteBatch batch;
    private OrthographicCamera cam;
    private OrthographicCamera hudCam;


    private GameStateManager gsm;

	@Override
	public void create () {

		batch = new SpriteBatch();
        cam = new OrthographicCamera();
        cam.setToOrtho(false, B2DVars.DEFAULT_GAME_WIDTH, B2DVars.DEFAULT_GAME_HEIGHT);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, B2DVars.DEFAULT_GAME_WIDTH, B2DVars.DEFAULT_GAME_HEIGHT);

        Gdx.input.setInputProcessor(new SPInputProcessor());

        gsm = new GameStateManager(this);
	}



	@Override
	public void render () {
        Gdx.graphics.setTitle(TITLE + " -- FPS: " + Gdx.graphics.getFramesPerSecond());
        gsm.update(Gdx.graphics.getDeltaTime());
        gsm.render();
        SPInput.update();
	}

	public void dispose(){

    }

    public void resize(int w, int h){
        cam.update();
        hudCam.update();
    }

    public void setState(int state){
        gsm.pushState(state);
    }

    public void pause(){}
    public void resume(){}
    public SpriteBatch getBatch(){return batch;}
    public OrthographicCamera getCam(){return cam;}
    public OrthographicCamera getHudCam() {return hudCam;}
}
