package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import org.bk.Game;
import org.bk.data.component.Persistence;
import org.bk.data.component.Transform;

import static org.bk.data.component.Mapper.PERSISTENCE;

/**
 * Created by dante on 20.10.2016.
 */
public class SystemPopulateSystem extends EntitySystem {
    private Game game;
    private ImmutableArray<Entity> allTransformEntities;
    public final Signal<String> systemChanged = new Signal<String>();
    private boolean dispatchOnNextUpdate;

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
        if (dispatchOnNextUpdate) {
            systemChanged.dispatch(game.currentSystem.name);
            dispatchOnNextUpdate = false;
        }
        Persistence playerPersistence = PERSISTENCE.get(game.playerEntity);
        if (game.currentSystem == null || playerPersistence.system != game.currentSystem) {
            game.currentSystem = playerPersistence.system;
            Gdx.app.debug(SystemPopulateSystem.class.getSimpleName(), "Switching world to system " + game.currentSystem);
            removeAllEntitiesNotInSystem();
            game.populateCurrentSystem();
            dispatchOnNextUpdate = true;
        }
    }

    private void removeAllEntitiesNotInSystem() {
        for (Entity entity: allTransformEntities) {
            Persistence persistence = PERSISTENCE.get(entity);
            if (persistence != null && persistence.system == game.currentSystem) {
                continue;
            }
            if (persistence == null || persistence.temporary) {
                getEngine().removeEntity(entity);
            }
        }
    }
}
