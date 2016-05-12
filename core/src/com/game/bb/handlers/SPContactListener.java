package com.game.bb.handlers;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.game.bb.gamestates.GameState;

import java.util.ArrayList;

/**
 * Created by erik on 08/05/16.
 */
public class SPContactListener implements ContactListener {
    private int footContact = 0, amntJumps = 0;
    private boolean playerHit = false;
    private Array<Body> bodiesToRemove;

    public SPContactListener(){
        bodiesToRemove = new Array<Body>();
    }


    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData().equals(B2DVars.ID_FOOT) || fb.getUserData().equals(B2DVars.ID_FOOT)) {
            footContact++;
            amntJumps = 0;
        }
        if (fa.getUserData().equals(B2DVars.ID_PLAYER) && fb.getUserData().equals(B2DVars.ID_BULLET)){
            playerHit = true;
            bodiesToRemove.add(fb.getBody());
        } else if (fa.getUserData().equals(B2DVars.ID_BULLET) && fb.getUserData().equals(B2DVars.ID_PLAYER)){
            playerHit = true;
            bodiesToRemove.add(fa.getBody());
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData().equals(B2DVars.ID_FOOT) ||fb.getUserData().equals(B2DVars.ID_FOOT))
            footContact--;
    }

    public Array<Body> getBodiesToRemove(){
        Array<Body> temp = bodiesToRemove;
        if (bodiesToRemove.size > 1)
            System.out.println("Size of bodiesToRemove: " + bodiesToRemove.size);
        bodiesToRemove.clear();
        return temp;
    }

    public void revive(){
        playerHit = false;
        amntJumps=0;
    }

    public boolean amIHit(){
        return playerHit;
    }

    public boolean canJump(){
        if (footContact > 0 || amntJumps < 4){
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
}
