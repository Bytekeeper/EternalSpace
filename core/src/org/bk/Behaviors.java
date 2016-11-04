package org.bk;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.DynamicGuardSelector;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import org.bk.ai.task.*;
import org.bk.data.Faction;
import org.bk.data.component.*;
import org.bk.data.component.Character;

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
        DynamicGuardSelector<Entity> root = new DynamicGuardSelector<Entity>();
        Sequence<Entity> patrolThenLand = new Sequence<Entity>();
        Parallel<Entity> patrolTree = new Parallel<Entity>(Parallel.Policy.Selector);
        patrolTree.addChild(new Wait<Entity>(10));
        patrolTree.addChild(new PatrolTask());
        patrolThenLand.addChild(patrolTree);
        patrolThenLand.addChild(new RandomLandingSpotTask(engine));
        Sequence<Entity> land = new Sequence<Entity>();
        land.addChild(new LandingTask());
        patrolThenLand.addChild(land);
        ImmutableArray<Entity> potentialEnemies = engine.getEntitiesFor(Family.all(Character.class, Transform.class, Health.class, Steering.class).get());
        Task<Entity> fight = new AttackTask(potentialEnemies);
        fight.setGuard(new EnemyNearby(potentialEnemies));
        root.addChild(fight);
        root.addChild(patrolThenLand);
        tree.addChild(root);
        tree.setObject(owner);
        return tree;
    }

    public BehaviorTree<Entity> jump(Entity entity, Game game) {
        BehaviorTree<Entity> tree = new BehaviorTree<Entity>();
        Sequence<Entity> root = new Sequence<Entity>();
        root.addChild(new RandomJumpTargetTask(game));
        root.addChild(new JumpTask());

        tree.setObject(entity);
        tree.addChild(root);
        return tree;
    }
}
