package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 18.10.2016.
 */
public class Landing implements Component, Pool.Poolable {
    public static final float LANDING_DURATION = 2;
    public float timeRemaining = LANDING_DURATION;
    public Entity target;

    @Override
    public void reset() {
        timeRemaining = LANDING_DURATION;
        target = null;
    }
}
