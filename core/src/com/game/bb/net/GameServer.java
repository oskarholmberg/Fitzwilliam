package com.game.bb.net;

import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oskar on 5/11/16.
 */
public class GameServer extends Thread {

    private DatagramSocket socket;
    private int port;
    private HashMap<String, String> connectedClients;

    public GameServer(int port) {
        this.port = port;
        // First String is ID, second is last known position.
        connectedClients = new HashMap<String, String>();
        try {
            System.out.println("Trying to start server on port: " + port);
            this.socket = new DatagramSocket(port);
            System.out.println("Success! Server listening on port: " + port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!socket.isClosed()) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String ipAddress = packet.getAddress().getHostAddress() + ":" + packet.getPort();
            String content = new String(packet.getData()).trim();
            String[] segments = content.split(":");
            if (connectedClients.get(segments[0]) == null) {
                //New client has connected
                for (String id : connectedClients.keySet()) {
                    String lastKnownPosition=connectedClients.get(id);
                    sendData((segments[0]+":"+"CONNECT:"+lastKnownPosition).getBytes());
                }
                connectedClients.put(segments[0], segments[2]+":"+segments[3]);

                System.out.println("CLIENT[" + ipAddress + "] connected.");
            }
            if (segments[1].equals("DISCONNECT")) {
                //Client has disconnected
                connectedClients.remove(ipAddress);
                System.out.println("CLIENT[" + ipAddress + "] disconnected.");
            }
            System.out.println("CLIENT[" + ipAddress + "] > " + content);
            sendData(packet.getData());

        }
    }

    public void sendData(byte[] data) {
        for (String s : connectedClients) {
            String[] client = s.split(":");
            try {
                InetAddress ip = InetAddress.getByName(client[0]);
                int port = Integer.valueOf(client[1]);
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                connectedClients.removeValue(s, false);
                System.out.println("CLIENT[" + s + "] disconnected.");
            }
        }
    }

    public static void main(String[] args) {
        new GameServer(8080).start();
    }
}
