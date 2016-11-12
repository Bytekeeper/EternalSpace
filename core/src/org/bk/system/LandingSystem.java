package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.Game;
import org.bk.data.component.*;
import org.bk.data.component.state.Landed;
import org.bk.data.component.state.Landing;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 18.10.2016.
 */
public class LandingSystem extends IteratingSystem {
    private final Game game;

    public LandingSystem(Game game, int priority) {
        super(Family.all(Landing.class).get(), priority);
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.remove(Physics.class);
        entity.remove(Steering.class);
        Landing landing = LANDING.get(entity);
        landing.timeRemaining = Math.max(0, landing.timeRemaining - deltaTime);
        if (landing.timeRemaining <= 0) {
            Entity landedOn = landing.on;
            game.control.setTo(entity, LANDED, Landed.class).on = landedOn;
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
