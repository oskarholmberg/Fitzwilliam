package com.game.bb.net;

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
    private ArrayList<String> connectedClients;

    public GameServer(int port){
        this.port=port;
        connectedClients = new ArrayList<String>();
        try {
            System.out.println("Trying to start server on port: " + port);
            this.socket=new DatagramSocket(port);
            System.out.println("Success! Server listening on port: " + port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        while(!socket.isClosed()){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String ipAddress = packet.getAddress().getHostAddress()+":"+packet.getPort();
            if(!connectedClients.contains(ipAddress)){
                connectedClients.add(ipAddress);
            }
            System.out.println("CLIENT["+ ipAddress + "] > " + new String(packet.getData()).trim());
            sendData(packet.getData());
        }
    }

    public void sendData(byte[] data) {
        for (String s : connectedClients) {
            String[] client = s.split(":");
            InetAddress ip = null;
            try {
                ip = InetAddress.getByName(client[0]);
                int port = Integer.valueOf(client[1]);
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                connectedClients.remove(s);
            }
        }
    }

    public static void main(String[] args){
        new GameServer(8080).start();
    }
}
