package org.bk.system.state;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.AIControlled;
import org.bk.data.component.Steering;
import org.bk.data.component.ManualControl;
import org.bk.data.component.state.States;

import static org.bk.data.component.Mapper.MANUAL_CONTROL;
import static org.bk.data.component.Mapper.STEERING;

/**
 * Created by dante on 10.11.2016.
 */
public class ManualControlSystem extends IteratingSystem {
    public ManualControlSystem() {
        super(Family.all(ManualControl.class, Steering.class).exclude(AIControlled.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (States.UNABORTABLE_ACTIONS.matches(entity)) {
            return;
        }
        ManualControl manualControl = MANUAL_CONTROL.get(entity);
        Steering steering = STEERING.get(entity);
        steering.turn = manualControl.turn;
        steering.thrust = manualControl.thrust;

        if (manualControl.turn != 0 || manualControl.thrust != 0) {
            States.abortActions(entity);
        }
    }
}
