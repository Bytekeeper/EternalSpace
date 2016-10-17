package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.component.*;
import org.bk.spec.ProjectileSpec;

import static org.bk.component.Mapper.MOVEMENT;
import static org.bk.component.Mapper.TRANSFORM;
import static org.bk.component.Mapper.WEAPON;

/**
 * Created by dante on 15.10.2016.
 */
public class WeaponSystem extends IteratingSystem {
    private static final float MAX_PROJECTILE_LIFETIME = 20;

    public WeaponSystem(int priority) {
        super(Family.all(Weapons.class, Transform.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Weapons weapons = WEAPON.get(entity);
        for (Weapons.Weapon weapon: weapons.weapons) {
            weapon.cooldown = Math.max(0, weapon.cooldown - deltaTime);
            if (weapon.cooldown > 0 || !weapon.firing) {
                return;
            }
            weapon.cooldown = weapon.cooldownPerShot;
            if (weapon.projectileSpec != null) {
                spawnProjectile(weapon, entity, weapon.projectileSpec);
            }
        }
    }

    private void spawnProjectile(Weapons.Weapon weapon, Entity owner, ProjectileSpec projectileSpec) {
        if (projectileSpec == null) {
            Gdx.app.error(WeaponSystem.class.getSimpleName(), "No ProjectileSpec");
            return;
        }
        if (owner == null) {
            Gdx.app.error(WeaponSystem.class.getSimpleName(), "Weapons without owner");
            return;
        }
        Transform sourceTransform = TRANSFORM.get(owner);
        Movement sourceMovement = MOVEMENT.get(owner);
        Entity projectileEntity = new Entity();
        Transform projectileTransform = new Transform();
        projectileTransform.orientRad = (sourceTransform.orientRad + weapon.orientRad) % MathUtils.PI2;
        projectileTransform.location.set(weapon.offset).rotateRad(sourceTransform.orientRad).add(sourceTransform.location);
        projectileEntity.add(projectileTransform);
        Projectile projectile = new Projectile();
        projectile.owner = owner;
        projectile.yield = projectileSpec.yield;
        projectileEntity.add(projectile);
        Movement movement = new Movement();
        movement.maxVelocity = 2000;
        movement.velocity.set(Vector2.X).rotateRad(sourceTransform.orientRad).scl(projectileSpec.initialSpeed).add(sourceMovement.velocity);
        projectileEntity.add(movement);
        Body body = new Body();
        body.dimension.set(projectileSpec.dimension);
        projectileEntity.add(body);
        LifeTime lifeTime = new LifeTime();
        lifeTime.remaining = Math.min(MAX_PROJECTILE_LIFETIME, projectileSpec.lifeTime);
        projectileEntity.add(lifeTime);
        getEngine().addEntity(projectileEntity);
    }
}
