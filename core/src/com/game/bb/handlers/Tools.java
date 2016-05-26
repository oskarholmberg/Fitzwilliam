package com.game.bb.handlers;

/**
 * Created by erik on 26/05/16.
 */
public class Tools {

    public static int getPlayerId(int entityId){
        String idString = Integer.toString(entityId);
        idString = idString.substring(0, 4);
        return Integer.valueOf(idString);
    }
}
