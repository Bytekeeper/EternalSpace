package org.bk.system.state;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.state.Idle;
import org.bk.data.component.state.JumpingIn;
import org.bk.data.component.state.LiftingOff;
import org.bk.data.component.state.Start;
import org.bk.fsm.TransitionListener;

/**
 * Created by dante on 12.11.2016.
 */
public class IdleSystem extends IteratingSystem {
    public IdleSystem(int priority) {
        super(Family.all(Start.class).get(), priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(Family.all(Idle.class).get(), new TransitionListener(Idle.class, LiftingOff.class, JumpingIn.class, Start.class));
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.add(getEngine().createComponent(Idle.class));
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
