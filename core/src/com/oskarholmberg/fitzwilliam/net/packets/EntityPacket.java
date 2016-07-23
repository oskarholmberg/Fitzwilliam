package com.oskarholmberg.fitzwilliam.net.packets;

import com.badlogic.gdx.utils.Pool;

public class EntityPacket implements Pool.Poolable{
    public float xp, yp, xf, yf;
    public int id, type, alive;
    public long time;

    @Override
    public void reset() {
        id=-1;
        time = -1;
    }
}
