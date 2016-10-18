package org.bk.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.component.Landing;
import org.bk.component.Steering;
import org.bk.component.Transform;

import static org.bk.component.Mapper.LANDING;
import static org.bk.component.Mapper.STEERING;
import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 18.10.2016.
 */
public class LandingTask extends LeafTask<Entity> {
    @Override
    public Status execute() {
        Transform transform = TRANSFORM.get(getObject());
        Steering steering = STEERING.get(getObject());
        if (steering.targetEntity == null || transform.location.dst2(steering.targetLocation.getPosition()) > 30) {
            return Status.FAILED;
        }
        Landing landing = LANDING.get(getObject());
        if (landing == null) {
            steering.land = true;
            return Status.RUNNING;
        }
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
