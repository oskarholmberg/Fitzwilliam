package com.oskarholmberg.fitzwilliam.gamestates;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.oskarholmberg.fitzwilliam.handlers.Assets;
import com.oskarholmberg.fitzwilliam.handlers.pools.Pooler;
import com.oskarholmberg.fitzwilliam.main.Game;
import com.oskarholmberg.fitzwilliam.net.client.GameClient;
import com.oskarholmberg.fitzwilliam.net.server.GameServer;

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
    public static final int PLAY = 1, START_SCREEN = 2, CONNECT = 3, JOIN_SERVER = 4, HOST_OFFLINE = 5,
            GAME_OVER = 6, MAP_SELECTION = 7;
    public ArrayMap<String, Array<String>> killedByEntities;
    public String victoryOrder;


    public GameStateManager(Game game) {
        this.game = game;
        states = new Stack<GameState>();
        Pooler.init();
        Assets.init();
        pushState(START_SCREEN);
    }

    public void setMapSelection(int mapNbr){
        this.mapNbr=mapNbr;
    }

    public void setKilledByEntities(ArrayMap<String, Array<String>> entities){
        killedByEntities = entities;
    }

    public void setVictoryOrder(String order){
        victoryOrder=order;
    }

    public boolean isHosting(){
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
        switch (state){
            case PLAY:
                if(hosting){
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
                return new HostOrJoinState(this);
            case JOIN_SERVER:
                return new JoinServerState(this);
            case HOST_OFFLINE:
                return new HostOfflineState(this);
            case GAME_OVER:
                hosting = false;
                return new GameOverState(this, killedByEntities, victoryOrder);
            case MAP_SELECTION:
                return new MapSelectState(this);
            default:
                return null;
        }
    }

    public void setState(int state) {
        //Make sure server and clients stop when states are changed.
        if(state != PLAY){
            if(server != null) server.stop();
            if(client != null) client.stop();
        }
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

    public void setClient(GameClient client){
        this.client=client;
    }

}

