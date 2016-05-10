package com.game.bb.network;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by erik on 08/05/16.
 */
public class NetworkMonitor {
    private ArrayList<String> actions;
    private ArrayList<String> opponentActions;
    private int i = 0;

    public NetworkMonitor(){
        actions = new ArrayList<String>();
        opponentActions = new ArrayList<String>();
    }

    public synchronized String getActions(){
        if(actions.isEmpty()){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return actions.remove(0);
    }

    public synchronized void bounceBall(){
        addAction("bounce the ball");
    }

    public synchronized void addAction(String action){
        actions.add(action);
        notifyAll();
    }

    public synchronized void addOpponentAction(String action){
        opponentActions.add(action);
    }

    public String getOpponentAction(){
        if(!opponentActions.isEmpty()) {
            return opponentActions.remove(0);
        } else {
            return "NO_ACTION";
        }
    }
}
