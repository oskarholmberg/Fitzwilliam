package com.game.bb.handlers.pools;

import com.game.bb.entities.EnemyBullet;
import com.game.bb.entities.EnemyGrenade;
import com.game.bb.net.packets.EntityCluster;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;
import com.game.bb.net.packets.TCPEventPacket;

/**
 * Created by erik on 21/05/16.
 */
public class Pooler {
    private static EntityPacketPool entityPacketPool;
    private static EntityClusterPool entityClusterPool;
    private static PlayerMovementPacketPool  playerMovementPacketPool;
    private static TCPEventPacketPool tcpEventPacketPool;
    private static EnemyBulletPool enemyBulletPool;
    private static EnemyGrenadePool enemyGrenadePool;


    public static void init() {
        entityPacketPool = new EntityPacketPool();
        entityClusterPool = new EntityClusterPool();
        playerMovementPacketPool = new PlayerMovementPacketPool();
        tcpEventPacketPool = new TCPEventPacketPool();
        enemyGrenadePool = new EnemyGrenadePool();
        enemyBulletPool = new EnemyBulletPool();
    }

    public static void reset(){
        entityPacketPool.clear();
        entityClusterPool.clear();
        playerMovementPacketPool.clear();
        tcpEventPacketPool.clear();
        enemyBulletPool.clear();
        enemyGrenadePool.clear();
    }


    public static class EntityPacketPool extends CountingPool<EntityPacket> {

        @Override
        protected EntityPacket newObject() {
            return new EntityPacket();
        }
    }

    public static void free(EntityPacket ep) {
        entityPacketPool.free(ep);
    }

    public static void free(EntityPacket... eps) {
        for (EntityPacket ep : eps) {
            entityPacketPool.free(ep);
        }
    }

    public static EntityPacket entityPacket() {
        return entityPacketPool.obtain();
    }

    public static class EntityClusterPool extends CountingPool<EntityCluster> {

        @Override
        protected EntityCluster newObject() {
            return new EntityCluster();
        }
    }

    public static void free(EntityCluster ep) {
        entityClusterPool.free(ep);
    }

    public static void free(EntityCluster... eps) {
        for (EntityCluster ep : eps) {
            entityClusterPool.free(ep);
        }
    }

    public static EntityCluster entityCluster() {
        return entityClusterPool.obtain();

    }

    public static class PlayerMovementPacketPool extends CountingPool<PlayerMovementPacket>{

        @Override
        protected PlayerMovementPacket newObject() {
            return new PlayerMovementPacket();
        }
    }

    public static PlayerMovementPacket playerMovementPacket(){
        return playerMovementPacketPool.obtain();
    }

    public static void free(PlayerMovementPacket pkt){
        playerMovementPacketPool.free(pkt);
    }

    public static void free(PlayerMovementPacket ...pkts){
        for (PlayerMovementPacket pkt : pkts){
            playerMovementPacketPool.free(pkt);
        }
    }

    public static class TCPEventPacketPool extends CountingPool<TCPEventPacket>{

        @Override
        protected TCPEventPacket newObject() {
            return new TCPEventPacket();
        }
    }

    public static TCPEventPacket tcpEventPacket(){
        return tcpEventPacketPool.obtain();
    }

    public static void free(TCPEventPacket pkt){
        tcpEventPacketPool.free(pkt);
    }

    public static class EnemyBulletPool extends CountingPool<EnemyBullet>{
        @Override
        protected EnemyBullet newObject() {
            return new EnemyBullet();
        }
    }

    public static EnemyBullet enemyBullet(){
        return enemyBulletPool.obtain();
    }

    public static void free(EnemyBullet eb){
        enemyBulletPool.free(eb);
    }

    public static class EnemyGrenadePool extends CountingPool<EnemyGrenade>{
        @Override
        protected EnemyGrenade newObject() {
            return new EnemyGrenade();
        }
    }

    public static EnemyGrenade enemyGrenade() { return enemyGrenadePool.obtain(); }

    public static void free(EnemyGrenade eg){
        enemyGrenadePool.free(eg);
    }
}
