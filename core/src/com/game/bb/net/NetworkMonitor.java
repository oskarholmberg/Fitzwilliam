package com.game.bb.net;

import com.badlogic.gdx.utils.Array;

/**
 * Created by erik on 13/05/16.
 */
public class NetworkMonitor {
    private Array<String> opponentActions;
    private GameClient client;
    private String ipAddress;
    private int port;
    private boolean host;

    public NetworkMonitor(){

    }

    private void init(){
    }

    public String netPacketBuilder(String ID, String command, float xPos, float yPos, float xForce, float yForce, String... misc ){

        return null;
    }

    public synchronized void sendPlayerAction(String s){

    }
}
