package org.bk.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.Transform;
import org.bk.data.component.state.JumpingIn;
import org.bk.data.component.state.JumpingOut;
import org.bk.data.component.state.ManualControl;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 11.11.2016.
 */
public class JumpingInSystem extends IteratingSystem {
    private final Game game;
    private final Vector2 tv = new Vector2();

    public JumpingInSystem(Game game, int priority) {
        super(Family.all(JumpingIn.class, Transform.class).get(), priority);
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                JumpingIn jumpingIn = JUMPING_IN.get(entity);
                jumpingIn.arriveAt.setToRandomDirection().scl(MathUtils.random(0, 800));
            }

            @Override
            public void entityRemoved(Entity entity) {

            }
        });
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(Physics.class);
        entity.remove(Steering.class);

        JumpingIn jumpingIn = JUMPING_IN.get(entity);
        Transform transform = TRANSFORM.get(entity);

        jumpingIn.timeRemaining = Math.max(0, jumpingIn.timeRemaining - deltaTime);

        float targetOrientation = tv.set(game.currentSystem.position).sub(jumpingIn.from.position).angleRad();
        transform.orientRad = targetOrientation;
        tv.set(Vector2.X).setAngleRad(targetOrientation);
        float dst = jumpingIn.timeRemaining * jumpingIn.timeRemaining * jumpingIn.timeRemaining * Game.JUMP_SCALE;
        transform.location.set(tv).scl(-dst).add(jumpingIn.arriveAt);

        if (jumpingIn.timeRemaining <= 0) {
            entity.add(getEngine().createComponent(Physics.class));
            entity.add(getEngine().createComponent(Steering.class));
            game.control.setTo(entity, MANUAL_CONTROL, ManualControl.class);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
