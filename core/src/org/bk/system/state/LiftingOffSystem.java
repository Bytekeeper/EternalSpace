package org.bk.system.state;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import org.bk.Game;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.state.Land;
import org.bk.data.component.state.Landed;
import org.bk.data.component.state.LiftingOff;

import static org.bk.data.component.Mapper.LANDED;
import static org.bk.data.component.Mapper.LIFTING_OFF;

/**
 * Created by dante on 10.11.2016.
 */
public class LiftingOffSystem extends IteratingSystem {
    private final Game game;

    public LiftingOffSystem(Game game) {
        super(Family.all(LiftingOff.class).get());
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LiftingOff liftingOff = LIFTING_OFF.get(entity);
        Landed landed = (Landed) entity.remove(Landed.class);
        if (landed != null) {
            liftingOff.from = landed.on;
        }
        entity.remove(Physics.class);
        entity.remove(Steering.class);

        liftingOff.timeRemaining = Math.max(0, liftingOff.timeRemaining - deltaTime);

        if (game.playerEntity == entity) {
            float volume = MathUtils.lerp(Game.ENGINE_NOISE_VOLUME_LOW, 0, liftingOff.timeRemaining / LiftingOff.LIFTOFF_DURATION);
            game.assets.snd_engine_noise.setVolume(game.engine_noise_id, volume);
        }

        if (liftingOff.timeRemaining <= 0) {
            entity.add(getEngine().createComponent(Physics.class));
            entity.add(getEngine().createComponent(Steering.class));
            entity.remove(LiftingOff.class);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
