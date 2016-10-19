package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 18.10.2016.
 */
public class Landing implements Component, Pool.Poolable {
    public boolean isLiftingOff;
    public float duration;
    public float timeRemaining;
    public Entity target;
    public boolean hasPhysics;
    public boolean hasSteering;

    @Override
    public void reset() {
        isLiftingOff = hasPhysics = hasSteering = false;
        duration = timeRemaining = 0;
        target = null;
    }
}
