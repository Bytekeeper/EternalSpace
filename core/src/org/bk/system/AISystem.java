package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.bk.Assets;
import org.bk.Game;
import org.bk.data.component.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class AISystem extends IteratingSystem {
    private final static boolean DEBUG = false;
    private final SpriteBatch uiBatch;
    private final Assets assets;
    private int line;

    public AISystem(Game game, int priority) {
        super(Family.all(Steering.class, Transform.class, AIControlled.class).get(), priority);
        uiBatch = game.uiBatch;
        assets = game.assets;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    public void update(float deltaTime) {
        line = 0;
        if (DEBUG) {
            uiBatch.begin();
        }
        super.update(deltaTime);
        if (DEBUG) {
            uiBatch.end();
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Not ready yet?
        Steering steering = STEERING.get(entity);
        Physics physics = PHYSICS.get(entity);
        if (steering == null || steering.steerable == null ||
                physics == null || physics.physicsBody == null) {
            return;
        }
        AIControlled aiControlled = AI_CONTROLLED.get(entity);
        if (aiControlled.enemy != null) {
            if (!TRANSFORM.has(aiControlled.enemy) || !STEERING.has(aiControlled.enemy)) {
                aiControlled.enemy = null;
            }
        }
        if (aiControlled.behaviorTree == null) {
            Gdx.app.debug(AISystem.class.getSimpleName(), "AI entity without behavior!");
            return;
        }
        aiControlled.behaviorTree.step();
        if (DEBUG) {
            Transform transform = TRANSFORM.get(entity);
            assets.debugFont.draw(uiBatch, String.format("loc = (%1.1f, %1.1f) turn = %1.1f thrust = %1.1f",
                    transform.location.x, transform.location.y, steering.turn, steering.thrust),
                    0, line);
            line += 20;
        }
    }
}
