package com.game.bb.net.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.TCPEventPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by erik on 17/05/16.
 */
public class GameClientNew extends Listener {
    private Client client;
    private int udpPort = 8080, tcpPort = 8081;
    private Array<Object> reveicedPackets;

    public GameClientNew(){
        reveicedPackets = new Array<Object>();
        client = new Client();
        client.getKryo().register(EntityPacket.class);
        client.getKryo().register(TCPEventPacket.class);
        client.addListener(this);
        client.start();
        Gdx.app.log("NET_CLIENT", "Game client started at " + TimeUtils.millis());

        findLocalServer();
    }

    private void findLocalServer(){
        Gdx.app.log("NET_CLIENT_CONNECT", "Addresses found: ");
        try {
            client.connect(5000, "localhost", tcpPort, udpPort);
            Gdx.app.log("NET_CLIENT_CONNECT", "Connected to host @");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void received(Connection c, Object packet){
        reveicedPackets.add(packet);
    }

    public Array<Object> getReceivedPackets(){
        Array<Object> temp = reveicedPackets;
        reveicedPackets.clear();
        return temp;
    }



    public void sendUDP(Object packet){
        client.sendUDP(packet);
    }

    public void sendTCP(Object packet){
        client.sendTCP(packet);
    }
}
