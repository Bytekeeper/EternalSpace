package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 16.10.2016.
 */
public class AIControlled implements Component, Pool.Poolable {
    public transient BehaviorTree<Entity> behaviorTree;
    public Entity enemy;

    @Override
    public void reset() {
        behaviorTree = null;
        enemy = null;
    }
}
