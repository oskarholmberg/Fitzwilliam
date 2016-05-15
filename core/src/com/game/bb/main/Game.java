package com.game.bb.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.SPInput;
import com.game.bb.handlers.SPInputProcessor;

public class Game extends ApplicationAdapter {
    public final static String TITLE = "Space Pirates";

    private SpriteBatch batch;
    private OrthographicCamera cam;
    private OrthographicCamera hudCam;


    private com.game.bb.handlers.GameStateManager gsm;

	@Override
	public void create () {

		batch = new SpriteBatch();
        cam = new OrthographicCamera();
        cam.setToOrtho(false, B2DVars.CAM_WIDTH, B2DVars.CAM_HEIGHT);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, B2DVars.CAM_WIDTH, B2DVars.CAM_HEIGHT);

        Gdx.input.setInputProcessor(new SPInputProcessor());

        gsm = new com.game.bb.handlers.GameStateManager(this);
	}



	@Override
	public void render () {
            gsm.update(Gdx.graphics.getDeltaTime());
            gsm.render();
            SPInput.update();
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
