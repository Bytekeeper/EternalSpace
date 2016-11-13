package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import org.bk.Assets;
import org.bk.data.component.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class AISystem extends IteratingSystem {

    public AISystem() {
        super(Family.all(Steering.class, Transform.class, AIControlled.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Not ready yet?
        AIControlled aiControlled = AI_CONTROLLED.get(entity);
        if (aiControlled.behaviorTree == null) {
            Gdx.app.debug(AISystem.class.getSimpleName(), "AI entity without behavior!");
            return;
        }
        if (STEERING.has(entity) && STEERING.get(entity).steerable == null) {
            return;
        }
        WEAPON_CONTROL.get(entity).primaryFire = false;
        aiControlled.behaviorTree.step();
    }
}
