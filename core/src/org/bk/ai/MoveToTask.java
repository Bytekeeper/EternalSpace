package org.bk.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import org.bk.component.AIControlled;
import org.bk.component.Steering;

import static org.bk.component.Mapper.AI_CONTROLLED;
import static org.bk.component.Mapper.STEERING;

/**
 * Created by dante on 18.10.2016.
 */
public class MoveToTask extends LeafTask<Entity> {
    @Override
    public Status execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
