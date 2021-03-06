package com.oskarholmberg.fitzwilliam.net.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.oskarholmberg.fitzwilliam.handlers.B2DVars;
import com.oskarholmberg.fitzwilliam.handlers.pools.Pooler;
import com.oskarholmberg.fitzwilliam.net.packets.EntityCluster;
import com.oskarholmberg.fitzwilliam.net.packets.EntityPacket;
import com.oskarholmberg.fitzwilliam.net.packets.PlayerMovementPacket;
import com.oskarholmberg.fitzwilliam.net.packets.TCPEventPacket;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by erik on 17/05/16.
 */
public class GameServer extends Listener {

    private int udpPort = 8080, tcpPort = 8081;
    private Server kryoServer;
    private HashMap<Connection, String> connections;
    private HashMap<Connection, Integer> playerIds;
    private int playerId, mapNbr = 3;
    private Array<String> availableColors;
    private IntMap<String> takenColors;

    public GameServer(int mapNbr){
        this.mapNbr=mapNbr;
        kryoServer = new Server();
        connections = new HashMap<Connection, String>();
        playerIds = new HashMap<Connection, Integer>();
        takenColors = new IntMap<String>();
        init();
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
        Gdx.app.log("NET_SERVER", "Client @" + c.getRemoteAddressUDP().getAddress().toString().substring(1) + ":" + c.getRemoteAddressTCP().getPort() + " connected.");
        connections.put(c, c.getRemoteAddressTCP().getAddress().toString().substring(1) + ":"
                + c.getRemoteAddressTCP().getPort());
        int id = newPlayerId();
        playerIds.put(c, id);
        String color = newPlayerColor(id);
        TCPEventPacket pkt = Pooler.tcpEventPacket();
        pkt.action = B2DVars.NET_SERVER_INFO;
        pkt.id=id;
        pkt.color = color;
        pkt.misc = mapNbr;
        c.sendTCP(pkt);
        Pooler.free(pkt);

    }
    private int newPlayerId(){
        playerId++;
        return playerId;
    }
    private String newPlayerColor(int id){
        String color = availableColors.random();
        availableColors.removeValue(color, true);
        takenColors.put(id, color);
        return color;
    }

    public void init(){
        playerId = 10;
        availableColors = new Array<String>();
        availableColors.add(B2DVars.COLOR_BLUE);
        availableColors.add(B2DVars.COLOR_RED);
        availableColors.add(B2DVars.COLOR_YELLOW);
        availableColors.add(B2DVars.COLOR_GREEN);
        connections.clear();
        playerIds.clear();
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
        //Restore color to available.
        String color = takenColors.get(playerIds.get(c));
        availableColors.add(color);
        playerIds.remove(c);
        connections.remove(c);
        for (Connection connection : kryoServer.getConnections()){
            connection.sendTCP(packet);
        }
    }

    public void stop(){
        kryoServer.stop();
    }
}
