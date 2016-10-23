package org.bk.ai.task;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.component.Mapper;
import org.bk.component.Celestial;
import org.bk.component.Steering;
import org.bk.component.Transform;

import static org.bk.component.Mapper.CELESTIAL;

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
        if (steering.modeTargetEntity == null || !CELESTIAL.has(steering.modeTargetEntity)) {
            Entity entity = engine.getEntitiesFor(Family.all(Celestial.class, Transform.class).get()).random();
            if (entity == null) {
                return Status.FAILED;
            }
            steering.modeTargetEntity = entity;
            steering.mode = Steering.SteeringMode.LANDING;
        }
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
