package org.bk.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import org.bk.Assets;
import org.bk.Game;
import org.bk.component.*;

import static org.bk.ai.SteeringUtil.applySteering;
import static org.bk.component.Mapper.*;

/**
 * Created by dante on 16.10.2016.
 */
public class AISystem extends IteratingSystem {
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
        engine.addEntityListener(Family.all(Steering.class, Transform.class, AIControlled.class).get(), new MyAIEntityListener());
    }

    @Override
    public void update(float deltaTime) {
        line = Gdx.graphics.getHeight() - 20;
        uiBatch.begin();
        super.update(deltaTime);
        uiBatch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AIControlled aiControlled = AI_CONTROLLED.get(entity);
        Steerable<Vector2> steerable = aiControlled.steerable;
        Steering steering = STEERING.get(entity);
        Transform transform = TRANSFORM.get(entity);

        SteeringAcceleration<Vector2> tsa = applySteering(aiControlled.behavior, steerable, steering);
        assets.debugFont.draw(uiBatch, String.format("loc = (%1.1f, %1.1f) accel = (%1.1f, %1.1f) turn = %1.1f thrust = %1.1f",
                transform.location.x, transform.location.y, tsa.linear.x, tsa.linear.y, steering.turn, steering.thrust),
                0, line);
        line -= 20;
    }

    private class MyAIEntityListener implements EntityListener {
        @Override
        public void entityAdded(Entity entity) {
            AIControlled aiControlled = AI_CONTROLLED.get(entity);
//            aiControlled.steerable = SteeringUtil.toSteeringBehavior(entity);
        }

        @Override
        public void entityRemoved(Entity entity) {

        }
    }
}
