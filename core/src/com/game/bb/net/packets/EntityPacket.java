package com.game.bb.net.packets;

import com.badlogic.gdx.utils.Pool;

public class EntityPacket implements Pool.Poolable{
    public float xp, yp, xf, yf;
    public int id, type, alive;

    @Override
    public void reset() {
        id=-1;
    }
}
