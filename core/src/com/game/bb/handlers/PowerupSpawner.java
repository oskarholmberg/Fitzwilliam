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
    private float mapWidth;
    private PowerupHandler powerHandler;

    public PowerupSpawner(World world, GameClient client, float mapWidth, PowerupHandler powerHandler){
        this.world=world;
        this.client=client;
        this.mapWidth=mapWidth;
        this.powerHandler=powerHandler;
        randomTime = 20f;
        timeCheck = 0;
    }

    public void generatePowerup(){
        Vector2 powerPos = new Vector2(MathUtils.random(10f / B2DVars.PPM, (mapWidth - 10f) / B2DVars.PPM),
                630 / B2DVars.PPM);
        powerIdAccum++;
        int id = Integer.valueOf(powerId + Integer.toString(powerIdAccum));
        int powerType = MathUtils.random(2, B2DVars.POWERTYPE_AMOUNT);
        // below used for debugging powerUps, managed through limiting the range
        //int powerType = MathUtils.random(1, 1);
        SPPower power = new SPPower(world, powerPos.x, powerPos.y, id, powerType);
        powerHandler.addPower(id, power);
        TCPEventPacket pkt = Pooler.tcpEventPacket();
        pkt.id = id;
        pkt.action = B2DVars.NET_SPAWN_POWER;
        pkt.pos = powerPos;
        pkt.misc = powerType;
        client.sendTCP(pkt);
        Pooler.free(pkt);
    }

    public void update(float dt){
        if (timeCheck > randomTime){
            randomTime = MathUtils.random(20.0f, 30.0f);
            timeCheck = 0f;
            generatePowerup();
        } else {
            timeCheck += dt;
        }
    }
}
