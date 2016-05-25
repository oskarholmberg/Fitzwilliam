package com.game.bb.handlers;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.game.bb.entities.EnemyEntity;
import com.game.bb.entities.SPPower;
import com.game.bb.entities.SPSprite;

/**
 * Created by erik on 08/05/16.
 */
public class SPContactListener implements ContactListener {
    private int footContact = 0, amntJumps = 0, killingEntityID;
    private boolean playerHit = false, playerPowerUp = false;
    private Fixture  lastPowerUp;
    private Array<Integer> idsToRemove;

    public SPContactListener() {
        idsToRemove = new Array<Integer>();
    }


    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();


        // if a foot touches ground
        if (fa.getUserData().equals(B2DVars.ID_FOOT) || fb.getUserData().equals(B2DVars.ID_FOOT)) {
            footContact++;
            amntJumps = 0;
            //If the player is hit by an enemy bullet
        } else if (fa.getUserData().equals(B2DVars.ID_PLAYER) && fb.getUserData().equals(B2DVars.ID_BULLET)) {
            killingEntityID = ((SPSprite) fb.getBody().getUserData()).getID();
            playerHit = true;
            // - || -
        } else if (fa.getUserData().equals(B2DVars.ID_BULLET) && fb.getUserData().equals(B2DVars.ID_PLAYER)) {
            killingEntityID = ((SPSprite) fa.getBody().getUserData()).getID();
            playerHit = true;
            // If a bullet touches ground
        } else if (fa.getUserData().equals(B2DVars.ID_BULLET) && (fb.getUserData().equals(B2DVars.ID_GROUND)
                || fb.getUserData().equals(B2DVars.ID_DOME))) {
            killingEntityID = ((SPSprite) fa.getBody().getUserData()).getID();
            idsToRemove.add(((SPSprite) fa.getBody().getUserData()).getID());
            // - || -
        } else if ((fa.getUserData().equals(B2DVars.ID_GROUND)  || fa.getUserData().equals(B2DVars.ID_DOME))
                && fb.getUserData().equals(B2DVars.ID_BULLET)) {
            killingEntityID = ((SPSprite) fb.getBody().getUserData()).getID();
            idsToRemove.add(((SPSprite) fb.getBody().getUserData()).getID());
            // If a grenade touches player
        } else if ((fa.getUserData().equals(B2DVars.ID_GRENADE) || fa.getUserData().equals(B2DVars.ID_ENEMY_GRENADE))
                && fb.getUserData().equals(B2DVars.ID_PLAYER)){
            killingEntityID = ((SPSprite) fa.getBody().getUserData()).getID();
            playerHit = true;
            // - || -
        } else if ((fb.getUserData().equals(B2DVars.ID_GRENADE) || fb.getUserData().equals(B2DVars.ID_ENEMY_GRENADE))
                && fa.getUserData().equals(B2DVars.ID_PLAYER)){
            killingEntityID = ((SPSprite) fb.getBody().getUserData()).getID();
            playerHit = true;
            // If a player catches a powerup
        } else if (fa.getUserData().equals(B2DVars.ID_POWERUP) && fb.getUserData().equals(B2DVars.ID_PLAYER)){
            lastPowerUp = fa;
            playerPowerUp = true;
            // - || -
        } else if (fb.getUserData().equals(B2DVars.ID_POWERUP) && fa.getUserData().equals(B2DVars.ID_PLAYER)){
            lastPowerUp = fb;
            playerPowerUp = true;
        } else if ((fa.getUserData().equals(B2DVars.ID_PLAYER) || fb.getUserData().equals(B2DVars.ID_PLAYER)) &&
                (fa.getUserData().equals(B2DVars.ID_ENEMY_ENTITY) || fb.getUserData().equals(B2DVars.ID_ENEMY_ENTITY))){
            if (fa.getUserData().equals(B2DVars.ID_ENEMY_ENTITY))
                killingEntityID = ((EnemyEntity) fa.getBody().getUserData()).getId();
            else if (fb.getUserData().equals(B2DVars.ID_ENEMY_ENTITY))
                killingEntityID = ((EnemyEntity) fb.getBody().getUserData()).getId();
            playerHit = true;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData().equals(B2DVars.ID_FOOT) || fb.getUserData().equals(B2DVars.ID_FOOT)) {
            footContact--;
        }
    }


    public Array<Integer> getIdsToRemove() {
        return idsToRemove;
    }

    public boolean isPlayerHit() {
        if (playerHit) {
            playerHit = false;
            return true;
        }
        return false;
    }

    public SPPower getLastPowerTaken(){
        return (SPPower) lastPowerUp.getBody().getUserData();
    }

    public boolean powerTaken() {
        if (playerPowerUp) {
            playerPowerUp = false;
            return true;
        }
        return false;
    }

    public void revivePlayer() {
        playerHit = false;
    }

    public int getKillingEntityID() {
        return killingEntityID;
    }

    public boolean canJump() {
        if (footContact > 0 || amntJumps < 4) {
            amntJumps++;
            return true;
        }
        return false;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public void resetJumps() {
        amntJumps = 0;
    }

    public void clearIdList() {
        if (idsToRemove.size > 0)
            idsToRemove.clear();
    }
}
