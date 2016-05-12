package com.game.bb.handlers;

import com.game.bb.main.Game;
import com.game.bb.gamestates.GameState;
import com.game.bb.net.GameClient;
import com.game.bb.net.GameServer;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by erik on 06/05/16.
 */
public class GameStateManager {
    private Game game;
    private Stack<GameState> states;
    private int PLAY = 123123;
    private ArrayList<String> opponentActions;
    private GameClient client;
    private String ipAddress;
    private int port;
    private boolean host;


    public GameStateManager(Game game, String ipAddress, int port) {
        this.game = game;
        this.ipAddress = ipAddress;
        this.port = port;
        states = new Stack<GameState>();
        opponentActions = new ArrayList<String>();
        init();
        pushState(PLAY);
    }

    private void init(){
        //If host start a server
        if(host){
            new GameServer(port).start();
        }
        //Start client
        client = new GameClient(this, ipAddress, port);
        client.start();
        Runtime.getRuntime().addShutdownHook(new Thread(client.getDisconnecter()));
        byte[] clientInfo = (B2DVars.MY_ID + ":CONNECT:" + B2DVars.CAM_WIDTH/2+":"+B2DVars.CAM_HEIGHT/2+":"+B2DVars.BIT_OPPONENT+":"+B2DVars.ID_OPPONENT+":"+B2DVars.MY_ID+":red").getBytes();
        client.sendData(clientInfo);
    }

    public Game game() {
        return game;
    }

    public void update(float dt) {
        states.peek().update(dt);
    }

    public void render() {
        states.peek().render();
    }

    private GameState getState(int state) {
        if (state == PLAY)
            return new com.game.bb.gamestates.Play(this);
        return null;
    }

    public void setState(int state) {
        popState();
        pushState(state);
    }

    public void pushState(int state) {
        states.push(getState(state));
    }

    public void popState() {
        states.pop().dispose();
    }


    public synchronized void addAction(String action) {
        client.sendData(action.getBytes());
    }

    public synchronized void addOpponentAction(String action){
        opponentActions.add(action);
        notifyAll();
    }

    public synchronized String getOpponentAction() {
        if (!opponentActions.isEmpty()) {
            return opponentActions.remove(0);
        } else {
            return "NO_ACTION";
        }
    }
}

