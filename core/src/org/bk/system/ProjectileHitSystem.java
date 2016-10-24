package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.Health;
import org.bk.data.component.Projectile;
import org.bk.data.component.Touching;

import static org.bk.data.component.Mapper.HEALTH;
import static org.bk.data.component.Mapper.PROJECTILE;
import static org.bk.data.component.Mapper.TOUCHING;

/**
 * Created by dante on 16.10.2016.
 */
public class ProjectileHitSystem extends IteratingSystem {
    public ProjectileHitSystem(int priority) {
        super(Family.all(Projectile.class, Touching.class).get(), priority);
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Projectile projectile = PROJECTILE.get(entity);
        Touching touching = TOUCHING.get(entity);
        float damagePerEntity = projectile.yield / touching.touchList.size;
        for (Entity e: touching.touchList) {
            Health health = HEALTH.get(e);
            if (health != null) {
                float damageRem = damagePerEntity;
                float shieldDamage = Math.min(health.shields, damageRem);
                health.shields -= shieldDamage;
                damageRem -= shieldDamage;
                health.hull = Math.max(health.hull - damageRem, 0);
            }
        }
        getEngine().removeEntity(entity);
    }
}
