package com.game.bb.handlers;

import com.game.bb.gamestates.ConnectionState;
import com.game.bb.gamestates.StartScreenState;
import com.game.bb.gamestates.PlayState;
import com.game.bb.main.Game;
import com.game.bb.gamestates.GameState;
import com.game.bb.net.GameServer;

import java.util.Stack;

/**
 * Created by erik on 06/05/16.
 */
public class GameStateManager {
    private Game game;
    private Stack<GameState> states;
    private String ipAddress = "192.168.0.103";
    private int port = 8080;
    private boolean hosting = false;
    public static final int PLAY = 1, START_SCREEN = 2, CONNECTION_STATE = 3;


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
        if (state == PLAY) {
            if (hosting) {
                new GameServer(port).start();
            }
            return new PlayState(this, ipAddress, port);
        } else if (state == START_SCREEN) {
            return new StartScreenState(this);
        } else if (state == CONNECTION_STATE) {
            return new ConnectionState(this);
        }
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

    public void hostGame(boolean hosting) {
        this.hosting = hosting;
    }
}

