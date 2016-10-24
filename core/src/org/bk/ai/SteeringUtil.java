package org.bk.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.data.component.Movement;
import org.bk.data.component.Physics;
import org.bk.data.component.Steering;
import org.bk.data.component.Transform;

/**
 * Created by dante on 16.10.2016.
 */
public class SteeringUtil {
    private static SteeringAcceleration<Vector2> tsa = new SteeringAcceleration<Vector2>(new Vector2());

    public static SteeringAcceleration<Vector2> applySteering(SteeringBehavior<Vector2> behavior, Steerable<Vector2> steerable, Steering steering) {
        behavior.calculateSteering(tsa);
        if (tsa.linear.len2() > 2) {
            float delta = ((tsa.linear.angleRad() - steerable.getOrientation() + 3 * MathUtils.PI) % MathUtils.PI2) - MathUtils.PI;
            steering.turn = Math.signum(delta) * Math.min(1, Math.abs(delta) * 15);
            if (Math.abs(delta) < 0.05f) {
                float thrust = (0.05f - Math.abs(delta)) / 0.05f;
                thrust *= tsa.linear.len() / steerable.getMaxLinearAcceleration();
                steering.thrust = MathUtils.clamp(thrust, 0, 1);
            }
        } else {
            steering.turn = MathUtils.clamp(tsa.angular / steerable.getMaxAngularAcceleration(), -1, 1);
        }
        return tsa;
    }


    public static Steerable<Vector2> toSteeringBehavior(final Movement movement, final Transform transform, final Physics physics) {
        return new SteerableAdapter<Vector2>() {
            @Override
            public Vector2 getLinearVelocity() {
                return movement.velocity;
            }

            @Override
            public Vector2 getPosition() {
                return transform.location;
            }

            @Override
            public float getOrientation() {
                return transform.orientRad;
            }

            @Override
            public float getMaxLinearAcceleration() {
                return movement.linearThrust / physics.physicsBody.getMass();
            }

            @Override
            public float getMaxLinearSpeed() {
                return movement.maxVelocity;
            }

            @Override
            public float getAngularVelocity() {
                return physics.physicsBody.getAngularVelocity();
            }

            @Override
            public float getMaxAngularSpeed() {
                return movement.angularThrust / physics.physicsBody.getMass() / physics.physicsBody.getAngularDamping() / physics.physicsBody.getAngularDamping();
            }

            @Override
            public float getMaxAngularAcceleration() {
                return getMaxAngularSpeed();
            }

            @Override
            public float vectorToAngle(Vector2 vector) {
                return vector.angleRad();
            }

            @Override
            public Vector2 angleToVector(Vector2 outVector, float angle) {
                return outVector.set(1, 0).setAngleRad(angle);
            }
        };
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
