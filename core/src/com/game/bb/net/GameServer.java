package com.game.bb.net;

import com.game.bb.handlers.B2DVars;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Created by oskar on 5/11/16.
 */
public class GameServer extends Thread {

    private DatagramSocket datagramSocket;
    private HashMap<String, String> connectedClients;
    private HashMap<String, Integer> clientDeaths;
    private String ipAddress;
    private int port = 8080;

    public GameServer() {
        // First String is ipAddress, second is last known action.
        connectedClients = new HashMap<String, String>();
        // First String is ipAddress, second is deathcount;
        clientDeaths = new HashMap<String, Integer>();
        try {
            System.out.println("Trying to start server on port: " + port);
            this.datagramSocket = new DatagramSocket(port);
            System.out.println("Success! Server listening on port: " + port);
            new GameServerAnnouncer().start();

        } catch (SocketException e) {
            System.out.println("Server already running. Joining own game.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!datagramSocket.isClosed()) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                datagramSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ipAddress = packet.getAddress().getHostAddress() + ":" + packet.getPort();
            String content = new String(packet.getData()).trim();
            String[] segments = content.split(":");
            if (segments[1].equals("CONNECT")) {
                //New client has connected, give that client info of all other clients
                //and their location.
                connectedClients.put(ipAddress, content);
                clientDeaths.put(ipAddress, 0);
                for (String id : connectedClients.keySet()) {
                    String[] info = connectedClients.get(id).split(":");
                    sendData((info[0] + ":CONNECT:" + info[2] + ":" + info[3] + ":" + info[4] + ":" + info[5] +
                            ":" + B2DVars.BIT_OPPONENT + ":" + B2DVars.ID_OPPONENT + ":red" +
                            ":" + clientDeaths.get(id) + ":SPRITE_ID").getBytes(), id);
                }
                System.out.println("CLIENT[" + ipAddress + "] connected.");
            }
            if (segments[1].equals("MOVE")) {
                connectedClients.put(ipAddress, content);
            }
            if (segments[1].equals("DEATH")) {
                clientDeaths.put(ipAddress, clientDeaths.get(ipAddress) + 1);
            }
            if (segments[1].equals("DISCONNECT")) {
                //Client has disconnected
                connectedClients.remove(ipAddress);
                System.out.println("CLIENT[" + ipAddress + "] disconnected.");
            }
            System.out.println("CLIENT[" + ipAddress + "] > " + content);
            sendData(packet.getData(), ipAddress);
        }
    }

    /**
     * Sends data to client unless client ip = ipAddress
     *
     * @param data,      Data to send
     * @param ipAddress, IP address to ignore.
     */
    public void sendData(byte[] data, String ipAddress) {
        for (String s : connectedClients.keySet()) {
            if (!s.equals(ipAddress)) {
                String[] address = s.split(":");
                try {
                    InetAddress ip = InetAddress.getByName(address[0]);
                    int port = Integer.valueOf(address[1]);
                    DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
                    try {
                        datagramSocket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnknownHostException e) {
                    connectedClients.remove(s);
                    System.out.println("CLIENT[" + s + "] disconnected.");
                }
            }
        }
    }

    public static void main(String[] args) {
        new GameServer().start();
    }

    private class GameServerAnnouncer extends Thread {

        private MulticastSocket multicastSocket;

        public GameServerAnnouncer() throws IOException {
            this.multicastSocket = new MulticastSocket(port+1);
            InetAddress group = InetAddress.getByName("224.0.13.37");
            multicastSocket.joinGroup(group);
        }

        public void run() {
            while (true) {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                try {
                    multicastSocket.receive(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String content = new String(packet.getData()).trim();

                if (content.equals("HELLO")) {
                    System.out.println("Hello received from " + packet.getAddress() + ":" + packet.getPort());
                    try {
                        String localAddress = (getOutboundAddress(packet.getSocketAddress()).getHostAddress());
                        sendInfo(localAddress.getBytes(), packet.getAddress(), packet.getPort());
                        System.out.println("Server info sent back. Server ip is:" + localAddress);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void sendInfo(byte[] data, InetAddress address, int port) {
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                try {
                    multicastSocket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private InetAddress getOutboundAddress(SocketAddress remoteAddress) throws SocketException {
            DatagramSocket sock = new DatagramSocket();
            // connect is needed to bind the socket and retrieve the local address
            // later (it would return 0.0.0.0 otherwise)
            sock.connect(remoteAddress);

            final InetAddress localAddress = sock.getLocalAddress();

            sock.disconnect();
            sock.close();
            sock = null;

            return localAddress;
        }
    }

