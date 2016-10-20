package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.component.*;

import static org.bk.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class SteeringSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();

    public SteeringSystem(int priority) {
        super(Family.all(Movement.class, Steering.class, Transform.class, Physics.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Steering steering = STEERING.get(entity);
        Movement movement = MOVEMENT.get(entity);
        Transform transform = TRANSFORM.get(entity);

        tv.set(Vector2.X).rotateRad(transform.orientRad).scl(MathUtils.clamp(steering.thrust, -1, 1) * movement.linearThrust);
        movement.linearAccel.add(tv);
        movement.angularAccel += MathUtils.clamp(steering.turn, -1, 1) * movement.angularThrust;

        if (steering.mode == Steering.SteeringMode.LANDING && movement.velocity.len2() < 100) {
            assert !LANDING.has(entity);
            Touching touching = TOUCHING.get(entity);
            if (touching != null) {
                for (Entity e : touching.touchList) {
                    if (PLANET.has(e)) {
                        Landing landing = getEngine().createComponent(Landing.class);
                        landing.landingDirection = Landing.LandingDirection.LANDING;
                        landing.duration = landing.timeRemaining = 2;
                        landing.target = e;
                        entity.add(landing);
                    }
                }
            }
        }
        if (steering.mode == Steering.SteeringMode.JUMPING && movement.velocity.len2() < 20 &&
                Math.abs(transform.orientRad - getEngine().getSystem(SystemPopulateSystem.class).orientationToward(steering.jumpTo)) < 0.01) {
            Jumping jumping = getEngine().createComponent(Jumping.class);
            jumping.sourceOrTargetSystem = steering.jumpTo;
            jumping.timeRemaining = Jumping.JUMP_DURATION / 2;
            jumping.direction = Jumping.JumpDirection.DEPART;
            jumping.referencePoint.set(transform.location);
            entity.add(jumping);
        }
        Mounts mounts = MOUNTS.get(entity);
        if (mounts != null) {
            for (Mounts.Weapon weapon : mounts.weapons) {
                weapon.firing = steering.primaryFire;
            }
        }
        steering.thrust = 0;
        steering.turn = 0;
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
