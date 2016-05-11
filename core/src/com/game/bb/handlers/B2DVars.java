package com.game.bb.handlers;


/**
 * Created by erik on 06/05/16.
 */
public class B2DVars {

    // PPM ratio
    public static final float PPM = 100;

    public static final String MY_ID = Long.toString(System.currentTimeMillis() + (int) Math.random()*100);

    // collision bits
    public static final short BIT_GROUND = 2, BIT_PLAYER = 4, BIT_OPPONENT = 8, BIT_BULLET = 16;
}
