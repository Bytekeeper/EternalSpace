package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.LifeTime;

import static org.bk.data.component.Mapper.LIFE_TIME;

/**
 * Created by dante on 16.10.2016.
 */
public class LifeTimeSystem extends IteratingSystem {
    public LifeTimeSystem() {
        super(Family.all(LifeTime.class).get());
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
