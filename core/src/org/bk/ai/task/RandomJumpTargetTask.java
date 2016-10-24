package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.Game;
import org.bk.data.SolarSystem;

import static org.bk.data.component.Mapper.STEERING;

/**
 * Created by dante on 24.10.2016.
 */
public class RandomJumpTargetTask extends LeafTask<Entity> {
    private final Game game;

    public RandomJumpTargetTask(Game game) {
        this.game = game;
    }

    @Override
    public Status execute() {
        SolarSystem thorin = game.gameData.getSystem("Thorin");
        STEERING.get(getObject()).jumpTo = thorin;
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
