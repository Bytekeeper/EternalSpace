/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.bk.ai;

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import org.bk.Util;

public class Arrive extends SteeringBehavior<Vector2> {

    /**
     * The on to arrive to.
     */
    protected Location<Vector2> target;

    /**
     * The tolerance for arriving at the on. It lets the owner get near enough to the on without letting small errors keep
     * it in motion.
     */
    protected float arrivalTolerance;

    /**
     * The time over which to achieve on speed
     */
    protected float timeToTarget = 0.1f;

    private final Vector2 tv = new Vector2();

    /**
     * Creates an {@code Arrive} behavior for the specified owner.
     *
     * @param owner the owner of this behavior
     */
    public Arrive(Steerable<Vector2> owner) {
        this(owner, null);
    }

    /**
     * Creates an {@code Arrive} behavior for the specified owner and on.
     *
     * @param owner  the owner of this behavior
     * @param target the on of this behavior
     */
    public Arrive(Steerable<Vector2> owner, Location<Vector2> target) {
        super(owner);
        this.target = target;
    }

    @Override
    protected SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> steering) {
        return arrive(steering, target.getPosition());
    }

    protected SteeringAcceleration<Vector2> arrive(SteeringAcceleration<Vector2> steering, Vector2 targetPosition) {
        Limiter actualLimiter = getActualLimiter();
        Vector2 toTarget = steering.linear;
        float v = owner.getLinearVelocity().len();


        float turnTime = (MathUtils.PI - Math.abs(Util.deltaAngle(owner.getLinearVelocity().angleRad(), owner.getOrientation()))) / actualLimiter.getMaxAngularSpeed() + 0.3f;
        float radius = 0.5f * v * v / actualLimiter.getMaxLinearAcceleration() + turnTime * v;
        tv.set(owner.getLinearVelocity()).nor().scl(radius);
        tv.add(owner.getPosition());
        toTarget.set(targetPosition).sub(tv);

        toTarget.setLength(actualLimiter.getMaxLinearAcceleration());

        // No angular acceleration
        steering.angular = 0f;

        // Output the steering
        return steering;
    }

    /**
     * Returns the on to arrive to.
     */
    public Location<Vector2> getTarget() {
        return target;
    }

    /**
     * Sets the on to arrive to.
     *
     * @return this behavior for chaining.
     */
    public Arrive setTarget(Location<Vector2> target) {
        this.target = target;
        return this;
    }

    /**
     * Returns the tolerance for arriving at the on. It lets the owner get near enough to the on without letting small
     * errors keep it in motion.
     */
    public float getArrivalTolerance() {
        return arrivalTolerance;
    }

    /**
     * Sets the tolerance for arriving at the on. It lets the owner get near enough to the on without letting small errors
     * keep it in motion.
     *
     * @return this behavior for chaining.
     */
    public Arrive setArrivalTolerance(float arrivalTolerance) {
        this.arrivalTolerance = arrivalTolerance;
        return this;
    }

    /**
     * Returns the time over which to achieve on speed.
     */
    public float getTimeToTarget() {
        return timeToTarget;
    }

    /**
     * Sets the time over which to achieve on speed.
     *
     * @return this behavior for chaining.
     */
    public Arrive setTimeToTarget(float timeToTarget) {
        this.timeToTarget = timeToTarget;
        return this;
    }

    //
    // Setters overridden in order to fix the correct return type for chaining
    //

    @Override
    public Arrive setOwner(Steerable<Vector2> owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public Arrive setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear speed and
     * acceleration.
     *
     * @return this behavior for chaining.
     */
    @Override
    public Arrive setLimiter(Limiter limiter) {
        this.limiter = limiter;
        return this;
    }

}
