package com.game.bb.network;

import com.game.bb.handlers.B2DVars;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by erik on 08/05/16.
 */
public class ServerWriter extends Thread {
    private Socket socket;
    private NetworkMonitor mon;
    private DataOutputStream os;

    public ServerWriter(NetworkMonitor mon){
        this.mon=mon;
        try {
            socket = new Socket("192.168.1.163", 8080);
            os = new DataOutputStream(socket.getOutputStream());
            os.writeUTF(B2DVars.MY_ID); //Sending temporary name
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        while(!socket.isClosed()){
            try {
                os.writeUTF(mon.getActions());
                os.flush();
            } catch(SocketException e){
                try {
                    socket.close();
                    System.out.println("Server connection lost...");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
