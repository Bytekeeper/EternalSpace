package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.component.Steering;

import static org.bk.component.Mapper.STEERING;

/**
 * Created by dante on 21.10.2016.
 */
public class JumpTask extends LeafTask<Entity> {
    @Override
    public Status execute() {
        Steering steering = STEERING.get(getObject());
        if (steering == null) {
            return Status.SUCCEEDED;
        }
        steering.mode = Steering.SteeringMode.JUMPING;
        return Status.RUNNING;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
