package com.game.bb.handlers;


/**
 * Created by erik on 06/05/16.
 */
public class B2DVars {

    // PPM ratio
    public static final float PPM = 100;

    public static final int SCALE = 2;
    public final static int CAM_WIDTH = 240*SCALE, CAM_HEIGHT = 160*SCALE;

    public static final String MY_ID = Long.toString(System.currentTimeMillis() + (int) Math.random()*100);

    // collision bits
    public static final short BIT_GROUND = 2, BIT_PLAYER = 4, BIT_OPPONENT = 8, BIT_BULLET = 16;

    public static final int PLAYER_HEIGHT = 11, PLAYER_WIDTH = 9;

    public static final String ID_FOOT = "foot", ID_PLAYER = "player", ID_OPPONENT = "opponent",
    ID_GROUND = "ground", ID_BULLET = "bullet";
}
