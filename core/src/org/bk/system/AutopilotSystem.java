package org.bk.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import org.bk.Game;
import org.bk.ai.Arrive;
import org.bk.ai.SteeringUtil;
import org.bk.ai.task.Stop;
import org.bk.data.component.Movement;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.Transform;

import static org.bk.data.component.Mapper.*;

/**
 * Created by dante on 20.10.2016.
 */
public class AutopilotSystem extends IteratingSystem {
    private final Arrive steerToLandingSpot = new Arrive(null, null);
    private final Stop stop = new Stop(null);
    private final Game game;
    private final Vector2 tv = new Vector2();

    public AutopilotSystem(Game game, int priority) {
        super(Family.all(Steering.class, Transform.class, Movement.class, Physics.class).get(), priority);
        this.game = game;
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

        Steerable<Vector2> steerable = steering.steerable;
        switch (steering.mode) {
            case LANDING:
                if (steering.modeTargetEntity == null) {
                    steering.mode = Steering.SteeringMode.FREE_FLIGHT;
                } else {
                    steerToLandingSpot.setOwner(steerable).
                            setTarget(TRANSFORM.get(steering.modeTargetEntity).steerableLocation);
                    SteeringUtil.applySteering(steerToLandingSpot, steerable, steering);
                }
                break;
            case JUMPING:
                tv.set(steering.jumpTo.position).sub(game.currentSystem.position).angleRad();
                stop.setOwner(steerable).
                        setOrientation(tv.angleRad());
                SteeringUtil.applySteering(stop, steerable, steering);
                break;
        }
    }

    private void ensureSteerable(Steering steering, Movement movement, Transform transform, Physics physics) {
        if (steering.steerable == null) {
            steering.steerable = SteeringUtil.toSteeringBehavior(movement, transform, physics);
        }
    }
}
