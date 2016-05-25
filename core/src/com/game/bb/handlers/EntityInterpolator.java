package com.game.bb.handlers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.TimeUtils;
import com.game.bb.entities.EnemyEntity;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;


public class EntityInterpolator {
    private EnemyEntity entity;
    private Body body;
    private Vector2 targetPos, currentPos, velocity;
    private long lastUpdateTime;

    public EntityInterpolator(EnemyEntity entity){
        this.entity=entity;
        targetPos = new Vector2();
        velocity = new Vector2();
    }

    public void init(){
        body = entity.getBody();
        currentPos = body.getPosition();
        lastUpdateTime = TimeUtils.millis();
    }

    public float getAlpha(){
        long now = TimeUtils.millis();
        float alpha = (now - lastUpdateTime) / 30f;
        lastUpdateTime = now;
        return MathUtils.clamp(alpha, 0f, 1.0f);
    }



    public void updateEntityState(EntityPacket pkt){
        currentPos.set(body.getPosition());
        targetPos.set(pkt.xp, pkt.yp);
        velocity.set(currentPos).lerp(targetPos, getAlpha());
        body.setTransform(velocity, 0);
        body.setLinearVelocity(pkt.xf, pkt.yf);
    }

    public Vector2 getPlayerPosition(PlayerMovementPacket pkt){
        currentPos.set(body.getPosition());
        targetPos.set(pkt.xp, pkt.yp);
        velocity.set(currentPos).lerp(targetPos, getAlpha());
        return velocity;
    }
}
