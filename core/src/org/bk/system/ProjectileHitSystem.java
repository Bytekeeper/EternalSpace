package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.Game;
import org.bk.data.component.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class ProjectileHitSystem extends IteratingSystem {
    private final Game game;

    public ProjectileHitSystem(Game game, int priority) {
        super(Family.all(Projectile.class, Touching.class).get(), priority);
        this.game = game;
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Projectile projectile = PROJECTILE.get(entity);
        Owned owned = OWNED.get(entity);
        Touching touching = TOUCHING.get(entity);
        float damagePerEntity = projectile.yield / touching.touchList.size;
        for (Entity e: touching.touchList) {
            Damage damage = DAMAGE.get(e);
            if (damage == null) {
                damage = getEngine().createComponent(Damage.class);
                e.add(damage);
            }
            float damageRem = damagePerEntity;
            damage.overall += damageRem;
            if (owned != null && owned.owner == game.playerEntity) {
                damage.byPlayer += damageRem;
            }
            Shield shield = SHIELD.get(e);
            if (shield != null) {
                float shieldDamage = Math.min(shield.shields, damageRem);
                shield.shields -= shieldDamage;
                damageRem -= shieldDamage;
            }
            Health health = HEALTH.get(e);
            if (health != null) {
                health.hull -= Math.min(health.hull, damageRem);
            }
        }
        getEngine().removeEntity(entity);
    }
}
