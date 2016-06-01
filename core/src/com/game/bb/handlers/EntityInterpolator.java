package com.game.bb.handlers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.game.bb.entities.EnemyEntity;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;

import java.sql.Time;


public class EntityInterpolator {
    private EnemyEntity entity;
    private Body body;
    private Array<EntityPacket> entityStates;
    private Vector2 targetPos, currentPos, interpolatedPos;
    private long lastPacketTime;

    public EntityInterpolator(EnemyEntity entity){
        this.entity=entity;
        targetPos = new Vector2();
        interpolatedPos = new Vector2();
    }

    public void init(){
        body = entity.getBody();
        currentPos = body.getPosition();
        targetPos = body.getPosition();
        entityStates = new Array<EntityPacket>();
        lastPacketTime = TimeUtils.millis();
    }

    public void addEntityPacket(EntityPacket pkt){
        entityStates.add(pkt);
    }

    public void updateEntityState(){
        if (entityStates.size > 0) {
//            if ((entityStates.peek().time + 100) <= TimeUtils.millis()) {
                EntityPacket pkt = entityStates.pop();
                targetPos.set(pkt.xp, pkt.yp);
                body.setLinearVelocity(pkt.xf, pkt.yf);
                lastPacketTime = TimeUtils.millis();
//            }
        }
        currentPos.set(body.getPosition());
        interpolatedPos.set(currentPos.lerp(targetPos, getAlpha()));
        body.setTransform(interpolatedPos, 0);
    }

    public float getAlpha(){

        float alpha = TimeUtils.timeSinceMillis(lastPacketTime) / 35f;
        return MathUtils.clamp(alpha, 0f, 1.0f);
    }

    public Vector2 getPlayerPosition(PlayerMovementPacket pkt){
        currentPos.set(body.getPosition());
        targetPos.set(pkt.xp, pkt.yp);
        interpolatedPos.set(currentPos).lerp(targetPos, getAlpha());
        return interpolatedPos;
    }
}
