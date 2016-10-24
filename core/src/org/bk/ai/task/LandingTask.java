package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.data.component.Steering;

import static org.bk.data.component.Mapper.STEERING;

/**
 * Created by dante on 18.10.2016.
 */
public class LandingTask extends LeafTask<Entity> {
    @Override
    public Status execute() {
        Steering steering = STEERING.get(getObject());
        if (steering.modeTargetEntity == null) {
            return Status.FAILED;
        }
        steering.mode = Steering.SteeringMode.LANDING;
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
