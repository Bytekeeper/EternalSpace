package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 16.10.2016.
 */
public class Steering implements Component, Pool.Poolable {
    public float thrust;
    public float turn;
    public boolean land;

    public Steerable<Vector2> steerable;
    public Location<Vector2> targetLocation;
    public Entity targetEntity;
    public SteeringBehavior<Vector2> behavior;
    public boolean primaryFire;

    @Override
    public void reset() {
        thrust = turn = 0;
        steerable = null;
        targetEntity = null;
        targetLocation = null;
        behavior = null;
        land = primaryFire = false;
    }
}
