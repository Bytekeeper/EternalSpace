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
        steering.linear.set(owner.getLinearVelocity()).setLength(owner.getMaxLinearAcceleration()).
                scl(-Math.min(1, owner.getLinearVelocity().len2()));
        if (steering.linear.len2() < MathUtils.FLOAT_ROUNDING_ERROR) {
            steering.linear.setZero();
        }
        float delta = (orientation - owner.getOrientation() + MathUtils.PI * 3) % MathUtils.PI2 - MathUtils.PI;
        steering.angular = Math.signum(delta) * Math.min(1, Math.abs(delta) * 5) * owner.getMaxAngularAcceleration();
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
