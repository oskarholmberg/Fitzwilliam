package com.game.bb.net;

import com.game.bb.handlers.GameStateManager;
import com.game.bb.main.Game;
import com.sun.org.apache.xpath.internal.SourceTree;

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

    private GameStateManager gsm;
    private InetAddress ipAddress;
    private DatagramSocket socket;
    private int port;

    public GameClient(GameStateManager gsm, String ipAddress, int port){
        this.gsm=gsm;
        this.port=port;
        try {
            System.out.println("Connecting to " + ipAddress + ":" + port);
            this.socket=new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
            System.out.println("Connection successful!");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
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
            String action = new String(packet.getData());
            System.out.println("SERVER > " + action);
            gsm.addAction(action);
        }
    }

    public void sendData(byte[] data){
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
