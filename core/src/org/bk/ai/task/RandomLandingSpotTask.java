package org.bk.ai.task;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.Game;
import org.bk.data.component.*;
import org.bk.data.component.state.Land;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 18.10.2016.
 */
public class RandomLandingSpotTask extends LeafTask<Entity> {
    private final Game game;
    private final Engine engine;

    public RandomLandingSpotTask(Game game, Engine engine) {
        this.game = game;
        this.engine = engine;
    }

    @Override
    public Status execute() {
        if (LAND.has(getObject()) || LANDING.has(getObject()) || LANDED.has(getObject())) {
            return Status.SUCCEEDED;
        }
        Entity entity = engine.getEntitiesFor(Family.all(LandingPlace.class, Transform.class).get()).random();
        if (entity == null) {
            return Status.FAILED;
        }
        Land land = game.engine.createComponent(Land.class);
        land.on = entity;
        getObject().add(land);
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
