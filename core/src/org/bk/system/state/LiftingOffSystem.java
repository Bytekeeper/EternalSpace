package org.bk.system.state;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import org.bk.Game;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.state.Idle;
import org.bk.data.component.state.Landed;
import org.bk.data.component.state.LiftingOff;
import org.bk.fsm.TransitionListener;

import static org.bk.data.component.Mapper.LANDED;
import static org.bk.data.component.Mapper.LIFTING_OFF;

/**
 * Created by dante on 10.11.2016.
 */
public class LiftingOffSystem extends IteratingSystem {
    private final Game game;

    public LiftingOffSystem(Game game, int priority) {
        super(Family.all(LiftingOff.class).get(), priority);
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new TransitionListener(LiftingOff.class, Landed.class) {
            @Override
            protected void enterState(Entity entity) {
                LIFTING_OFF.get(entity).from = LANDED.get(entity).on;
            }
        });
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(Physics.class);
        entity.remove(Steering.class);

        LiftingOff liftingOff = LIFTING_OFF.get(entity);
        liftingOff.timeRemaining = Math.max(0, liftingOff.timeRemaining - deltaTime);

        if (game.playerEntity == entity) {
            float volume = MathUtils.lerp(Game.ENGINE_NOISE_VOLUME_LOW, 0, liftingOff.timeRemaining / LiftingOff.LIFTOFF_DURATION);
            game.assets.snd_engine_noise.setVolume(game.engine_noise_id, volume);
        }

        if (liftingOff.timeRemaining <= 0) {
            entity.add(getEngine().createComponent(Physics.class));
            entity.add(getEngine().createComponent(Steering.class));
            Idle idle = getEngine().createComponent(Idle.class);
            entity.add(idle);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
