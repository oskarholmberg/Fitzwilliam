package com.game.bb.handlers;

/**
 * Created by erik on 24/05/16.
 */
public class WeaponHandler {
    private int amountBullets;
    private int amountGrenades;
    private HUD hud;

    public WeaponHandler(HUD hud){
        this.hud=hud;
        amountBullets = B2DVars.AMOUNT_BULLET;
        amountGrenades = B2DVars.AMOUNT_GRENADE;
    }

    private void refreshAmmo(float dt){

    }

    public void update(float dt){
        refreshAmmo(dt);
    }
}
