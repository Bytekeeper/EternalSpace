package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.component.Landing;
import org.bk.component.Persistence;
import org.bk.component.Physics;
import org.bk.component.Steering;

import static org.bk.component.Mapper.*;

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
        landing.timeRemaining = Math.max(0, landing.timeRemaining - deltaTime);
        if (landing.timeRemaining <= 0) {
            Persistence persistence = PERSISTENCE.get(entity);
            if (landing.landingDirection == Landing.LandingDirection.DEPART) {
                if (landing.hasPhysics) {
                    entity.add(getEngine().createComponent(Physics.class));
                    landing.hasPhysics = false;
                }
                if (landing.hasSteering) {
                    entity.add(getEngine().createComponent(Steering.class));
                    landing.hasSteering = true;
                }
                entity.remove(Landing.class);
            } else if (persistence != null && persistence.temporary){
                getEngine().removeEntity(entity);
            } else {
                landing.landed = true;
            }
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
