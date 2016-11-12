package org.bk.ai.task;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.steer.behaviors.Wander;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.ai.SteeringUtil;
import org.bk.data.component.Steering;
import org.bk.data.component.state.ManualControl;

import static org.bk.data.component.Mapper.MANUAL_CONTROL;
import static org.bk.data.component.Mapper.STEERING;

/**
 * Created by dante on 18.10.2016.
 */
public class PatrolTask extends LeafTask<Entity> {
    private Wander wander;
    private final Game game;

    public PatrolTask(Game game) {
        this.game = game;
    }

    @Override
    public Status execute() {
        Steering steering = STEERING.get(getObject());
        if (wander == null) {
            wander = new Wander<Vector2>(steering.steerable);
            wander.setWanderOffset(150).
                    setWanderRadius(80).
                    setWanderRate(MathUtils.PI / 8);
        }
        game.control.setTo(getObject(), MANUAL_CONTROL, ManualControl.class);
        SteeringUtil.applySteering(wander, steering.steerable, STEERING.get(getObject()));
        return Status.RUNNING;
    }

    @Override
    protected Task<Entity> copyTo(Task<Entity> task) {
        return null;
    }
}
