package com.game.bb.net.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.game.bb.handlers.B2DVars;
import com.game.bb.handlers.pools.Pooler;
import com.game.bb.net.packets.EntityCluster;
import com.game.bb.net.packets.EntityPacket;
import com.game.bb.net.packets.PlayerMovementPacket;
import com.game.bb.net.packets.TCPEventPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by erik on 17/05/16.
 */
public class GameServer extends Listener {

    private int udpPort = 8080, tcpPort = 8081;
    private Server kryoServer;
    private HashMap<Connection, String> connections;
    private HashMap<Connection, Integer> playerIds;
    private int playerId;
    private Array<String> availableColors;

    public GameServer(){
        kryoServer = new Server();
        connections = new HashMap<Connection, String>();
        playerIds = new HashMap<Connection, Integer>();
        playerId = 10;
        availableColors = new Array<String>();
        availableColors.add(B2DVars.COLOR_BLUE);
        availableColors.add(B2DVars.COLOR_RED);
        availableColors.add(B2DVars.COLOR_YELLOW);
        availableColors.add(B2DVars.COLOR_GREEN);
        Class[] classes = {String.class, Vector2.class, EntityPacket.class, int.class,
            TCPEventPacket.class, EntityCluster.class, EntityPacket[].class, PlayerMovementPacket.class,
            String.class};
        for (Class c : classes){
            kryoServer.getKryo().register(c);
        }
        try {
            kryoServer.bind(tcpPort, udpPort);
        } catch (IOException e) {
            Gdx.app.log("NET_SERVER", "Server already running, joining local game.");
        }
        kryoServer.addListener(this);
        kryoServer.start();
        Gdx.app.log("NET_SERVER", "Game server started");
        Gdx.app.log("NET_SERVER", "Server running on UDP port: " + udpPort + ", TCP port: " + tcpPort);
    }

    @Override
    public void connected(Connection c){
        Gdx.app.log("NET_SERVER", "Client @" + c.getRemoteAddressUDP().getAddress().toString().substring(1) + " connected.");
        connections.put(c, c.getRemoteAddressTCP().getAddress().toString().substring(1) + ":"
                + c.getRemoteAddressTCP().getPort());
        int id = newPlayerId();
        String color = newPlayerColor();
        TCPEventPacket pkt = Pooler.tcpEventPacket();
        pkt.action = B2DVars.NET_SERVER_INFO;
        pkt.id=id;
        pkt.color = color;
        c.sendTCP(pkt);
        Pooler.free(pkt);

    }
    private int newPlayerId(){
        playerId++;
        return playerId;
    }
    private String newPlayerColor(){
        String color = availableColors.random();
        availableColors.removeValue(color, true);
        return color;
    }

    @Override
    public void received(Connection c, Object packet){
        //Gdx.app.log("NET_SERVER_PACKET_RECEIVED", packet.getClass().toString());
        if (packet instanceof EntityCluster || packet instanceof PlayerMovementPacket) {
            for (Connection connect : kryoServer.getConnections()) {
                if (!c.equals(connect)) {
                    connect.sendUDP(packet);
                }
            }
        } else if (packet instanceof TCPEventPacket){
            if(((TCPEventPacket) packet).action == B2DVars.NET_CONNECT){
                if(!playerIds.containsKey(c)){
                    playerIds.put(c, ((TCPEventPacket) packet).id);
                }
            }
            for (Connection connect : kryoServer.getConnections()) {
                if (!c.equals(connect)) {
                    connect.sendTCP(packet);
                }
            }
        }
    }

    @Override
    public void disconnected(Connection c){
        Gdx.app.log("NET_SERVER", "Client @" + connections.get(c) + " disconnected.");
        TCPEventPacket packet = new TCPEventPacket();
        packet.action = B2DVars.NET_DISCONNECT;
        packet.id = playerIds.get(c);
        playerIds.remove(c);
        connections.remove(c);
        for (Connection connection : kryoServer.getConnections()){
            connection.sendTCP(packet);
        }
    }

    public void stop(){
        kryoServer.stop();
    }

    public static void main(String[] args){
        new GameServer();
    }
}
