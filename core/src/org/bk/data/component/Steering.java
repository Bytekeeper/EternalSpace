package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.SolarSystem;

/**
 * Created by dante on 16.10.2016.
 */
public class Steering implements Component, Pool.Poolable {
    public float thrust;
    public float turn;
    public Steerable<Vector2> steerable;

    @Override
    public void reset() {
        thrust = turn = 0;
        steerable = null;
    }
}
