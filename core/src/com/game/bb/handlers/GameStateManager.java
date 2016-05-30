package com.game.bb.handlers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.game.bb.gamestates.ConnectionState;
import com.game.bb.gamestates.GameOverState;
import com.game.bb.gamestates.HostOfflineState;
import com.game.bb.gamestates.JoinServerState;
import com.game.bb.gamestates.StartScreenState;
import com.game.bb.gamestates.PlayState;
import com.game.bb.handlers.pools.Pooler;
import com.game.bb.main.Game;
import com.game.bb.gamestates.GameState;
import com.game.bb.net.client.GameClient;
import com.game.bb.net.server.GameServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Stack;

/**
 * Created by erik on 06/05/16.
 */
public class GameStateManager {
    private Game game;
    private Stack<GameState> states;
    private GameClient client;
    private GameServer server;
    private int mapNbr;
    private boolean hosting = false;
    public static final int PLAY = 1, START_SCREEN = 2, CONNECT = 3, JOIN_SERVER = 4, HOST_OFFLINE = 5, GAME_OVER = 6;
    public ArrayMap<String, Array<String>> killedByEntities;
    public String victoryOrder;


    public GameStateManager(Game game) {
        this.game = game;
        states = new Stack<GameState>();
        Pooler.init();
        Assets.init();
        pushState(START_SCREEN);
    }

    public void setMapSelection(int mapNbr) {
        this.mapNbr = mapNbr;
    }

    public void setKilledByEntities(ArrayMap<String, Array<String>> entities) {
        killedByEntities = entities;
    }

    public void setVictoryOrder(String order) {
        victoryOrder = order;
    }

    public boolean isHosting() {
        return hosting;
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
        switch (state) {
            case PLAY:
                if (hosting) {
                    server = new GameServer(mapNbr);
                    client = new GameClient();
                    try {
                        client.connectToServer(InetAddress.getLocalHost());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                return new PlayState(this, client);
            case START_SCREEN:
                return new StartScreenState(this);
            case CONNECT:
                return new ConnectionState(this);
            case JOIN_SERVER:
                return new JoinServerState(this);
            case HOST_OFFLINE:
                return new HostOfflineState(this);
            case GAME_OVER:
                if (server != null) server.stop();
                if (client != null) client.stop();
                return new GameOverState(this, killedByEntities, victoryOrder);
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

    public void setClient(GameClient client) {
        this.client = client;
    }

}

