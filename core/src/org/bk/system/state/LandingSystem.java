package org.bk.system.state;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import org.bk.Game;
import org.bk.data.component.*;
import org.bk.data.component.state.*;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 18.10.2016.
 */
public class LandingSystem extends IteratingSystem {

    private final Game game;

    public LandingSystem(Game game) {
        super(Family.all(Landing.class).get());
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(Physics.class);
        entity.remove(Steering.class);
        Landing landing = LANDING.get(entity);
        if (landing.on == null) {
            Gdx.app.error(LandSystem.class.getSimpleName(), entity.getComponents() + " is landing on 'null'!");
        }
        landing.timeRemaining = Math.max(0, landing.timeRemaining - deltaTime);

        if (game.playerEntity == entity) {
            float volume = MathUtils.lerp(0, Game.ENGINE_NOISE_VOLUME_LOW, landing.timeRemaining / Landing.LANDING_DURATION);
            game.assets.snd_engine_noise.setVolume(game.engine_noise_id, volume);
        }

        if (landing.timeRemaining <= 0) {
            Entity landedOn = landing.on;

            Name name = NAME.get(landedOn);
            if (game.playerEntity == entity && name != null) {
                game.addMessage("Landed on " + name.name);
            }

            Landed landed = getEngine().createComponent(Landed.class);
            landed.on = landedOn;
            entity.add(landed);
            entity.remove(Landing.class);
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
