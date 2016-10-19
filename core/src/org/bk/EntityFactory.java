package org.bk;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import org.bk.ai.SteeringUtil;
import org.bk.component.*;

import java.util.HashMap;
import java.util.Map;

import static org.bk.component.Mapper.MOVEMENT;
import static org.bk.component.Mapper.PHYSICS;
import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 19.10.2016.
 */
public class EntityFactory {
    private final PooledEngine engine;
    private Map<EntityDefinitionKey, EntityPopulator> populators = new HashMap<EntityDefinitionKey, EntityPopulator>();
    private Map<String, EntityDefinitionKey> usedKeys = new HashMap<String, EntityDefinitionKey>();

    public EntityFactory(PooledEngine engine) {
        this.engine = engine;
        registerShip();
        registerProjectile();
        registerAsteroid();
        registerPlanet();
    }

    public EntityDefinitionKey key(String name) {
        EntityDefinitionKey entityDefinitionKey = usedKeys.get(name);
        if (entityDefinitionKey == null) {
            entityDefinitionKey = new EntityDefinitionKey(name);
            usedKeys.put(name, entityDefinitionKey);
        }
        return entityDefinitionKey;
    }

    private void register(String name, EntityPopulator populator) {
        populators.put(key(name), populator);
    }

    private void registerPlanet() {
        register("planet", new EntityPopulator() {
            @Override
            public void populate(Entity e) {
                Body body = engine.createComponent(Body.class).reset();
                body.dimension.set(190, 190);
                Transform transform = engine.createComponent(Transform.class).reset();
                transform.location.set(100, 50);

                e.add(body);
                e.add(engine.createComponent(Planet.class).reset());
                e.add(transform);
                e.add(engine.createComponent(Physics.class));
            }
        });
    }

    private void registerAsteroid() {
        populators.put(key("asteroid"), new EntityPopulator() {
            @Override
            public void populate(Entity e) {
                Movement movement = engine.createComponent(Movement.class).reset();
                movement.maxVelocity = 500;
                Body body = engine.createComponent(Body.class);
                body.dimension.set(45, 45);

                e.add(engine.createComponent(Asteroid.class).reset());
                e.add(engine.createComponent(Transform.class).reset());
                e.add(movement);
                e.add(body);
                e.add(engine.createComponent(Physics.class));
            }
        });
    }

    private void registerProjectile() {
        populators.put(key("projectile"), new EntityPopulator() {
            @Override
            public void populate(Entity e) {
                Body body = engine.createComponent(Body.class).reset();
                body.dimension.set(8, 50);
                LifeTime lifeTime = engine.createComponent(LifeTime.class);
                lifeTime.remaining = 2;
                Projectile projectile = engine.createComponent(Projectile.class).reset();
                projectile.initialSpeed = 1000;
                projectile.yield = 10;


                e.add(engine.createComponent(Transform.class).reset());
                e.add(engine.createComponent(Movement.class).reset());
                e.add(projectile);
                e.add(body);
                e.add(engine.createComponent(Physics.class));
                e.add(lifeTime);
            }
        });
    }

    private void registerShip() {
        populators.put(key("ship"), new EntityPopulator() {
            @Override
            public void populate(Entity e) {
                Body body = engine.createComponent(Body.class).reset();
                body.dimension.set(35, 40);
                Transform transform = engine.createComponent(Transform.class).reset();
                transform.location.set(400, 300);
                transform.orientRad = 0;
                Movement movement = engine.createComponent(Movement.class).reset();
                movement.maxVelocity = 500;
                movement.angularThrust = 3000;
                movement.linearThrust = 20000;
                Health health = engine.createComponent(Health.class).reset();
                health.hull = 100;
                health.shields = 100;
                Mounts mounts = new Mounts();
                Mounts.Weapon weapon = new Mounts.Weapon();
                weapon.cooldownPerShot = 0.1f;
                weapon.projectileDefinition = key("projectile");
                weapon.offset.set(20, -5);
                mounts.weapons.add(weapon);
                weapon = new Mounts.Weapon();
                weapon.cooldownPerShot = 0.1f;
                weapon.projectileDefinition = key("projectile");
                weapon.offset.set(20, 5);
                mounts.weapons.add(weapon);
                mounts.thrusters.add(new Vector2(-20, 0));
                Physics physics = engine.createComponent(Physics.class);
                Steering steering = engine.createComponent(Steering.class);

                e.add(body);
                e.add(physics);
                e.add(transform);
                e.add(new Ship());
                e.add(movement);
                e.add(health);
                e.add(mounts);
                e.add(steering);
            }
        });
    }

    public Entity create(EntityDefinitionKey entityDefinitionKey) {
        Entity entity = engine.createEntity();
        EntityPopulator entityPopulator = populators.get(entityDefinitionKey);
        if (entityPopulator == null) {
            throw new IllegalStateException("No entity named '" + entityDefinitionKey.entityDefinitionName + "' is defined.");
        }
        entityPopulator.populate(entity);
        return entity;
    }

    private interface EntityPopulator {
        void populate(Entity e);
    }

    public static class EntityDefinitionKey {
        public final String entityDefinitionName;

        protected EntityDefinitionKey(String entityDefinitionName) {
            this.entityDefinitionName = entityDefinitionName;
        }
    }
}
