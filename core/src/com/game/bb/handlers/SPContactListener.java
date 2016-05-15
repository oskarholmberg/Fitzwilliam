package com.game.bb.handlers;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.game.bb.entities.SPSprite;

/**
 * Created by erik on 08/05/16.
 */
public class SPContactListener implements ContactListener {
    private int footContact = 0, amntJumps = 0;
    private boolean playerHit = false;
    private Array<Body> bodiesToRemove, grenadeBounces;
    private Fixture killingEntity;

    public SPContactListener() {
        bodiesToRemove = new Array<Body>();
        grenadeBounces = new Array<Body>();
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
            killingEntity = fb;
            playerHit = true;
            bodiesToRemove.add(fb.getBody());
            // - || -
        } else if (fa.getUserData().equals(B2DVars.ID_BULLET) && fb.getUserData().equals(B2DVars.ID_PLAYER)) {
            killingEntity = fa;
            playerHit = true;
            bodiesToRemove.add(fa.getBody());
            // If a bullet touches ground
        } else if (fa.getUserData().equals(B2DVars.ID_BULLET) && fb.getUserData().equals(B2DVars.ID_GROUND)) {
            killingEntity = fa;
            bodiesToRemove.add(fa.getBody());
            // - || -
        } else if (fa.getUserData().equals(B2DVars.ID_GROUND)  && fb.getUserData().equals(B2DVars.ID_BULLET)) {
            killingEntity = fb;
            bodiesToRemove.add(fb.getBody());
            // If a grenade touches player
        } else if (fa.getUserData().equals(B2DVars.ID_GRENADE) && fb.getUserData().equals(B2DVars.ID_PLAYER)){
            killingEntity = fa;
            playerHit = true;
            bodiesToRemove.add(fa.getBody());
            // - || -
        } else if (fb.getUserData().equals(B2DVars.ID_GRENADE) && fa.getUserData().equals(B2DVars.ID_PLAYER)){
            killingEntity = fb;
            playerHit = true;
            bodiesToRemove.add(fb.getBody());
            // If a grenade bounces
        } else if (fa.getUserData().equals(B2DVars.ID_GRENADE)){
            if (!grenadeBounces.contains(fa.getBody(), true)){
                grenadeBounces.add(fa.getBody());
            }
            // - || -
        } else if (fb.getUserData().equals(B2DVars.ID_GRENADE)){
            if (!grenadeBounces.contains(fb.getBody(), true)){
                grenadeBounces.add(fb.getBody());
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData().equals(B2DVars.ID_FOOT) || fb.getUserData().equals(B2DVars.ID_FOOT))
            footContact--;
    }

    public Array<Body> getGrenadeBounces(){
        return grenadeBounces;
    }

    public Array<Body> getBodiesToRemove() {
        return bodiesToRemove;
    }

    public boolean isPlayerHit() {
        if (playerHit) {
            playerHit = false;
            return true;
        }
        return false;
    }

    public void revivePlayer() {
        playerHit = false;
    }

    public SPSprite getKillingEntity() {
        System.out.println(killingEntity.getUserData());
        return (SPSprite) killingEntity.getBody().getUserData();
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

    public void clearBulletList() {
        if (bodiesToRemove.size > 0)
            bodiesToRemove.clear();
    }

    public void clearGrenadeList(){
        if (grenadeBounces.size > 0)
            grenadeBounces.clear();
    }
}
