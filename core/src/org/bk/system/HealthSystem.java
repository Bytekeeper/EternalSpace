package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.systems.IteratingSystem;
import org.bk.Game;
import org.bk.data.Faction;
import org.bk.data.component.Character;
import org.bk.data.component.Damage;
import org.bk.data.component.Health;
import org.bk.data.component.Steering;

import static org.bk.data.component.Mapper.CHARACTER;
import static org.bk.data.component.Mapper.DAMAGE;
import static org.bk.data.component.Mapper.HEALTH;

/**
 * Created by dante on 16.10.2016.
 */
public class HealthSystem extends IteratingSystem {
    private final Game game;

    public HealthSystem(Game game) {
        super(Family.all(Health.class).get());
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Health health = HEALTH.get(entity);
        if (health.hull == 0) {
            game.entityDestroyed.dispatch(entity);
            getEngine().removeEntity(entity);
        }
    }
}
