package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import org.bk.system.SystemPopulateSystem;

/**
 * Created by dante on 16.10.2016.
 */
public class Steering implements Component, Pool.Poolable {
    public float thrust;
    public float turn;
    public SteeringMode mode = SteeringMode.FREE_FLIGHT;
    public SystemPopulateSystem.SystemKey jumpTo;
    public Steerable<Vector2> steerable;
    public Entity modeTargetEntity;
    public boolean primaryFire;

    @Override
    public void reset() {
        thrust = turn = 0;
        steerable = null;
        modeTargetEntity = null;
        jumpTo = null;
        mode = SteeringMode.FREE_FLIGHT;
    }

    public enum SteeringMode {
        FREE_FLIGHT,
        LANDING,
        JUMPING;
    };
}
