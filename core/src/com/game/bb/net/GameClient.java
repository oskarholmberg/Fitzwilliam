package com.game.bb.net;

import com.game.bb.handlers.B2DVars;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by oskar on 5/11/16.
 */
public class GameClient extends Thread {

    private InetAddress ipAddress;
    private DatagramSocket socket;
    private int port;
    private Disconnecter disconnecter;
    private PlayStateNetworkMonitor mon;

    public GameClient(PlayStateNetworkMonitor mon, String ipAddress, int port) {
        this.port = port;
        this.mon=mon;
        disconnecter = new Disconnecter(this);
        try {
            System.out.println("Connecting to " + ipAddress + ":" + port);
            this.socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
            System.out.println("Connection successful!");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
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
            String content = new String(packet.getData()).trim();
            System.out.println("SERVER > " + content);
            if (content.split("&")[0].equals("SETUP")) {
                String[] players = content.split("&");
                for (int i = 0; i < players.length; i++) {

                }
            } else {
                mon.addOpponentAction(content);
            }
        }
    }

    public void sendData(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void disconnect() {
        byte[] disconnect = (B2DVars.MY_ID+":DISCONNECT").getBytes();
        sendData(disconnect);
    }

    public Runnable getDisconnecter() {
        return disconnecter;
    }

    private static class Disconnecter implements Runnable {

        private GameClient client;

        private Disconnecter(GameClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.disconnect();
        }
    }
}
