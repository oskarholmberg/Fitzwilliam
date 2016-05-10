package com.game.bb.network;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by erik on 08/05/16.
 */
public class ServerReader extends Thread{
    private NetworkMonitor mon;
    private Socket socket;
    private DataInputStream is;
    public ServerReader(NetworkMonitor mon){
        this.mon=mon;
        try {
            System.out.println("Trying to connect to 192.168.1.163");
            socket = new Socket("192.168.1.163", 8081);
            is = new DataInputStream(socket.getInputStream());
            System.out.println("Success!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        int i = 0;
        while(!socket.isClosed()){
            try {
                mon.addOpponentAction(is.readUTF());
                i++;
                System.out.println("operation" + i);
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
                    while(true){
                        try {
                            socket = new Socket("192.168.1.163", 8081);
                            is = new DataInputStream(socket.getInputStream());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
