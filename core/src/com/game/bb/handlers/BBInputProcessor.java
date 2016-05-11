package com.game.bb.handlers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/**
 * Created by erik on 08/05/16.
 */
public class BBInputProcessor extends InputAdapter{


    public boolean keyDown(int k){
        if (k == Input.Keys.W)
            PongInput.setKey(PongInput.BUTTON_W, true);
        if (k == Input.Keys.S)
            PongInput.setKey(PongInput.BUTTON_S, true);
        if (k == Input.Keys.RIGHT)
            PongInput.setKey(PongInput.BUTTON_RIGHT, true);
        if (k == Input.Keys.LEFT)
            PongInput.setKey(PongInput.BUTTON_LEFT, true);
        return true;
    }

    public boolean keyUp(int k){
        if (k == Input.Keys.W)
            PongInput.setKey(PongInput.BUTTON_W, false);
        if (k == Input.Keys.S)
            PongInput.setKey(PongInput.BUTTON_S, false);
        if (k == Input.Keys.RIGHT)
            PongInput.setKey(PongInput.BUTTON_RIGHT, false);
        if (k == Input.Keys.LEFT)
            PongInput.setKey(PongInput.BUTTON_LEFT, false);
        return true;
    }

}
