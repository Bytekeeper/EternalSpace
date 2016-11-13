package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.data.component.Persistence;
import org.bk.data.component.state.JumpedOut;
import org.bk.data.component.state.Landed;

import static org.bk.data.component.Mapper.PERSISTENCE;

/**
 * Created by dante on 10.11.2016.
 */
public class PersistenceSystem extends IteratingSystem {
    public PersistenceSystem() {
        super(Family.one(JumpedOut.class, Landed.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Persistence persistence = PERSISTENCE.get(entity);
        if (persistence == null || persistence.temporary) {
            getEngine().removeEntity(entity);
        }
    }
}
