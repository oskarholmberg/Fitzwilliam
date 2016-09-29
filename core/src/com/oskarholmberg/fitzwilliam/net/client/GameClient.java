package com.oskarholmberg.fitzwilliam.net.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.oskarholmberg.fitzwilliam.net.packets.EntityCluster;
import com.oskarholmberg.fitzwilliam.net.packets.EntityPacket;
import com.oskarholmberg.fitzwilliam.net.packets.PlayerMovementPacket;
import com.oskarholmberg.fitzwilliam.net.packets.TCPEventPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by erik on 17/05/16.
 */
public class GameClient extends Listener {
    private Client kryoClient;
    private int udpPort = 8080, tcpPort = 8081;
    private Array<TCPEventPacket> tcpPackets;
    private Array<EntityCluster> entityClusters;
    private Array<PlayerMovementPacket> movementPackets;

    public GameClient() {
        tcpPackets = new Array<TCPEventPacket>();
        entityClusters = new Array<EntityCluster>();
        movementPackets = new Array<PlayerMovementPacket>();
        System.setProperty("java.net.preferIPv4Stack", "true"); //Used to accommodate Android playing.
        kryoClient = new Client();
        Class[] classes = {String.class, Vector2.class, EntityPacket.class, int.class,
                TCPEventPacket.class, EntityCluster.class, EntityPacket[].class, PlayerMovementPacket.class,
                String.class};
        for (Class c : classes){
            kryoClient.getKryo().register(c);
        }
        kryoClient.addListener(this);
        kryoClient.start();
        Gdx.app.log("NET_CLIENT", "GameClient started at " + TimeUtils.millis());
    }

    public List<InetAddress> getLocalServers(int timeout) {
        return kryoClient.discoverHosts(udpPort, timeout);
    }

    public void connectToServer(InetAddress address) {
        try {
            kryoClient.connect(10000, address, tcpPort, udpPort);
            Gdx.app.log("NET_CLIENT_CONNECT", "Connected to host @");
        } catch (IOException e) {
            Gdx.app.log("NET_CLIENT", "Host went offline.");
        }
    }

    @Override
    public void received(Connection c, Object packet){
        if(packet instanceof TCPEventPacket){
            Gdx.app.log("NET_CLIENT_TCP_RECEIVED", packet.toString());
            tcpPackets.add((TCPEventPacket) packet);
        } else if (packet instanceof EntityCluster) {
            entityClusters.add((EntityCluster) packet);
        } else if (packet instanceof PlayerMovementPacket){
            movementPackets.add((PlayerMovementPacket) packet);
        }
    }

    public Array<TCPEventPacket> getTCPEventPackets() {
        Array<TCPEventPacket> temp = new Array<TCPEventPacket>();
        temp.addAll(tcpPackets);
        tcpPackets.clear();
        return temp;

    }

    public Array<PlayerMovementPacket> getOpponentMovements(){
        Array<PlayerMovementPacket> temp = new Array<PlayerMovementPacket>();
        temp.addAll(movementPackets);
        movementPackets.clear();
        return temp;
    }

    public Array<EntityCluster> getEntityClusters(){
        Array<EntityCluster> temp = new Array<EntityCluster>();
        temp.addAll(entityClusters);
        entityClusters.clear();
        return temp;
    }


    public void sendUDP(Object packet) {
        kryoClient.sendUDP(packet);
        //Gdx.app.log("NET_CLIENT_SEND_UDP", packet.getClass().toString());
    }

    public void sendTCP(Object packet) {
        kryoClient.sendTCP(packet);
        Gdx.app.log("NET_CLIENT_SEND_TCP", packet.getClass().toString());
    }

    public boolean isConnected(){
        return kryoClient.isConnected();
    }

    public void stop(){
        kryoClient.stop();
    }
}
