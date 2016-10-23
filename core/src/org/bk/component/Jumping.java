package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 20.10.2016.
 */
public class Jumping implements Component, Pool.Poolable {
    public static final float JUMP_DURATION = 8;
    public String sourceOrTargetSystem;
    public float timeRemaining = JUMP_DURATION / 2;
    public JumpDirection direction = JumpDirection.DEPART;
    public final Vector2 referencePoint = new Vector2();

    @Override
    public void reset() {
        timeRemaining = JUMP_DURATION / 2;
        sourceOrTargetSystem = null;
        direction = JumpDirection.DEPART;
        referencePoint.setZero();
    }

    public enum JumpDirection {
        DEPART,
        ARRIVE;
    }
}
