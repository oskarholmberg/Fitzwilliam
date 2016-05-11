package com.game.bb.handlers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.game.bb.gamestates.GameState;

/**
 * Created by erik on 08/05/16.
 */
public class PongContactListener implements ContactListener {
    private int footContact = 0, amntJumps = 0;
    private boolean playerHit = false;

    public PongContactListener(){

    }


    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData().equals("PlayerFoot") || fb.getUserData().equals("PlayerFoot")) {
            footContact++;
            amntJumps = 0;
        }
        if (fa.getUserData().equals("Player") && fb.getUserData().equals("Bullet")){
            playerHit = true;
        } else if (fa.getUserData().equals("Bullet") && fb.getUserData().equals("Player")){
            playerHit = true;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa.getUserData().equals("PlayerFoot") ||fb.getUserData().equals("PlayerFoot"))
            footContact--;
    }

    public void revive(){ playerHit = false; }

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
