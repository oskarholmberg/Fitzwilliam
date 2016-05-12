package com.game.bb.net;

import com.badlogic.gdx.utils.Array;
import com.game.bb.handlers.B2DVars;

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
    private HashMap<String, String> connectedClients;

    public GameServer(int port) {
        // First String is ipAddress, second is last known action.
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
            if (connectedClients.get(ipAddress) == null) {
                //New client has connected
                connectedClients.put(ipAddress, content);
                for (String id : connectedClients.keySet()) {
                    String[] info=connectedClients.get(id).split(":");
                    sendData((info[0]+":CONNECT:0:0:"+info[4]+":"+info[5]+":"+ B2DVars.BIT_OPPONENT+":"+B2DVars.ID_OPPONENT+":red").getBytes());
                }
                System.out.println("CLIENT[" + ipAddress + "] connected.");
            }
            if(segments[1].equals("MOVE")){
                connectedClients.put(ipAddress, content);
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
        for (String s : connectedClients.keySet()) {
            String[] address = s.split(":");
            try {
                InetAddress ip = InetAddress.getByName(address[0]);
                int port = Integer.valueOf(address[1]);
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                connectedClients.remove(s);
                System.out.println("CLIENT[" + s + "] disconnected.");
            }
        }
    }

    public static void main(String[] args) {
        new GameServer(8080).start();
    }
}
