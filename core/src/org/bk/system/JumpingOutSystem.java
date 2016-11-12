package org.bk.system;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.data.SolarSystem;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.Transform;
import org.bk.data.component.state.JumpedOut;
import org.bk.data.component.state.JumpingIn;
import org.bk.data.component.state.JumpingOut;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 20.10.2016.
 */
public class JumpingOutSystem extends IteratingSystem {
    private final Vector2 tv = new Vector2();
    private final Game game;

    public JumpingOutSystem(Game game, int priority) {
        super(Family.all(JumpingOut.class, Transform.class).get(), priority);
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                JUMPING_OUT.get(entity).startFrom.set(TRANSFORM.get(entity).location);
            }

            @Override
            public void entityRemoved(Entity entity) {

            }
        });
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        JumpingOut jumping = JUMPING_OUT.get(entity);
        entity.remove(Steering.class);
        entity.remove(Physics.class);
        Transform transform = TRANSFORM.get(entity);

        jumping.timeRemaining = Math.max(0, jumping.timeRemaining - deltaTime);

        SolarSystem targetSystem = jumping.to;
        transform.orientRad = tv.set(targetSystem.position).sub(game.currentSystem.position).angleRad();
        tv.set(Vector2.X).setAngleRad(transform.orientRad);
        float timePassed = JumpingOut.JUMP_OUT_DURATION - jumping.timeRemaining;
        float dst = timePassed * timePassed * timePassed * Game.JUMP_SCALE;
        transform.location.set(tv).scl(dst).add(jumping.startFrom);

        if (jumping.timeRemaining <= 0) {
            if (entity == game.playerEntity) {
                game.flash(0.2f);
                game.assets.snd_hyperdrive_shutdown.play();
                game.control.setTo(entity, JUMPING_IN, JumpingIn.class).from = game.currentSystem;
                PERSISTENCE.get(entity).system = targetSystem;
            } else {
                game.control.setTo(entity, JUMPED_OUT, JumpedOut.class).to = targetSystem;
            }
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }

}
