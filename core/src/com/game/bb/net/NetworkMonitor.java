package com.game.bb.net;

import com.badlogic.gdx.utils.Array;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 13/05/16.
 */
public class NetworkMonitor {
    private Array<String> opponentActions;
    private GameClient client;

    public NetworkMonitor(){
        opponentActions = new Array<String>();
        init();
    }

    private void init(){
        client = new GameClient(this, "localhost", 8080);
        client.start();
        Runtime.getRuntime().addShutdownHook(new Thread(client.getDisconnecter()));
        sendPlayerAction(netPacketBuilder(B2DVars.MY_ID, "CONNECT", B2DVars.CAM_WIDTH/2/B2DVars.PPM,
                B2DVars.CAM_HEIGHT/B2DVars.PPM, 0, 0, Short.toString(B2DVars.BIT_OPPONENT), B2DVars.ID_OPPONENT, "red"));
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

    public synchronized void sendPlayerAction(String packet){
        System.out.println("Sending: " + packet);
        client.sendData(packet.getBytes());
    }
    public synchronized void addOpponentAction(String action){
        opponentActions.add(action);
    }
    public synchronized String getOpponentAction() {
        if (opponentActions.size!=0) {
            return opponentActions.pop();
        } else {
            return "NO_ACTION";
        }
    }
}
