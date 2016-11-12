package org.bk.system.state;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.AIControlled;
import org.bk.data.component.Steering;
import org.bk.data.component.state.Idle;
import org.bk.data.component.state.Jump;
import org.bk.data.component.state.Land;
import org.bk.data.component.state.ManualControl;
import org.bk.fsm.TransitionListener;

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
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new TransitionListener(ManualControl.class, Jump.class, Land.class, Idle.class));
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ManualControl manualControl = MANUAL_CONTROL.get(entity);
        Steering steering = STEERING.get(entity);
        steering.turn = manualControl.turn;
        steering.thrust = manualControl.thrust;
    }
}
