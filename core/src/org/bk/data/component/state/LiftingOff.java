package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 10.11.2016.
 */
public class LiftingOff implements Component, Pool.Poolable {
    public static final float LIFTOFF_DURATION = 2;
    public float timeRemaining = LIFTOFF_DURATION;
    public Entity from;

    @Override
    public void reset() {
        timeRemaining = LIFTOFF_DURATION;
        from = null;
    }
}
