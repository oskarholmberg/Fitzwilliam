package com.game.bb.network;

import org.lwjgl.Sys;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by erik on 08/05/16.
 */
public class ServerReader extends Thread{
    private NetworkMonitor mon;
    private Socket socket;
    private DataInputStream is;
    private String address;
    private int port;

    public ServerReader(NetworkMonitor mon, String address, int port){
        this.mon=mon;
        this.address = address;
        this.port = port;
        try {
            System.out.println("Trying to connect to " + address);
            socket = new Socket(address, port);
            is = new DataInputStream(socket.getInputStream());
            System.out.println("Success!");
        } catch (ConnectException e ){
            System.out.println("Server @"+ address + ":" + port + " is offline. But you may still play offline.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while(!socket.isClosed()){
            try {
                mon.addOpponentAction(is.readUTF());
            } catch (SocketException e){
                try{
                    socket.close();
                    System.out.println("No server available...");
                } catch (IOException e1){
                    e1.printStackTrace();
                }
            } catch (EOFException e){
                try {
                    socket.close();
                    while(socket.isClosed()){
                        sleep(5000);
                        try {
                            socket = new Socket(address, port);
                            is = new DataInputStream(socket.getInputStream());
                        } catch (ConnectException e1){

                        }
                        catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean isConnected() {
        return socket != null;
    }
}
