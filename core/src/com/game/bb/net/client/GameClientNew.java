package com.game.bb.net.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.game.bb.net.packets.EntityPacket;

import java.io.IOException;

/**
 * Created by erik on 17/05/16.
 */
public class GameClientNew extends Listener {
    private Client kryoClient;
    private int udpPort = 8080, tcpPort = 8081;
    private Array<Object> receivedPackets;

    public GameClientNew(){
        receivedPackets = new Array<Object>();
        kryoClient = new Client();
        Class[] classes = {String.class, Vector2.class, EntityPacket.class};
        for (Class c : classes){
            kryoClient.getKryo().register(c);
        }
        kryoClient.addListener(this);
        kryoClient.start();
        Gdx.app.log("NET_CLIENT", "GameClient started at " + TimeUtils.millis());

        findLocalServer();
    }

    private void findLocalServer(){
        Gdx.app.log("NET_CLIENT_CONNECT", "Addresses found: ");
        try {
            kryoClient.connect(5000, kryoClient.discoverHost(udpPort, 5000), tcpPort, udpPort);
            Gdx.app.log("NET_CLIENT_CONNECT", "Connected to host @");
        } catch (IOException e) {
            System.out.println("No server found...");
            e.printStackTrace();
        }
    }

    @Override
    public void received(Connection c, Object packet){
        Gdx.app.log("NET_CLIENT_PACKET_RECIEVED", packet.toString());
        if(packet instanceof EntityPacket) receivedPackets.add(packet);
    }

    public Object getReceivedPacket(){
        if(receivedPackets.size > 0)
            return receivedPackets.pop();
        return null;
    }



    public void sendUDP(Object packet){
        kryoClient.sendUDP(packet);
        Gdx.app.log("NET_CLIENT_PACKET_UDP", packet.getClass().toString());
    }

    public void sendTCP(Object packet){
        kryoClient.sendTCP(packet);
        Gdx.app.log("NET_CLIENT_PACKET_TCP", packet.getClass().toString());
    }
}
