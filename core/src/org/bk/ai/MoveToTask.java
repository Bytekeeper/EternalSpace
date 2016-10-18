package org.bk.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import org.bk.component.Steering;

import static org.bk.component.Mapper.STEERING;

/**
 * Created by dante on 18.10.2016.
 */
public class MoveToTask extends LeafTask<Entity> {
    @Override
    public Status execute() {
        Steering steering = STEERING.get(getObject());
        if (steering.targetLocation == null) {
            return Status.FAILED;
        }
        if (!(steering.behavior instanceof Arrive)) {
            steering.behavior = new Arrive<Vector2>(steering.steerable, steering.targetLocation);
        }
        SteeringUtil.applySteering(steering.behavior, steering.steerable, STEERING.get(getObject()));
        return steering.steerable.getPosition().dst(steering.targetLocation.getPosition()) > 10 ?
                Status.RUNNING : Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
