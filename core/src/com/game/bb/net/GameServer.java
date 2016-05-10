package com.game.bb.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by oskar on 5/11/16.
 */
public class GameServer extends Thread {

    private DatagramSocket socket;
    private int port;

    public GameServer(int port){
        this.port=port;
        try {
            System.out.println("Trying to start server on port: " + port);
            this.socket=new DatagramSocket(port);
            System.out.println("Server running...");
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
            System.out.println("CLIENT > " + new String(packet.getData()));
            sendData(packet.getData(), packet.getAddress(), packet.getPort());
        }
    }

    public void sendData(byte[] data, InetAddress ipAddress, int port){
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        new GameServer(1337).start();
    }
}
