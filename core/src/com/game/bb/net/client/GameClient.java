package com.game.bb.net.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.game.bb.net.packets.EntityCluster;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;
import com.game.bb.net.packets.TCPEventPacket;

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
    private List<InetAddress> addresses;
    private int lastEntitySeq = 0;
    private int lastMovementSeq = 0;

    public GameClient() {
        tcpPackets = new Array<TCPEventPacket>();
        entityClusters = new Array<EntityCluster>();
        movementPackets = new Array<PlayerMovementPacket>();
        kryoClient = new Client();
        Class[] classes = {String.class, Vector2.class, EntityPacket.class, int.class,
                TCPEventPacket.class, EntityCluster.class, EntityPacket[].class, PlayerMovementPacket.class};
        for (Class c : classes){
            kryoClient.getKryo().register(c);
        }
        kryoClient.addListener(this);
        kryoClient.start();
        Gdx.app.log("NET_CLIENT", "GameClient started at " + TimeUtils.millis());

        findLocalServer();
    }

    private void findLocalServer() {
        addresses = kryoClient.discoverHosts(udpPort, 2000);
        Gdx.app.log("NET_CLIENT_CONNECT", "Addresses found: " + addresses.toString());
    }

    public List<InetAddress> getLocalServers() {
        return addresses;
    }

    public void connectToServer(int index) {
        try {
            kryoClient.connect(5000, addresses.get(index), tcpPort, udpPort);
            Gdx.app.log("NET_CLIENT_CONNECT", "Connected to host @");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void received(Connection c, Object packet){
        if(packet instanceof TCPEventPacket){
            Gdx.app.log("NET_CLIENT_TCP_RECEIVED", packet.toString());
            tcpPackets.add((TCPEventPacket) packet);
        } else if (packet instanceof EntityCluster) {
            if (((EntityCluster) packet).seq > lastEntitySeq){
                lastEntitySeq = ((EntityCluster) packet).seq;
                entityClusters.add((EntityCluster) packet);
            }
        } else if (packet instanceof PlayerMovementPacket){
            if (((PlayerMovementPacket) packet).seq > lastMovementSeq){
                lastMovementSeq = ((PlayerMovementPacket) packet).seq;
                movementPackets.add((PlayerMovementPacket) packet);
            }
        }
    }

    public TCPEventPacket getTCPEventPackets() {
        if (tcpPackets.size > 0)
            return tcpPackets.pop();

        //Array<Object> temp = new Array<Object>();
        //temp.addAll(tcpPackets);
        return null;

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
}
