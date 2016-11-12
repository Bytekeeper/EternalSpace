package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.SolarSystem;

/**
 * Created by dante on 10.11.2016.
 */
public class JumpingIn implements Component, Pool.Poolable {
    public static final float JUMP_IN_DURATION = 3;
    public float timeRemaining = JUMP_IN_DURATION;
    public final Vector2 arriveAt = new Vector2();
    public SolarSystem from;

    @Override
    public void reset() {
        arriveAt.setZero();
        timeRemaining = JUMP_IN_DURATION;
        from = null;
    }
}
