package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import org.bk.Game;
import org.bk.component.Persistence;
import org.bk.component.Transform;

import static org.bk.component.Mapper.PERSISTENCE;

/**
 * Created by dante on 20.10.2016.
 */
public class SystemPopulateSystem extends EntitySystem {
    private Game game;
    private SystemKey currentSystem;
    private ImmutableArray<Entity> allTransformEntities;
    public final Signal<SystemKey> systemChanged = new Signal<SystemKey>();

    public SystemPopulateSystem(Game game, int priority) {
        super(priority);
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        allTransformEntities = engine.getEntitiesFor(Family.all(Transform.class).get());
    }

    @Override
    public void update(float deltaTime) {
        Persistence playerPersistence = PERSISTENCE.get(game.player);
        if (playerPersistence.system != currentSystem) {
            currentSystem = playerPersistence.system;
            Gdx.app.debug(SystemPopulateSystem.class.getSimpleName(), "Switching world to system " + currentSystem.name);
            removeAllEntitiesNotInSystem();
            game.addEntitiesOf(currentSystem);
            systemChanged.dispatch(currentSystem);
        }
    }

    private void removeAllEntitiesNotInSystem() {
        for (Entity entity: allTransformEntities) {
            Persistence persistence = PERSISTENCE.get(entity);
            if (persistence != null && persistence.system == currentSystem) {
                continue;
            }
            if (persistence == null || persistence.temporary) {
                getEngine().removeEntity(entity);
            }
        }
    }

    public SystemKey key(String name) {
        return new SystemKey(name);
    }

    public float orientationToward(SystemKey target) {
        return MathUtils.PI / 3;
    }

    public float orientationFrom(SystemKey sourceOrTargetSystem) {
        return (orientationToward(sourceOrTargetSystem) + MathUtils.PI) % MathUtils.PI2;
    }

    public static class SystemKey {
        public final String name;

        SystemKey(String name) {
            this.name = name;
        }
    }
}
