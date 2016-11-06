package org.bk.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.data.component.AIControlled;
import org.bk.data.component.Ship;

import static org.bk.data.component.Mapper.AI_CONTROLLED;
import static org.bk.data.component.Mapper.STEERING;

/**
 * Created by dante on 06.11.2016.
 */
public class SelectionSystem extends EntitySystem {
    private final Vector2 tv = new Vector2();
    private final Game game;

    public SelectionSystem(final Game game, int priority) {
        super(priority);
        this.game = game;
        game.inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                tv.set(screenX,  screenY);
                game.viewport.unproject(tv);
                tv.scl(Box2DPhysicsSystem.W2B);
                Entity picked = getEngine().getSystem(Box2DPhysicsSystem.class).pick(tv);
                if (picked != null && STEERING.has(game.playerEntity)) {
                    game.player.selectedEntity = picked;
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void addedToEngine(Engine engine) {
        final ImmutableArray<Entity> aiNPCs = engine.getEntitiesFor(Family.all(AIControlled.class).get());
        engine.addEntityListener(Family.all(Ship.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
            }

            @Override
            public void entityRemoved(Entity entity) {
                if (entity == game.player.selectedEntity) {
                    game.player.selectedEntity = null;
                }
                for (Entity e: aiNPCs) {
                    AIControlled aiControlled = AI_CONTROLLED.get(e);
                    if (aiControlled.enemy == entity) {
                        aiControlled.enemy = null;
                    }
                }
            }
        });
    }
}
