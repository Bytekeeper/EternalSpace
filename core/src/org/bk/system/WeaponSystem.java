package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.EntityFactory;
import org.bk.Game;
import org.bk.component.*;

import static org.bk.component.Mapper.*;

/**
 * Created by dante on 15.10.2016.
 */
public class WeaponSystem extends IteratingSystem {
    private static final float MAX_PROJECTILE_LIFETIME = 20;
    private final Game game;

    public WeaponSystem(Game game, int priority) {
        super(Family.all(Mounts.class, Transform.class).get(), priority);
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Mounts mounts = MOUNTS.get(entity);
        for (Mounts.Weapon weapon: mounts.weapons) {
            weapon.cooldown = Math.max(0, weapon.cooldown - deltaTime);
            if (weapon.cooldown > 0 || !weapon.firing) {
                continue;
            }
            weapon.cooldown = weapon.cooldownPerShot;
            if (weapon.projectileDefinition != null) {
                spawnProjectile(weapon, entity, weapon.projectileDefinition);
            }
        }
    }

    private void spawnProjectile(Mounts.Weapon weapon, Entity owner, EntityFactory.EntityDefinitionKey projectileSpec) {
        if (projectileSpec == null) {
            Gdx.app.error(WeaponSystem.class.getSimpleName(), "No ProjectileSpec");
            return;
        }
        if (owner == null) {
            Gdx.app.error(WeaponSystem.class.getSimpleName(), "Mounts without owner");
            return;
        }
        Transform sourceTransform = TRANSFORM.get(owner);
        Movement sourceMovement = MOVEMENT.get(owner);

        Entity projectileEntity = game.spawn(weapon.projectileDefinition, Transform.class, Movement.class, Projectile.class, Physics.class);

        Transform projectileTransform = TRANSFORM.get(projectileEntity);
        projectileTransform.orientRad = (sourceTransform.orientRad + weapon.orientRad) % MathUtils.PI2;
        projectileTransform.location.set(weapon.offset).rotateRad(sourceTransform.orientRad).add(sourceTransform.location);
        Projectile projectile = PROJECTILE.get(projectileEntity);
        projectile.owner = owner;
        Movement movement = MOVEMENT.get(projectileEntity);
        movement.maxVelocity = 2000;
        movement.velocity.set(Vector2.X).rotateRad(sourceTransform.orientRad).scl(projectile.initialSpeed).add(sourceMovement.velocity);
    }
}
