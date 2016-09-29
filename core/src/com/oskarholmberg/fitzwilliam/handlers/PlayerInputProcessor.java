package com.oskarholmberg.fitzwilliam.handlers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/**
 * Created by erik on 08/05/16.
 */
public class PlayerInputProcessor extends InputAdapter{

    public boolean touchDown(int x, int y, int pointer, int button) {
        PlayerInput.x = x;
        PlayerInput.y = y;
        PlayerInput.down = true;
        return true;
    }

    public boolean touchUp(int x, int y, int pointer, int button) {
        PlayerInput.x = x;
        PlayerInput.y = y;
        PlayerInput.down = false;
        return true;
    }


    public boolean keyDown(int k){
        if (k == Input.Keys.W)
            PlayerInput.setKey(PlayerInput.BUTTON_W, true);
        if (k == Input.Keys.E)
            PlayerInput.setKey(PlayerInput.BUTTON_E, true);
        if (k == Input.Keys.RIGHT)
            PlayerInput.setKey(PlayerInput.BUTTON_RIGHT, true);
        if (k == Input.Keys.LEFT)
            PlayerInput.setKey(PlayerInput.BUTTON_LEFT, true);
        if (k == Input.Keys.Y)
            PlayerInput.setKey(PlayerInput.BUTTON_Y, true);
        if (k == Input.Keys.K)
            PlayerInput.setKey(PlayerInput.BUTTON_K, true);
        return true;
    }

    public boolean keyUp(int k){
        if (k == Input.Keys.W)
            PlayerInput.setKey(PlayerInput.BUTTON_W, false);
        if (k == Input.Keys.E)
            PlayerInput.setKey(PlayerInput.BUTTON_E, false);
        if (k == Input.Keys.RIGHT)
            PlayerInput.setKey(PlayerInput.BUTTON_RIGHT, false);
        if (k == Input.Keys.LEFT)
            PlayerInput.setKey(PlayerInput.BUTTON_LEFT, false);
        if (k == Input.Keys.Y)
            PlayerInput.setKey(PlayerInput.BUTTON_Y, false);
        if (k == Input.Keys.K)
            PlayerInput.setKey(PlayerInput.BUTTON_K, false);
        return true;
    }

}
