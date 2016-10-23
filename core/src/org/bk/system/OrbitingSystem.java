package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.component.Movement;
import org.bk.component.Orbiting;
import org.bk.component.Transform;
import org.omg.CORBA.ORB;

import static org.bk.component.Mapper.MOVEMENT;
import static org.bk.component.Mapper.ORBITING;
import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 23.10.2016.
 */
public class OrbitingSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();

    public OrbitingSystem(int priority) {
        super(Family.all(Transform.class, Movement.class, Orbiting.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = TRANSFORM.get(entity);
        Orbiting orbiting = ORBITING.get(entity);
        Vector2 orbitAround = TRANSFORM.get(orbiting.entity).location;
        tv.set(transform.location).sub(orbitAround);
        if (tv.len2() < 1) {
            tv.setToRandomDirection();
        }
        float rotation = orbiting.speed / MathUtils.PI2 / orbiting.distance * deltaTime;
        tv.rotateRad(rotation).setLength(orbiting.distance);
        transform.location.set(orbitAround).add(tv);
    }
}
