package org.bk.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.MatchVelocity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 16.10.2016.
 */
public class Attack extends BlendedSteering<Vector2> {
    private final BehaviorAndWeight<Vector2> pursue;
    private final BehaviorAndWeight<Vector2> matchVelocity;
    private final Steerable<Vector2> target;
    private float maxMatchDistance;

    public Attack(Steerable<Vector2> owner, Steerable<Vector2> target) {
        super(owner);
        this.target = target;

        Pursue<Vector2> pursue = new Pursue<Vector2>(owner, target);
        pursue.setMaxPredictionTime(10);
        this.pursue = new BehaviorAndWeight<Vector2>(pursue, 1);
        matchVelocity = new BehaviorAndWeight<Vector2>(new MatchVelocity<Vector2>(owner, target), 1);
        add(this.pursue);
        add(matchVelocity);
    }

    public void setMaxMatchDistance(float maxMatchDistance) {
        this.maxMatchDistance = maxMatchDistance;
    }

    @Override
    protected SteeringAcceleration<Vector2> calculateRealSteering(SteeringAcceleration<Vector2> blendedSteering) {
        float dst = target.getPosition().dst(owner.getPosition());
        float pw = Math.min(1, dst / (5 * maxMatchDistance + MathUtils.FLOAT_ROUNDING_ERROR));
        pw *= pw;
        pursue.setWeight(pw);
        matchVelocity.setWeight(1 - pw);
        return super.calculateRealSteering(blendedSteering);
    }
}
