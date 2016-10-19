package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 16.10.2016.
 */
public class AIControlled implements Component, Pool.Poolable {
    public BehaviorTree<Entity> behaviorTree;

    @Override
    public void reset() {
        behaviorTree = null;
    }
}
