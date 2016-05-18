package com.game.bb.net;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.game.bb.entities.SPGrenade;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.B2DVars;

/**
 * Created by erik on 13/05/16.
 */
public class PlayStateNetworkMonitor {
    private Array<String> opponentActions;
    private GameClient client;
    private PlayState ps;
    private String ipAddress;

    public PlayStateNetworkMonitor(PlayState ps, String ipAddress) {
        this.ps = ps;
        this.ipAddress = ipAddress;
        opponentActions = new Array<String>();
        init();
    }

    private void init() {
        System.out.println("Starting client.");
        client = new GameClient(this, ipAddress);
        client.start();
    }

    public float[] getPacketFloats(String[] split) {
        if (split.length > 5) {
            float[] floats = {Float.valueOf(split[2]), Float.valueOf(split[3]), Float.valueOf(split[4]), Float.valueOf(split[5])};
            return floats;
        }
        return null;
    }

    public synchronized void sendPlayerAction(String action, float xForce, float yForce, String... misc) {
        String packet = B2DVars.MY_ID + ":" + action + ":" + xForce + ":" + yForce + ":" + ps.playerPosition().x + ":" +
                ps.playerPosition().y;
        for (String s : misc) {
            packet += (":" + s);
        }
        client.sendData(packet.getBytes());
    }

    public synchronized void addOpponentAction(String action) {
        opponentActions.add(action);
    }

    public synchronized String getOpponentAction() {
        if (opponentActions.size != 0) {
            return opponentActions.pop();
        } else {
            return "NO_ACTION";
        }
    }
}
