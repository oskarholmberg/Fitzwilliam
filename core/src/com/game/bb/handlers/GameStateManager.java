package com.game.bb.handlers;

import com.game.bb.gamestates.ConnectionState;
import com.game.bb.gamestates.LobbyState;
import com.game.bb.gamestates.StartScreenState;
import com.game.bb.gamestates.PlayState;
import com.game.bb.main.Game;
import com.game.bb.gamestates.GameState;
import com.game.bb.net.GameServer;
import com.game.bb.net.PlayStateNetworkMonitor;
import com.game.bb.net.server.GameServerNew;

import java.util.Stack;

/**
 * Created by erik on 06/05/16.
 */
public class GameStateManager {
    private Game game;
    private Stack<GameState> states;
    private String ipAddress;
    private boolean hosting = false;
    public static final int PLAY = 1, START_SCREEN = 2, CONNECT = 3, LOBBY = 4;


    public GameStateManager(Game game) {
        this.game = game;
        states = new Stack<GameState>();
        pushState(START_SCREEN);
        //pushState(PLAY); //remove this later
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
        switch (state){
            case PLAY:
                if(hosting){
                    new GameServerNew();
                    new GameServer().start();
                }
                PlayState ps = new PlayState(this);
                PlayStateNetworkMonitor mon = new PlayStateNetworkMonitor(ps, ipAddress);
                ps.setNetworkMonitor(mon);
                return ps;
            case START_SCREEN:
                return new StartScreenState(this);
            case CONNECT:
                return new ConnectionState(this);
            case LOBBY:
                return new LobbyState(this);
            default:
                return null;
        }
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

    public void hostGame(boolean hosting) {
        this.hosting = hosting;
    }

    public void setIpAddress(String ip){
        ipAddress = ip;
    }
}

