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
        String packet = ID + ":" + command + ":" + xPos + ":" + yPos + ":" + xForce + ":" + yForce;
        for (String s : misc){
            packet += (":" + s);
        }
        return packet;
    }

    public float[] getPacketFloats(String packet){
        String[] split = packet.split(":");
        float[] floats = {Float.valueOf(split[2]), Float.valueOf(split[3]), Float.valueOf(split[4]), Float.valueOf(split[5])};
        return floats;
    }

    public synchronized void sendPlayerAction(String s){

    }
}
