package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import org.bk.ai.Arrive;
import org.bk.ai.SteeringUtil;
import org.bk.ai.task.Stop;
import org.bk.component.Movement;
import org.bk.component.Physics;
import org.bk.component.Steering;
import org.bk.component.Transform;

import static org.bk.component.Mapper.*;

/**
 * Created by dante on 20.10.2016.
 */
public class SteeringModeSystem extends IteratingSystem {
    private final Arrive<Vector2> steerToLandingSpot = new Arrive<Vector2>(null, null);
    private final Stop stop = new Stop(null);

    private SteeringAcceleration<Vector2> tsa = new SteeringAcceleration<Vector2>(new Vector2());

    public SteeringModeSystem(int priority) {
        super(Family.all(Steering.class, Transform.class, Movement.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Steering steering = STEERING.get(entity);
        Movement movement = MOVEMENT.get(entity);
        Transform transform = TRANSFORM.get(entity);
        Physics physics = PHYSICS.get(entity);
        if (steering == null || physics == null) {
            return;
        }

        ensureSteerable(steering, movement, transform, physics);

        switch (steering.mode) {
            case FREE_FLIGHT:
                break;
            case LANDING:
                if (steering.modeTargetEntity == null) {
                    steering.mode = Steering.SteeringMode.FREE_FLIGHT;
                } else {
                    steerToLandingSpot.setOwner(steering.steerable).
                            setTarget(TRANSFORM.get(steering.modeTargetEntity).steerableLocation);
                    SteeringUtil.applySteering(steerToLandingSpot, steering.steerable, steering);
                }
                break;
            case JUMPING:
                stop.setOwner(steering.steerable).
                        setOrientation(getEngine().getSystem(SystemPopulateSystem.class).orientationToward(steering.jumpTo));
                SteeringUtil.applySteering(stop, steering.steerable, steering);
                break;
        }
    }

    private void ensureSteerable(Steering steering, Movement movement, Transform transform, Physics physics) {
        if (steering.steerable == null) {
            steering.steerable = SteeringUtil.toSteeringBehavior(movement, transform, physics);
        }
    }


}
