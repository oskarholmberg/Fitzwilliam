package com.game.bb.handlers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.entities.SPPower;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.pools.Pooler;
import com.game.bb.net.client.GameClient;
import com.game.bb.net.packets.TCPEventPacket;


public class PowerupSpawner {
    private World world;
    private GameClient client;
    private float randomTime;
    private float timeCheck;
    private String powerId = "123123";
    private int powerIdAccum = 0;

    public PowerupSpawner(World world, GameClient client){
        this.world=world;
        this.client=client;
        randomTime = 5f;
        System.out.println("Time until power up spawn: " + randomTime);
        timeCheck = 0;
    }

    public void generatePowerup(){
        Vector2 powerPos = new Vector2(MathUtils.random(10f / B2DVars.PPM, 950f / B2DVars.PPM),
                630 / B2DVars.PPM);
        powerIdAccum++;
        int id = Integer.valueOf(powerId + Integer.toString(powerIdAccum));
        SPPower power = new SPPower(world, powerPos.x, powerPos.y, id, B2DVars.POWERTYPE_TILTSCREEN);
        PlayState.playState.addPowerup(power, id);
        TCPEventPacket pkt = Pooler.tcpEventPacket();
        pkt.id = id;
        pkt.action = B2DVars.NET_POWER;
        pkt.pos = powerPos;
        pkt.misc = B2DVars.POWERTYPE_TILTSCREEN;
        client.sendTCP(pkt);
        Pooler.free(pkt);
    }

    public void update(float dt){
        if (timeCheck > randomTime){
            randomTime = MathUtils.random(30.0f, 60.0f);
            System.out.println("Powerup spawned! next time: " + randomTime);
            timeCheck = 0f;
            generatePowerup();
        } else {
            timeCheck += dt;
        }
    }
}
