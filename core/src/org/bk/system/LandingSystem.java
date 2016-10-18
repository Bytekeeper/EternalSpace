package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.component.Landing;
import org.bk.component.Transform;

import static org.bk.component.Mapper.LANDING;

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
        landing.timeRemaining -= deltaTime;
        if (landing.timeRemaining <= 0) {
            if (landing.isLiftingOff) {
                entity.remove(Landing.class);
            } else {
                getEngine().removeEntity(entity);
            }
        }
    }
}
