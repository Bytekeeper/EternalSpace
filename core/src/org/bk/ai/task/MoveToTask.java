package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;

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
