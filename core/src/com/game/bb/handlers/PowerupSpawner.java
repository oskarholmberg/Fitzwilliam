package com.game.bb.handlers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.game.bb.net.client.GameClient;


public class PowerupSpawner {
    private World world;
    private GameClient client;
    private float randomTime;
    private float timeCheck;

    public PowerupSpawner(World world, GameClient client){
        this.world=world;
        this.client=client;
        randomTime = MathUtils.random(20.0f, 30.0f);
        timeCheck = 0;
    }

    public void generatePowerup(){
        Vector2 powerPos = new Vector2(MathUtils.random(10f, 950f), 630);

    }

    public void update(float dt){
        if (timeCheck > randomTime){
            randomTime = MathUtils.random(20.0f, 30.0f);
            generatePowerup();
        } else {
            timeCheck += dt;
        }
    }
}
