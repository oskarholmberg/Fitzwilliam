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

import javax.xml.crypto.Data;

/**
 * Created by oskar on 5/11/16.
 */
public class GameClient extends Thread {

    private GameStateManager gsm;
    private InetAddress ipAddress;
    private DatagramSocket socket;
    private int port;
    private Disconnecter disconnecter;

    public GameClient(GameStateManager gsm, String ipAddress, int port){
        this.gsm=gsm;
        this.port=port;
        disconnecter = new Disconnecter(this);
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
            String action = new String(packet.getData()).trim();
            gsm.addOpponentAction(action);
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

    protected void disconnect(){
        byte[] disconnect = "disconnect".getBytes();
        sendData(disconnect);
    }

    public Runnable getDisconnecter(){
        return disconnecter;
    }

    private static class Disconnecter implements Runnable{

        private GameClient client;

        private Disconnecter(GameClient client){
            this.client = client;
        }
        @Override
        public void run() {
            client.disconnect();
        }
    }
}
