package com.game.bb.net.packets;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by erik on 18/05/16.
 */
public class PlayerMovementPacket implements Pool.Poolable{
    public float xp, yp, xv, yv;
    public int tex, sound, seq, id;

    @Override
    public void reset() {
        id=0;
    }
}
