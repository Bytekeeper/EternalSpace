package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 18.10.2016.
 */
public class Landing implements Component, Pool.Poolable {
    public static final float LAND_OR_LIFTOFF_DURATION = 2;
    public LandingDirection landingDirection = LandingDirection.LANDING;
    public float timeRemaining = LAND_OR_LIFTOFF_DURATION;
    public Entity target;
    public boolean landed;

    @Override
    public void reset() {
        landed = false;
        timeRemaining = LAND_OR_LIFTOFF_DURATION;
        target = null;
        landingDirection = LandingDirection.LANDING;
    }

    public enum LandingDirection {
        LANDING,
        DEPART;
    }
}
