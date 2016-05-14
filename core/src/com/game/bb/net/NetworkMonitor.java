package com.game.bb.net;

import com.badlogic.gdx.utils.Array;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 13/05/16.
 */
public class NetworkMonitor {
    private Array<String> opponentActions;
    private GameClient client;
    private PlayState ps;

    public NetworkMonitor(PlayState ps) {
        this.ps=ps;
        opponentActions = new Array<String>();
        init();
    }

    private void init(){
        client = new GameClient(this, "localhost", 8080);
        client.start();
        Runtime.getRuntime().addShutdownHook(new Thread(client.getDisconnecter()));
        String initConnect = B2DVars.MY_ID + ":" + "CONNECT" + ":" + "0" + ":" + "0" + ":" + B2DVars.CAM_WIDTH/2/B2DVars.PPM +
                ":" + B2DVars.CAM_HEIGHT/B2DVars.PPM + ":" + B2DVars.BIT_OPPONENT + ":" + B2DVars.ID_OPPONENT + ":" + "red";
        client.sendData(initConnect.getBytes());
    }

    public String netPacketBuilder(String ID, String command, float xPos, float yPos, float xForce, float yForce, String... misc ){
        String packet = ID + ":" + command + ":" + xPos + ":" + yPos + ":" + xForce + ":" + yForce;
        for (String s : misc){
            packet += (":" + s);
        }
        return packet;
    }

    public float[] getPacketFloats(String[] split){
        if (split.length > 5) {
            float[] floats = {Float.valueOf(split[2]), Float.valueOf(split[3]), Float.valueOf(split[4]), Float.valueOf(split[5])};
            return floats;
        }
        return null;
    }

    public synchronized void sendPlayerAction(String action, float xForce, float yForce, String... misc){
        String packet = B2DVars.MY_ID + ":" + action + ":" + xForce + ":" + yForce + ":" + ps.playerPosition().x + ":" +
                ps.playerPosition().y;
        for (String s : misc){
            packet += (":" + s);
        }
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
