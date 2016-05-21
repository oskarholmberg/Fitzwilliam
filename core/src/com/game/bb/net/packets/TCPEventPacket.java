package com.game.bb.net.packets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by erik on 17/05/16.
 */
public class TCPEventPacket implements Pool.Poolable{
    public int action;
    public Vector2 pos;
    public int id, misc;

    @Override
    public void reset() {
        id = 0;
    }
}
