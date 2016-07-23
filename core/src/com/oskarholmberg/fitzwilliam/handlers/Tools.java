package com.oskarholmberg.fitzwilliam.handlers;

/**
 * Created by erik on 26/05/16.
 */
public class Tools {
    private static int entityAccum;

    public static void init(){
        entityAccum = 0;
    }

    public static int getPlayerId(int entityId){
        String idString = Integer.toString(entityId);
        idString = idString.substring(0, 2);
        return Integer.valueOf(idString);
    }

    public static int newEntityId(){
        entityAccum++;
        String temp = Integer.toString(B2DVars.MY_ID) + entityAccum;
        return Integer.valueOf(temp);
    }
}
