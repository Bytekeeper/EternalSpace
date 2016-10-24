package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.data.component.Movement;
import org.bk.data.component.Orbiting;
import org.bk.data.component.Transform;

import static org.bk.data.component.Mapper.ORBITING;
import static org.bk.data.component.Mapper.TRANSFORM;

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
        if (orbiting.tidalLock) {
            transform.orientRad = tv.angleRad() + orbiting.tidalOrientation;
        }
        transform.location.set(orbitAround).add(tv);
    }
}
