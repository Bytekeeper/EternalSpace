package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.component.Landing;
import org.bk.component.Physics;
import org.bk.component.Steering;
import org.bk.component.Transform;

import static org.bk.component.Mapper.LANDING;
import static org.bk.component.Mapper.PHYSICS;
import static org.bk.component.Mapper.STEERING;

/**
 * Created by dante on 18.10.2016.
 */
public class LandingSystem extends IteratingSystem {
    public LandingSystem(int priority) {
        super(Family.all(Landing.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Landing landing = LANDING.get(entity);
        if (PHYSICS.has(entity)) {
            entity.remove(Physics.class);
            landing.hasPhysics = true;
        }
        if (STEERING.has(entity)) {
            entity.remove(Steering.class);
            landing.hasSteering = true;
        }
        landing.timeRemaining -= deltaTime;
        if (landing.timeRemaining <= 0) {
            if (landing.isLiftingOff) {
                if (landing.hasPhysics) {
                    entity.add(getEngine().createComponent(Physics.class));
                    landing.hasPhysics = false;
                }
                if (landing.hasSteering) {
                    entity.add(getEngine().createComponent(Steering.class));
                    landing.hasSteering = true;
                }
                entity.remove(Landing.class);
            } else {
                getEngine().removeEntity(entity);
            }
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
