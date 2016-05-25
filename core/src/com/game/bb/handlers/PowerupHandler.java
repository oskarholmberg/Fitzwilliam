package com.game.bb.handlers;

/**
 * Created by erik on 25/05/16.
 */
public class PowerupHandler {
    private float unlimitedAmmo = 20f;
    private static final float AMMO_DUR = 10f;

    public PowerupHandler(){

    }

    public boolean unlimitedAmmo(){
        if (unlimitedAmmo < AMMO_DUR){
            return true;
        }
        return false;
    }

    public void applyPowerup(int powerupType){
        switch (powerupType) {
            case B2DVars.POWERTYPE_AMMO:
                unlimitedAmmo = 0f;
                break;
        }
    }

    public void update(float dt){
        if (unlimitedAmmo < AMMO_DUR){
            unlimitedAmmo+=dt;
        }
    }
}
