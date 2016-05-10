package com.game.bb.network;

import com.game.bb.handlers.B2DVars;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by erik on 08/05/16.
 */
public class ServerWriter extends Thread {
    private Socket socket;
    private NetworkMonitor mon;
    private DataOutputStream os;
    private String address;
    private int port;

    public ServerWriter(NetworkMonitor mon, String address, int port){
        this.mon=mon;
        this.address=address;
        this.port = port;
        try {
            socket = new Socket(address, port);
            os = new DataOutputStream(socket.getOutputStream());
            os.writeUTF(B2DVars.MY_ID); //Sending temporary name
            os.flush();
        } catch (ConnectException e){
        }
        catch (IOException e) {
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
                    System.out.println("Server connection lost.");
                    while(socket.isClosed()){
                        sleep(5000);
                        System.out.println("Trying to reconnect...");
                        try {
                            socket = new Socket(address, port);
                            os = new DataOutputStream(socket.getOutputStream());
                            os.writeUTF(B2DVars.MY_ID); //Sending temporary name
                            os.flush();
                        } catch (ConnectException e1){

                        }
                        catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    System.out.println("Connection reestablished");
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
