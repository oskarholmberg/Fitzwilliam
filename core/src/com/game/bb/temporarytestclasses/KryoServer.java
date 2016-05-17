package com.game.bb.temporarytestclasses;

import com.badlogic.gdx.math.Interpolation;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

/**
 * Created by erik on 17/05/16.
 */
public class KryoServer {

    static Server server;
    static int udpPort = 8080, tcpPort = 8081;

    public static void main(String[] args){
        server = new Server();
        try {
            server.bind(tcpPort, udpPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();
    }
}
