package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.bk.Game;
import org.bk.data.SolarSystem;
import org.bk.data.component.state.Jump;

import static org.bk.data.component.Mapper.*;

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
        if (JUMP.has(getObject()) || JUMPING_OUT.has(getObject()) || JUMPING_IN.has(getObject())) {
            return Status.SUCCEEDED;
        }
        SolarSystem solarSystem = game.gameData.getSystem().random();
        Jump jump = game.engine.createComponent(Jump.class);
        jump.target = solarSystem;
        getObject().add(jump);
        return Status.SUCCEEDED;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
