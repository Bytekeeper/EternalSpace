package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.component.LifeTime;

import static org.bk.component.Mapper.LIFE_TIME;

/**
 * Created by dante on 16.10.2016.
 */
public class LifeTimeSystem extends IteratingSystem {
    public LifeTimeSystem(int priority) {
        super(Family.all(LifeTime.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LifeTime lifeTime = LIFE_TIME.get(entity);
        lifeTime.remaining -= deltaTime;
        if (lifeTime.remaining <= 0) {
            getEngine().removeEntity(entity);
        }
    }
}
