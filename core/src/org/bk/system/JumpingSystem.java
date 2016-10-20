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
 * Created by dante on 20.10.2016.
 */
public class JumpingSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();

    public JumpingSystem(int priority) {
        super(Family.all(Jumping.class, Transform.class, Persistence.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Jumping jumping = JUMPING.get(entity);
        if (STEERING.has(entity)) {
            jumping.hasSteering = true;
            entity.remove(Steering.class);
        }
        if (PHYSICS.has(entity)) {
            jumping.hasPhysics = true;
            entity.remove(Physics.class);
        }
        Transform transform = TRANSFORM.get(entity);
        if (jumping.direction == Jumping.JumpDirection.DEPART) {
            float targetOrientation = getEngine().getSystem(SystemPopulateSystem.class).orientationToward(jumping.sourceOrTargetSystem);
            transform.orientRad = targetOrientation;
            tv.set(Vector2.X).setAngleRad(targetOrientation);
            float timePassed = Jumping.JUMP_DURATION / 2 - jumping.timeRemaining;
            float dst = timePassed * timePassed * timePassed * 400;
            transform.location.set(tv).scl(dst).add(jumping.referencePoint);
        } else {
            float targetOrientation = getEngine().getSystem(SystemPopulateSystem.class).orientationFrom(jumping.sourceOrTargetSystem);
            transform.orientRad = targetOrientation;
            tv.set(Vector2.X).setAngleRad(targetOrientation);
            float dst = jumping.timeRemaining * jumping.timeRemaining * jumping.timeRemaining * 400;
            transform.location.set(tv).scl(-dst).add(jumping.referencePoint);
        }
        jumping.timeRemaining -= deltaTime;
        if (jumping.timeRemaining < 0) {
            if (jumping.direction == Jumping.JumpDirection.DEPART) {
                jumping.direction = Jumping.JumpDirection.ARRIVE;
                Persistence persistence = PERSISTENCE.get(entity);
                SystemPopulateSystem.SystemKey comingFrom = persistence.system;
                persistence.system = jumping.sourceOrTargetSystem;
                jumping.sourceOrTargetSystem = comingFrom;
                jumping.timeRemaining = Jumping.JUMP_DURATION / 2;
                jumping.referencePoint.setToRandomDirection().scl(MathUtils.random(0, 800));
            } else {
                if (jumping.hasPhysics) {
                    entity.add(getEngine().createComponent(Physics.class));
                }
                if (jumping.hasSteering) {
                    entity.add(getEngine().createComponent(Steering.class));
                }
                entity.remove(Jumping.class);
            }
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
