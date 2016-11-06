package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.Health;
import org.bk.data.component.Steering;

import static org.bk.data.component.Mapper.HEALTH;

/**
 * Created by dante on 16.10.2016.
 */
public class HealthSystem extends IteratingSystem {
    public HealthSystem(int priority) {
        super(Family.all(Health.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Health health = HEALTH.get(entity);
        if (health.hull == 0) {
            entity.remove(Steering.class);
            getEngine().removeEntity(entity);
        }
    }
}
