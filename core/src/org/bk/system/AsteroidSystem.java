package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import org.bk.Game;
import org.bk.component.Asteroid;
import org.bk.component.Movement;
import org.bk.component.Transform;

import static org.bk.component.Mapper.MOVEMENT;
import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 19.10.2016.
 */
public class AsteroidSystem extends IteratingSystem {
    private static final float MAX_ASTEROID_DISTANCE = 2000;
    private static final float MAX_ASTEROID_DISTANCE2 = MAX_ASTEROID_DISTANCE * MAX_ASTEROID_DISTANCE;
    private static final float MIN_ASTEROID_DISTANCE = 1000;
    private static final float MIN_ASTEROID_SPEED = 50;
    private static final float MAX_ASTEROID_SPEED = 300;
    private final Game game;
    private boolean refresh = true;

    public AsteroidSystem(Game game, int priority) {
        super(Family.all(Asteroid.class, Transform.class).get(), priority);
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        Vector3 cameraPosition = game.viewport.getCamera().position;
        int toSpawn = 20 - getEntities().size();
        while (toSpawn-- > 0) {
            spawnAsteroid(cameraPosition, refresh ? 0 : MIN_ASTEROID_DISTANCE, MAX_ASTEROID_DISTANCE);
        }
        super.update(deltaTime);
        refresh = false;
    }

    private void spawnAsteroid(Vector3 cameraPosition, float minAsteroidDistance, float maxAsteroidDistance) {
        Entity asteroid = game.spawn("asteroid");
        Movement movement = MOVEMENT.get(asteroid);
        movement.velocity.setToRandomDirection().scl(MathUtils.random(MIN_ASTEROID_SPEED, MAX_ASTEROID_SPEED));
        movement.angularVelocity = MathUtils.random(-MathUtils.PI2, MathUtils.PI2);
        TRANSFORM.get(asteroid).location.setToRandomDirection().scl(MathUtils.random(minAsteroidDistance, maxAsteroidDistance)).
                add(cameraPosition.x, cameraPosition.y);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = TRANSFORM.get(entity);
        Vector3 cameraPosition = game.viewport.getCamera().position;
        if (transform.location.dst2(cameraPosition.x, cameraPosition.y) > MAX_ASTEROID_DISTANCE2) {
            getEngine().removeEntity(entity);
        }
    }

    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
