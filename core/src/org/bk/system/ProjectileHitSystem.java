package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import org.bk.Game;
import org.bk.data.component.*;
import org.bk.data.component.Character;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class ProjectileHitSystem extends IteratingSystem {
    private final Game game;
    private final Array<Entity> toDamage = new Array<Entity>();

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
        toDamage.clear();
        Owned owned = OWNED.get(entity);
        Touching touching = TOUCHING.get(entity);
        for (Entity potentialTarget : touching.touchList) {
            Owned targetOwned = OWNED.get(potentialTarget);
            Character targetCharacter = CHARACTER.get(potentialTarget);
            if (targetOwned != null) {
                if (targetOwned.affiliation.isEnemy(owned.affiliation)) {
                    toDamage.add(potentialTarget);
                }
            } else if (targetCharacter != null) {
                if (targetCharacter.faction.isEnemy(owned.affiliation)) {
                    toDamage.add(potentialTarget);
                }
            } else {
                toDamage.add(potentialTarget);
            }
        }

        if (toDamage.size == 0) {
            return;
        }

        Projectile projectile = PROJECTILE.get(entity);
        float damagePerEntity = projectile.yield / toDamage.size;
        for (Entity e: toDamage) {
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
