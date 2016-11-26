package org.bk.system.state;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.ai.Arrive;
import org.bk.ai.SteeringUtil;
import org.bk.data.component.*;
import org.bk.data.component.state.*;
import org.bk.system.Box2DPhysicsSystem;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 10.11.2016.
 */
public class LandSystem extends IteratingSystem {
    private final Arrive steerToLandingSpot = new Arrive(null, null);

    public LandSystem() {
        super(Family.all(Land.class, Transform.class, Movement.class, Steering.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                States.abortActions(entity, Land.class);
            }

            @Override
            public void entityRemoved(Entity entity) {

            }
        });
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (States.UNABORTABLE_ACTIONS.matches(entity)) {
            entity.remove(Land.class);
            return;
        }

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
            for (Box2DPhysicsSystem.TouchInfo ti : touching.touchList) {
                Entity e = ti.other;
                if (LANDING_PLACE.has(e)) {
                    Vector2 landingLocation = TRANSFORM.get(e).location;
                    if (ownerLocation.dst(landingLocation) < BODY.get(e).dimension.x / 2) {
                        Landing landing = getEngine().createComponent(Landing.class);
                        landing.on = e;
                        entity.add(landing);
                        entity.remove(Land.class);
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
