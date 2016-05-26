package com.game.bb.net.packets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by erik on 17/05/16.
 */
public class TCPEventPacket implements Pool.Poolable{
    public int action;
    public Vector2 pos, force;
    public int id, misc;
    public String miscString;

    @Override
    public void reset() {
        id = -1;
        miscString="";
        action=-1;
    }
}
