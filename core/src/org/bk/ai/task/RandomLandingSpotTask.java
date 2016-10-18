package org.bk.ai.task;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.ai.SteeringUtil;
import org.bk.component.Mapper;
import org.bk.component.Planet;
import org.bk.component.Steering;
import org.bk.component.Transform;

import static org.bk.component.Mapper.PLANET;
import static org.bk.component.Mapper.TRANSFORM;

/**
 * Created by dante on 18.10.2016.
 */
public class RandomLandingSpotTask extends LeafTask<Entity> {
    private final Engine engine;

    public RandomLandingSpotTask(Engine engine) {
        this.engine = engine;
    }

    @Override
    public Status execute() {
        Steering steering = Mapper.STEERING.get(getObject());
        if (steering.targetEntity == null || !PLANET.has(steering.targetEntity)) {
            Entity entity = engine.getEntitiesFor(Family.all(Planet.class, Transform.class).get()).random();
            steering.targetEntity = entity;
            steering.targetLocation = SteeringUtil.toLocation(TRANSFORM.get(entity).location);
        }
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}