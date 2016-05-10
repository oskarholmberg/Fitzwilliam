package com.game.bb.handlers;

import com.game.bb.main.Game;
import com.game.bb.gamestates.GameState;
import com.game.bb.network.NetworkMonitor;
import com.game.bb.network.ServerReader;
import com.game.bb.network.ServerWriter;
import java.util.Stack;

/**
 * Created by erik on 06/05/16.
 */
public class GameStateManager {
    private Game game;
    private Stack<GameState> states;
    private int PLAY = 123123;
    private NetworkMonitor mon;

    public GameStateManager(Game game, String address, int port){
        this.game=game;
        states = new Stack<GameState>();
        mon = new NetworkMonitor();
        ServerWriter serverWriter = new ServerWriter(mon, address, port);
        if(serverWriter.isConnected()) serverWriter.start();
        ServerReader serverReader = new ServerReader(mon, address, port + 1);
        if(serverReader.isConnected()) serverReader.start();
        pushState(PLAY);
    }

    public NetworkMonitor getMonitor(){
        return mon;
    }

    public Game game(){return game;}

    public void update(float dt){
        states.peek().update(dt);
    }
    public void render(){
        states.peek().render();
    }

    private GameState getState(int state){
        if(state == PLAY)
            return new com.game.bb.gamestates.Play(this);
        return null;
    }

    public void setState(int state){
        popState();
        pushState(state);
    }

    public void pushState(int state){
        states.push(getState(state));
    }

    public void popState(){
        states.pop().dispose();
    }
}
