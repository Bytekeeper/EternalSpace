package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.component.Movement;
import org.bk.component.Steering;
import org.bk.component.Transform;

import static org.bk.component.Mapper.MOVEMENT;
import static org.bk.component.Mapper.STEERING;
import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 16.10.2016.
 */
public class SteeringSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();

    public SteeringSystem(int priority) {
        super(Family.all(Movement.class, Steering.class, Transform.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Steering steering = STEERING.get(entity);
        Movement movement = MOVEMENT.get(entity);
        Transform transform = TRANSFORM.get(entity);

        tv.set(Vector2.X).rotateRad(transform.orientRad).scl(MathUtils.clamp(steering.thrust, -1, 1) * movement.linearThrust);
        movement.linearAccel.add(tv);
        movement.angularAccel += MathUtils.clamp(steering.turn, -1 , 1) * movement.angularThrust;

        steering.thrust = 0;
        steering.turn = 0;
    }
}
