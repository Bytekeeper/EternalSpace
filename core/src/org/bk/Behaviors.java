package org.bk;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import org.bk.ai.LandingTask;
import org.bk.ai.PatrolTask;
import org.bk.ai.task.JumpTask;
import org.bk.ai.task.RandomLandingSpotTask;

/**
 * Created by dante on 18.10.2016.
 */
public class Behaviors {

    public BehaviorTree<Entity> land(Entity owner, Engine engine) {
        BehaviorTree<Entity> tree = new BehaviorTree<Entity>();
        Sequence<Entity> sequence = new Sequence<Entity>();
        sequence.addChild(new RandomLandingSpotTask(engine));
        sequence.addChild(new LandingTask());
        tree.addChild(sequence);
        tree.setObject(owner);
        return tree;
    }

    public BehaviorTree<Entity> patrol(Entity owner, Engine engine) {
        BehaviorTree<Entity> tree = new BehaviorTree<Entity>();
        Sequence<Entity> root = new Sequence<Entity>();
        Parallel<Entity> patrolTree = new Parallel<Entity>(Parallel.Policy.Selector);
        patrolTree.addChild(new Wait<Entity>(10));
        patrolTree.addChild(new PatrolTask());
        root.addChild(patrolTree);
        root.addChild(new RandomLandingSpotTask(engine));
        Sequence<Entity> land = new Sequence<Entity>();
        land.addChild(new LandingTask());
        root.addChild(land);
        tree.addChild(root);
        tree.setObject(owner);
        return tree;
    }

    public BehaviorTree<Entity> jump(Entity entity) {
        BehaviorTree<Entity> tree = new BehaviorTree<Entity>();
        tree.addChild(new JumpTask());
        tree.setObject(entity);
        return tree;
    }
}
