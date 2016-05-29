package com.game.bb.handlers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.pools.Pooler;
import com.game.bb.net.packets.TCPEventPacket;

/**
 * Created by erik on 25/05/16.
 */
public class PowerupHandler {
    private float ammoAccum = 20f, tiltAccum = 20f, tiltDirection = 1f;
    private static final float AMMO_DUR = 10f, TILT_DUR = 10f;
    private float rotationAngle = 0f;
    private boolean shielded;
    private int xOffset = 25, yOffset = 30;
    private SPAnimation animation = new SPAnimation(Assets.getAnimation("shield"), 0.2f);

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
            case B2DVars.POWERTYPE_SHIELD:
                shielded = true;
                TCPEventPacket pkt = Pooler.tcpEventPacket();
                pkt.id = B2DVars.MY_ID;
                pkt.action = B2DVars.NET_APPLY_ANTIPOWER;
                pkt.misc = B2DVars.POWERTYPE_SHIELD;
                pkt.miscString = "applyShield";
                PlayState.playState.client.sendTCP(pkt);
                Pooler.free(pkt);
                break;
        }
    }

    public void removeShield(){
        TCPEventPacket pkt = Pooler.tcpEventPacket();
        pkt.id = B2DVars.MY_ID;
        pkt.action = B2DVars.NET_APPLY_ANTIPOWER;
        pkt.misc = B2DVars.POWERTYPE_SHIELD;
        pkt.miscString = "removeShield";
        PlayState.playState.client.sendTCP(pkt);
        Pooler.free(pkt);
        shielded = false;
    }

    public boolean isShielded(){
        return shielded;
    }

    public void render(SpriteBatch sb){
        if(shielded){
            Vector2 pos = PlayState.playState.player.getPosition();
            sb.begin();
            sb.draw(animation.getFrame(), pos.x*B2DVars.PPM-xOffset, pos.y*B2DVars.PPM-yOffset, 50, 60);
            sb.end();
        }
    }

    public void update(float dt){
        animation.update(dt);
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
                float camX = PlayState.playState.player.getPosition().x * B2DVars.PPM;
                if ((camX + PlayState.playState.cam.viewportWidth / 2) > PlayState.playState.map.getMapWidth()){
                    PlayState.playState.cam.position.x = PlayState.playState.map.getMapWidth()
                            - PlayState.playState.cam.viewportWidth / 2;
                } else if ((camX - PlayState.playState.cam.viewportWidth / 2) < 0){
                    PlayState.playState.cam.position.x = 0 + PlayState.playState.cam.viewportWidth / 2;
                } else {
                    PlayState.playState.cam.position.x = camX;
                }
            }
        }
    }
}
