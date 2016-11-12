package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.ai.Arrive;
import org.bk.ai.SteeringUtil;
import org.bk.data.component.Movement;
import org.bk.data.component.Steering;
import org.bk.data.component.Touching;
import org.bk.data.component.Transform;
import org.bk.data.component.state.Land;
import org.bk.data.component.state.Landing;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 10.11.2016.
 */
public class LandSystem extends IteratingSystem {
    private final Arrive steerToLandingSpot = new Arrive(null, null);
    private final Game game;

    public LandSystem(Game game, int priority) {
        super(Family.all(Land.class, Transform.class, Movement.class, Steering.class).get(), priority);
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Steering steering = STEERING.get(entity);
        Steerable<Vector2> steerable = steering.steerable;
        Land land = LAND.get(entity);
        steerToLandingSpot.setOwner(steerable).
                setTarget(TRANSFORM.get(land.on).steerableLocation);
        SteeringUtil.applySteering(steerToLandingSpot, steerable, steering);

        Movement movement = MOVEMENT.get(entity);
        if (movement.velocity.len2() > Game.ACTION_VELOCITY_THRESHOLD2) {
            return;
        }
        Vector2 ownerLocation = TRANSFORM.get(entity).location;
        Touching touching = TOUCHING.get(entity);
         if (touching != null) {
            for (Entity e : touching.touchList) {
                if (LANDING_PLACE.has(e)) {
                    Vector2 landingLocation = TRANSFORM.get(e).location;
                    if (ownerLocation.dst(landingLocation) < BODY.get(e).dimension.x / 2) {
                        game.control.setTo(entity, LANDING, Landing.class).on = e;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public PooledEngine getEngine() {
        return (PooledEngine) super.getEngine();
    }
}
