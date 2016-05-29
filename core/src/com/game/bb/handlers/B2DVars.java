package com.game.bb.handlers;


/**
 * Created by erik on 06/05/16.
 */
public class B2DVars {

    // PPM ratio
    public static final float PPM = 100f;

    public static final int SCALE = 1;
    public final static int DEFAULT_GAME_WIDTH = 960*SCALE, DEFAULT_GAME_HEIGHT = 640*SCALE;
    public final static int AMOUNT_BULLET = 3, AMOUNT_GRENADE = 1, AMOUNT_LIVES = 5;

    public final static float VOID_X = 200f, VOID_Y = 200f;

    public final static float PH_JUMPX = 70f, PH_JUMPY = 200f, RESPAWN_TIME= 3f, PH_BULLET_SPEED = 300f,
            PH_GRENADE_X = 300f, PH_GRENADE_Y = -200f;

    public static int MY_ID = -123;
    public static String MY_COLOR = "blue";

    // collision bits
    public static final short BIT_GROUND = 2, BIT_PLAYER = 4, BIT_OPPONENT = 8, BIT_BULLET = 16,
            BIT_GRENADE = 32, BIT_DOME = 64, BIT_ENEMY_ENTITY = 128, BIT_BOUNCE = 256;

    public static final int POWERTYPE_AMMO = 1, POWERTYPE_TILTSCREEN = 2, POWERTYPE_SHIELD = 3;
    public static final int POWERTYPE_AMOUNT = 3;

    public static final float PLAYER_HEIGHT = 22f/PPM, PLAYER_WIDTH = 18f/PPM;

    public static final String ID_FOOT = "foot", ID_PLAYER = "player", ID_OPPONENT = "opponent",
    ID_GROUND = "ground", ID_BULLET = "bullet", ID_GRENADE = "grenade", ID_DOME = "dome",
    ID_POWERUP = "powerup", ID_ENEMY_GRENADE = "enemygrenade", ID_ENEMY_ENTITY = "enemyentity",
    ID_BOUNCE = "bounce";

    public static final int NET_CONNECT = 1, NET_RESPAWN = 2, NET_DEATH = 3, NET_SHOOT = 4,
            NET_GRENADE = 5, NET_DISCONNECT = 6, NET_DESTROY_BODY = 7, NET_POWER = 8, NET_NEW_ENTITY = 9,
            NET_APPLY_ANTIPOWER = 10, NET_GAME_OVER = 11, NET_REMOVE_ME = 12, NET_SERVER_INFO = 13;

    public static final int TYPE_BULLET = 1, TYPE_GRENADE = 2;

    public static final float ENTITY_UPDATE_FREQ = 1/30f, MOVEMENT_UPDATE_FREQ = 1/30f;

    public static final String COLOR_RED = "red", COLOR_BLUE = "blue", COLOR_GREEN = "green",
            COLOR_YELLOW = "yellow";

    public static void setMyId(int id){
        MY_ID = id;
    }
    public static void setMyColor(String color){
        MY_COLOR = color;
    }
}
