package org.bk.ai.task;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 20.10.2016.
 */
public class Stop extends SteeringBehavior<Vector2> {
    private float orientation;

    public Stop(Steerable<Vector2> owner) {
        super(owner);
    }

    @Override
    protected SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> steering) {
        steering.linear.set(owner.getLinearVelocity()).scl(-10).limit(owner.getMaxLinearAcceleration());
        if (steering.linear.len2() < 30) {
            steering.linear.setZero();
        }
        steering.angular = MathUtils.clamp((orientation - owner.getOrientation() + MathUtils.PI * 3) % MathUtils.PI2 - MathUtils.PI,
                -owner.getMaxAngularAcceleration(), owner.getMaxLinearAcceleration());
        return steering;
    }

    @Override
    public Stop setOwner(Steerable<Vector2> owner) {
        return (Stop) super.setOwner(owner);
    }

    public Stop setOrientation(float orientation) {
        this.orientation = orientation;
        return this;
    }
}
