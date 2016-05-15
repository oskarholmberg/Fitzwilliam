package com.game.bb.handlers;


/**
 * Created by erik on 06/05/16.
 */
public class B2DVars {

    // PPM ratio
    public static final float PPM = 100;

    public static final int SCALE = 1;
    public final static int CAM_WIDTH = 960*SCALE, CAM_HEIGHT = 640*SCALE;

    public final static float PH_JUMPX = 70, PH_JUMPY = 200, RESPAWN_TIME= 3f, PH_BULLET_SPEED = 200,
            PH_GRENADE_X = 300, PH_GRENADE_Y = -200;

    public static final String MY_ID = Long.toString(System.currentTimeMillis()) + Math.random();

    // collision bits
    public static final short BIT_GROUND = 2, BIT_PLAYER = 4, BIT_OPPONENT = 8, BIT_BULLET = 16, BIT_GRENADE = 32;

    public static final float PLAYER_HEIGHT = 22/PPM, PLAYER_WIDTH = 18/PPM;

    public static final String ID_FOOT = "foot", ID_PLAYER = "player", ID_OPPONENT = "opponent",
    ID_GROUND = "ground", ID_BULLET = "bullet", ID_GRENADE = "grenade";
}
