package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import org.bk.Util;
import org.bk.ai.Attack;
import org.bk.ai.Pursue;
import org.bk.ai.SteeringUtil;
import org.bk.data.Faction;
import org.bk.data.component.AIControlled;
import org.bk.data.component.Character;
import org.bk.data.component.Steering;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 04.11.2016.
 */
public class AttackTask extends LeafTask<Entity> {
    private final ImmutableArray<Entity> entitiesToObserve;
    private final Vector2 tv = new Vector2();
    public SteeringBehavior<Vector2> steeringBehavior;

    public AttackTask(ImmutableArray<Entity> entitiesToObserve) {
        this.entitiesToObserve = entitiesToObserve;
    }

    @Override
    public Status execute() {
        Steering steering = STEERING.get(getObject());
        AIControlled aiControlled = AI_CONTROLLED.get(getObject());
        if (steeringBehavior == null || aiControlled.enemy == null) {
            Faction ownerFaction = CHARACTER.get(getObject()).faction;
            for (Entity e: entitiesToObserve) {
                Character otherCharacter = CHARACTER.get(e);
                if (ownerFaction.isEnemy(otherCharacter.faction) && STEERING.has(e) && STEERING.get(e).steerable != null) {
                    aiControlled.enemy = e;
                    break;
                }
            }
            if (aiControlled.enemy == null) {
                return Status.SUCCEEDED;
            }
//            steeringBehavior = new Attack(steering.steerable, STEERING.get(aiControlled.enemy).steerable);
//            steeringBehavior.setMaxMatchDistance(200);
            Pursue pursue = new Pursue(steering.steerable, STEERING.get(aiControlled.enemy).steerable);
            pursue.setMaxPredictionTime(3);
            steeringBehavior = pursue;
        }
        SteeringUtil.applySteering(steeringBehavior, steering.steerable, steering);
        float targetAngle = tv.set(TRANSFORM.get(aiControlled.enemy).location).sub(TRANSFORM.get(getObject()).location).angleRad();
        if (Math.abs(Util.deltaAngle(targetAngle, TRANSFORM.get(getObject()).orientRad)) < 0.2f &&
                tv.len() < 1000) {
            STEERING.get(getObject()).primaryFire = true;
        }
        return Status.RUNNING;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
