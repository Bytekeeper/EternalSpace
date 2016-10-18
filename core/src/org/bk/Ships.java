package org.bk;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import org.bk.ai.SteeringUtil;
import org.bk.component.*;
import org.bk.spec.ProjectileSpec;

/**
 * Created by dante on 18.10.2016.
 */
public class Ships {
    public Entity addShip(PooledEngine engine) {
        Entity ship = engine.createEntity();
        Body body = engine.createComponent(Body.class);
        body.dimension.set(35, 40);
        ship.add(body);
        Transform transform = engine.createComponent(Transform.class);
        transform.location.set(400, 300);
        transform.orientRad = 0;
        ship.add(transform);
        ship.add(new Ship());
        Movement movement = engine.createComponent(Movement.class);
        movement.maxVelocity = 500;
        movement.angularThrust = 3000;
        movement.linearThrust = 20000;
        ship.add(movement);
        Health health = engine.createComponent(Health.class);
        health.hull = 100;
        health.shields = 100;
        ship.add(health);

        Mounts.Weapon weapon = new Mounts.Weapon();
        weapon.cooldownPerShot = 0.1f;
        weapon.projectileSpec = new ProjectileSpec();
        weapon.projectileSpec.dimension.set(8, 50);
        weapon.projectileSpec.initialSpeed = 1000;
        weapon.projectileSpec.lifeTime = 2;
        weapon.projectileSpec.yield = 10;
        Mounts mounts = new Mounts();
        mounts.weapons.add(weapon);
        mounts.thrusters.add(new Vector2(-20, 0));
        ship.add(mounts);
        Steering steering = engine.createComponent(Steering.class);
        steering.steerable = SteeringUtil.toSteeringBehavior(ship);
        ship.add(steering);
        engine.addEntity(ship);
        return ship;
    }

}
