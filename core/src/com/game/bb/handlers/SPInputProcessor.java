package com.game.bb.handlers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/**
 * Created by erik on 08/05/16.
 */
public class SPInputProcessor extends InputAdapter{

    public boolean touchDown(int x, int y, int pointer, int button) {
        SPInput.x = x;
        SPInput.y = y;
        SPInput.down = true;
        return true;
    }

    public boolean touchUp(int x, int y, int pointer, int button) {
        SPInput.x = x;
        SPInput.y = y;
        SPInput.down = false;
        return true;
    }


    public boolean keyDown(int k){
        if (k == Input.Keys.W)
            SPInput.setKey(SPInput.BUTTON_W, true);
        if (k == Input.Keys.S)
            SPInput.setKey(SPInput.BUTTON_S, true);
        if (k == Input.Keys.RIGHT)
            SPInput.setKey(SPInput.BUTTON_RIGHT, true);
        if (k == Input.Keys.LEFT)
            SPInput.setKey(SPInput.BUTTON_LEFT, true);
        return true;
    }

    public boolean keyUp(int k){
        if (k == Input.Keys.W)
            SPInput.setKey(SPInput.BUTTON_W, false);
        if (k == Input.Keys.S)
            SPInput.setKey(SPInput.BUTTON_S, false);
        if (k == Input.Keys.RIGHT)
            SPInput.setKey(SPInput.BUTTON_RIGHT, false);
        if (k == Input.Keys.LEFT)
            SPInput.setKey(SPInput.BUTTON_LEFT, false);
        return true;
    }

}
