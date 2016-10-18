package org.bk.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.component.Mapper;
import org.bk.component.Steering;

import static org.bk.component.Mapper.STEERING;

/**
 * Created by dante on 18.10.2016.
 */
public class PatrolTask extends LeafTask<Entity> {
    @Override
    public Status execute() {
        Steering steering = STEERING.get(getObject());
        if (!(steering.behavior instanceof Wander)) {
            Wander<Vector2> wander = new Wander<Vector2>(steering.steerable);
            wander.setWanderOffset(150).
                    setWanderRadius(60).
                    setWanderRate(MathUtils.PI / 2);
            steering.behavior = wander;
        }
        SteeringUtil.applySteering(steering.behavior, steering.steerable, STEERING.get(getObject()));
        return Status.RUNNING;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}