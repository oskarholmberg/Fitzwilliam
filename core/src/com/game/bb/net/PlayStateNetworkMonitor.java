package com.game.bb.net;

import com.badlogic.gdx.utils.Array;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.B2DVars;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by erik on 13/05/16.
 */
public class PlayStateNetworkMonitor {
    private Array<String> opponentActions;
    private GameClient client;
    private PlayState ps;
    private int port = 8080;

    public PlayStateNetworkMonitor(PlayState ps) {
        this.ps = ps;
        opponentActions = new Array<String>();
        init(port);
    }

    private void init(int port) {
        String serverIp = getServerIp();
        System.out.println("Starting client.");
        client = new GameClient(this, serverIp, port);
        client.start();
        Runtime.getRuntime().addShutdownHook(new Thread(client.getDisconnecter()));
        String initConnect = B2DVars.MY_ID + ":" + "CONNECT" + ":" + "0" + ":" + "0" + ":" + B2DVars.CAM_WIDTH / 2 / B2DVars.PPM +
                ":" + B2DVars.CAM_HEIGHT / B2DVars.PPM + ":" + B2DVars.BIT_OPPONENT + ":" + B2DVars.ID_OPPONENT + ":" + "red" + ":0";
        client.sendData(initConnect.getBytes());
    }

    private String getServerIp() {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] hello = "HELLO".getBytes();
            DatagramPacket packet = new DatagramPacket(hello, hello.length, InetAddress.getByName("224.0.13.37"), port+1);
            socket.send(packet);
            System.out.println("Hello sent! Waiting for response.");
            byte[] serverInfo = new byte[1024];
            DatagramPacket received = new DatagramPacket(serverInfo, serverInfo.length);
            socket.receive(received);
            System.out.println("Server info received.");
            return new String(received.getData()).trim();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }

    public float[] getPacketFloats(String[] split) {
        if (split.length > 5) {
            float[] floats = {Float.valueOf(split[2]), Float.valueOf(split[3]), Float.valueOf(split[4]), Float.valueOf(split[5])};
            return floats;
        }
        return null;
    }

    public synchronized void sendPlayerAction(String action, float xForce, float yForce, String... misc) {
        String packet = B2DVars.MY_ID + ":" + action + ":" + xForce + ":" + yForce + ":" + ps.playerPosition().x + ":" +
                ps.playerPosition().y;
        for (String s : misc) {
            packet += (":" + s);
        }
        client.sendData(packet.getBytes());
    }

    public synchronized void addOpponentAction(String action) {
        opponentActions.add(action);
    }

    public synchronized String getOpponentAction() {
        if (opponentActions.size != 0) {
            return opponentActions.pop();
        } else {
            return "NO_ACTION";
        }
    }
}
