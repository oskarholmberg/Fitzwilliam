package com.game.bb.handlers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.game.bb.entities.SPPower;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.pools.Pooler;
import com.game.bb.net.packets.TCPEventPacket;

/**
 * Created by erik on 25/05/16.
 */
public class PowerupHandler {
    private float ammoAccum = 20f, tiltAccum = 20f, shieldAccum = 20f, tiltDirection = 1f, ghostAccum = 20f;
    private static final float AMMO_DUR = 10f, TILT_DUR = 10f, SHIELD_DUR = 10f, GHOST_DUR = 5f;
    private float rotationAngle = 0f;
    private boolean shielded = false, ghosted = false;
    private IntMap<SPPower> powerups;
    private int xOffset = 25, yOffset = 30;
    private PlayState ps;
    private SPAnimation shield = new SPAnimation(Assets.getAnimation("shield"), 0.2f);

    public PowerupHandler(PlayState ps){
        this.ps=ps;
        powerups = new IntMap<SPPower>();
    }

    public boolean unlimitedAmmo(){
        if (ammoAccum < AMMO_DUR){
            return true;
        }
        return false;
    }

    public void addPower(int id, SPPower power){
        powerups.put(id, power);
    }

    public boolean containsPower(int id){
        return powerups.containsKey(id);
    }

    public void removePower(int id){
        SPPower power = powerups.remove(id);
        ps.world.destroyBody(power.getBody());
        power.dispose();
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
                shieldAccum = 0f;
                break;
            case B2DVars.POWERTYPE_GHOST:
                ghostAccum = 0f;
                ghosted = true;
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
    public boolean isGhosted(){
        return ghosted;
    }

    private void powerTaken(){
        if (ps.cl.powerTaken() && ps.cl.getLastPowerTaken() != null){
            SPPower power = powerups.remove(ps.cl.getLastPowerTaken().getId());
            int powerType = power.getPowerType();
            ps.world.destroyBody(power.getBody());
            power.dispose();

            TCPEventPacket pkt = Pooler.tcpEventPacket();
            pkt.action = B2DVars.NET_DESTROY_BODY;
            pkt.id = power.getId();
            ps.client.sendTCP(pkt);
            Pooler.free(pkt);

            switch (powerType) {
                case B2DVars.POWERTYPE_AMMO:
                    applyPowerup(powerType);
                    break;
                case B2DVars.POWERTYPE_TILTSCREEN:
                    TCPEventPacket pkt2 = Pooler.tcpEventPacket();
                    pkt2.action = B2DVars.NET_APPLY_ANTIPOWER;
                    pkt2.misc = powerType;
                    ps.client.sendTCP(pkt2);
                    Pooler.free(pkt2);
                    break;
                case B2DVars.POWERTYPE_SHIELD:
                    applyPowerup(powerType);
                    break;
                case B2DVars.POWERTYPE_GHOST:
                    applyPowerup(powerType);
            }
        }
    }

    public void update(float dt){
        powerTaken();
        shield.update(dt);
        for (IntMap.Keys it = powerups.keys(); it.hasNext;){
            powerups.get(it.next()).update(dt);
        }
        if (ghostAccum < GHOST_DUR){
            ghostAccum += dt;
            if (ghostAccum > GHOST_DUR){
                ghosted = false;
            }
        }
        if (shieldAccum < SHIELD_DUR){
            shieldAccum += dt;
            if (shieldAccum > SHIELD_DUR){
                removeShield();
            }
        }
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

    public void render(SpriteBatch sb){
        for (IntMap.Keys it = powerups.keys(); it.hasNext;){
            powerups.get(it.next()).render(sb);
        }
        if (ghosted){

        }
        if(shielded){
            sb.begin();
            Vector2 pos = PlayState.playState.player.getPosition();
            sb.draw(shield.getFrame(), pos.x*B2DVars.PPM-xOffset, pos.y*B2DVars.PPM-yOffset, 50, 60);
            sb.end();
        }
    }

}
