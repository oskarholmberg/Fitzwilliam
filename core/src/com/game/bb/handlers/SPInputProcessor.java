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
        if (k == Input.Keys.E)
            SPInput.setKey(SPInput.BUTTON_E, true);
        if (k == Input.Keys.RIGHT)
            SPInput.setKey(SPInput.BUTTON_RIGHT, true);
        if (k == Input.Keys.LEFT)
            SPInput.setKey(SPInput.BUTTON_LEFT, true);
        if (k == Input.Keys.Y)
            SPInput.setKey(SPInput.BUTTON_Y, true);
        return true;
    }

    public boolean keyUp(int k){
        if (k == Input.Keys.W)
            SPInput.setKey(SPInput.BUTTON_W, false);
        if (k == Input.Keys.E)
            SPInput.setKey(SPInput.BUTTON_E, false);
        if (k == Input.Keys.RIGHT)
            SPInput.setKey(SPInput.BUTTON_RIGHT, false);
        if (k == Input.Keys.LEFT)
            SPInput.setKey(SPInput.BUTTON_LEFT, false);
        if (k == Input.Keys.Y)
            SPInput.setKey(SPInput.BUTTON_Y, false);
        return true;
    }

}
