package com.game.bb.handlers;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.game.bb.entities.SPBullet;

/**
 * Created by erik on 08/05/16.
 */
public class SPContactListener implements ContactListener {
    private int footContact = 0, amntJumps = 0;
    private boolean playerHit = false;
    private Array<Body> bodiesToRemove, grenadeBounces;
    private Fixture killingBullet;

    public SPContactListener() {
        bodiesToRemove = new Array<Body>();
        grenadeBounces = new Array<Body>();
    }


    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();


        if (fa.getUserData().equals(B2DVars.ID_FOOT) || fb.getUserData().equals(B2DVars.ID_FOOT)) {
            footContact++;
            amntJumps = 0;
        } else if (fa.getUserData().equals(B2DVars.ID_PLAYER) && fb.getUserData().equals(B2DVars.ID_BULLET)) {
            //Unless player is dead, mark him as dead.
            playerHit = true;
            killingBullet = fb;
            bodiesToRemove.add(fb.getBody());
        } else if (fa.getUserData().equals(B2DVars.ID_BULLET) && fb.getUserData().equals(B2DVars.ID_PLAYER)) {
            playerHit = true;
            killingBullet = fa;
            bodiesToRemove.add(fa.getBody());
        } else if (fa.getUserData().equals(B2DVars.ID_BULLET) && (fb.getUserData().equals(B2DVars.ID_GROUND) || fb.getUserData().equals(B2DVars.ID_OPPONENT))) {
            bodiesToRemove.add(fa.getBody());
            killingBullet = fa;
        } else if ((fa.getUserData().equals(B2DVars.ID_GROUND) || fa.getUserData().equals(B2DVars.ID_OPPONENT)) && fb.getUserData().equals(B2DVars.ID_BULLET)) {
            bodiesToRemove.add(fb.getBody());
            killingBullet = fb;
        } else if (fa.getUserData().equals(B2DVars.ID_GRENADE) && fb.getUserData().equals(B2DVars.ID_PLAYER)){
            playerHit = true;
            bodiesToRemove.add(fa.getBody());
        } else if (fb.getUserData().equals(B2DVars.ID_GRENADE) && fa.getUserData().equals(B2DVars.ID_PLAYER)){
            playerHit = true;
            bodiesToRemove.add(fb.getBody());
        } else if (fa.getUserData().equals(B2DVars.ID_GRENADE)){
            System.out.println("Grenade contact!");
            if (!grenadeBounces.contains(fa.getBody(), true)){
                grenadeBounces.add(fa.getBody());
            }
        } else if (fb.getUserData().equals(B2DVars.ID_GRENADE)){
            System.out.println("Grenade contact!");
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

    public SPBullet getKillingBullet() {
        if (killingBullet.getBody().getUserData() instanceof SPBullet)
            return (SPBullet) killingBullet.getBody().getUserData();
        return null;
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
