package com.game.bb.handlers;

import com.game.bb.gamestates.PlayState;

/**
 * Created by erik on 25/05/16.
 */
public class PowerupHandler {
    private float ammoAccum = 20f, tiltAccum = 20f, tiltDirection = 1f;
    private static final float AMMO_DUR = 10f, TILT_DUR = 10f;
    private float rotationAngle = 0f;

    public PowerupHandler(){

    }

    public boolean unlimitedAmmo(){
        if (ammoAccum < AMMO_DUR){
            return true;
        }
        return false;
    }

    public void applyPowerup(int powerupType){
        switch (powerupType) {
            case B2DVars.POWERTYPE_AMMO:
                ammoAccum = 0f;
                break;
            case B2DVars.POWERTYPE_TILTSCREEN:
                tiltAccum = 0f;
                break;
        }
    }

    public void update(float dt){
        if (ammoAccum < AMMO_DUR){
            ammoAccum += dt;
        }
        if (tiltAccum < TILT_DUR){
            tiltAccum += dt;
            if (rotationAngle > 20f){
                tiltDirection = -1f;
            } else if (rotationAngle < -20f){
                tiltDirection = 1f;
            }
            PlayState.playState.cam.rotate(1f * tiltDirection);
            rotationAngle+=1f*tiltDirection;
            if (tiltAccum >= TILT_DUR) {
                PlayState.playState.cam.setToOrtho(false);
                rotationAngle = 0;
            }
        }
    }
}
