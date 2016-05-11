package com.game.bb.handlers;

/**
 * Created by erik on 06/05/16.
 */
public class B2DVars {

    // PPM ratio
    public static final float PPM = 100;

    public static final String MY_ID = "Oskar" + System.currentTimeMillis();

    // collision bits
    public static final short BIT_GROUND = 2, BIT_PLAYER = 4, BIT_OPPONENT = 8, BIT_BULLET = 16;

    public static final String ID_FOOT = "foot", ID_PLAYER = "player", ID_OPPONENT = "opponent",
    ID_GROUND = "ground", ID_BULLET = "bullet";
}
