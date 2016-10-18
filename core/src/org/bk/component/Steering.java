package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 16.10.2016.
 */
public class Steering implements Component {
    public float thrust;
    public float turn;
    public boolean land;

    public Steerable<Vector2> steerable;
    public Location<Vector2> targetLocation;
    public Entity targetEntity;
    public SteeringBehavior<Vector2> behavior;
}
