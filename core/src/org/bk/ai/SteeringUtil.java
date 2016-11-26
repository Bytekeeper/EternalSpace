package org.bk.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Util;
import org.bk.data.component.Movement;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.Transform;
import org.bk.system.Box2DPhysicsSystem;

/**
 * Created by dante on 16.10.2016.
 */
public class SteeringUtil {
    private static SteeringAcceleration<Vector2> tsa = new SteeringAcceleration<Vector2>(new Vector2());

    public static SteeringAcceleration<Vector2> applySteering(SteeringBehavior<Vector2> behavior, Steerable<Vector2> steerable, Steering steering) {
        behavior.calculateSteering(tsa);
        steering.thrust = 0;
        steering.turn = 0;
        if (tsa.linear.len2() > 30) {
            float delta = Util.deltaAngle(steerable.getOrientation(), tsa.linear.angleRad());
            if (Math.abs(delta) < 0.1f) {
                steering.thrust = 1;
            }
            float oversteer = steerable.getAngularVelocity() / 20f;
            delta = Util.deltaAngle(steerable.getOrientation() + oversteer, tsa.linear.angleRad());
            steering.turn = MathUtils.clamp(delta * 15, -1, 1);
        } else {
            steering.turn = MathUtils.clamp(tsa.angular / steerable.getMaxAngularAcceleration(), -1, 1);
        }
        return tsa;
    }



    public static Location<Vector2> toLocation(final Vector2 v) {
        return new Location<Vector2>() {
            @Override
            public Vector2 getPosition() {
                return v;
            }

            @Override
            public float getOrientation() {
                return 0;
            }

            @Override
            public void setOrientation(float orientation) {

            }

            @Override
            public float vectorToAngle(Vector2 vector) {
                return 0;
            }

            @Override
            public Vector2 angleToVector(Vector2 outVector, float angle) {
                return null;
            }

            @Override
            public Location<Vector2> newLocation() {
                return null;
            }
        };
    }
}
