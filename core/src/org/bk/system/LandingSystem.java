package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.Landing;
import org.bk.data.component.Persistence;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 18.10.2016.
 */
public class LandingSystem extends IteratingSystem {
    public LandingSystem(int priority) {
        super(Family.all(Landing.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(Physics.class);
        entity.remove(Steering.class);
        Landing landing = LANDING.get(entity);
        landing.timeRemaining = Math.max(0, landing.timeRemaining - deltaTime);
        if (landing.timeRemaining <= 0) {
            Persistence persistence = PERSISTENCE.get(entity);
            if (landing.landingDirection == Landing.LandingDirection.DEPART) {
                entity.add(getEngine().createComponent(Physics.class));
                entity.add(getEngine().createComponent(Steering.class));
                entity.remove(Landing.class);
            } else if (persistence != null && persistence.temporary) {
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
