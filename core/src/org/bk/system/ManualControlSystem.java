package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.AIControlled;
import org.bk.data.component.Steering;
import org.bk.data.component.state.ManualControl;

import static org.bk.data.component.Mapper.MANUAL_CONTROL;
import static org.bk.data.component.Mapper.STEERING;

/**
 * Created by dante on 10.11.2016.
 */
public class ManualControlSystem extends IteratingSystem {
    public ManualControlSystem(int priority) {
        super(Family.all(ManualControl.class, Steering.class).exclude(AIControlled.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ManualControl manualControl = MANUAL_CONTROL.get(entity);
        Steering steering = STEERING.get(entity);
        steering.turn = manualControl.turn;
        steering.thrust = manualControl.thrust;
    }
}
