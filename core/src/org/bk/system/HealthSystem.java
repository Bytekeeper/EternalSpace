package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.Game;
import org.bk.data.component.Body;
import org.bk.data.component.Effect;
import org.bk.data.component.Health;
import org.bk.data.component.Transform;

import static org.bk.data.component.Mapper.BODY;
import static org.bk.data.component.Mapper.HEALTH;
import static org.bk.data.component.Mapper.TRANSFORM;

/**
 * Created by dante on 16.10.2016.
 */
public class HealthSystem extends IteratingSystem {
    private final Game game;

    public HealthSystem(Game game) {
        super(Family.all(Health.class).get());
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Health health = HEALTH.get(entity);
        if (health.hull == 0) {
            Transform entityTransform = TRANSFORM.get(entity);
            Body body = BODY.get(entity);
            if (entityTransform != null && body != null && body.explosionEffect != null) {
                Entity explosionEntity = getEngine().createEntity();
                Transform explTransform = getEngine().createComponent(Transform.class);
                explTransform.location.set(entityTransform.location);
                Effect explEffect = getEngine().createComponent(Effect.class);
                explEffect.effect = body.explosionEffect;
                explosionEntity.add(explTransform);
                explosionEntity.add(explEffect);
                getEngine().addEntity(explosionEntity);
            }
            game.entityDestroyed.dispatch(entity);
            getEngine().removeEntity(entity);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
