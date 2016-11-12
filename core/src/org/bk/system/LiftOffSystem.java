package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.Game;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.state.Landing;
import org.bk.data.component.state.LiftingOff;
import org.bk.data.component.state.ManualControl;

import static org.bk.data.component.Mapper.LIFTING_OFF;
import static org.bk.data.component.Mapper.MANUAL_CONTROL;

/**
 * Created by dante on 10.11.2016.
 */
public class LiftOffSystem extends IteratingSystem {
    private final Game game;

    public LiftOffSystem(Game game, int priority) {
        super(Family.all(LiftingOff.class).get(), priority);
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(Physics.class);
        entity.remove(Steering.class);

        LiftingOff liftingOff = LIFTING_OFF.get(entity);
        liftingOff.timeRemaining = Math.max(0, liftingOff.timeRemaining - deltaTime);
        if (liftingOff.timeRemaining <= 0) {
            entity.add(getEngine().createComponent(Physics.class));
            entity.add(getEngine().createComponent(Steering.class));
            game.control.setTo(entity, MANUAL_CONTROL, ManualControl.class);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
