package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import org.bk.system.SystemPopulateSystem;

/**
 * Created by dante on 20.10.2016.
 */
public class Jumping implements Component, Pool.Poolable {
    public static final float JUMP_DURATION = 5;
    public SystemPopulateSystem.SystemKey sourceOrTargetSystem;
    public float timeRemaining;
    public JumpDirection direction = JumpDirection.DEPART;
    public boolean hasPhysics;
    public boolean hasSteering;
    public final Vector2 referencePoint = new Vector2();

    @Override
    public void reset() {
        timeRemaining = 0;
        sourceOrTargetSystem = null;
        direction = JumpDirection.DEPART;
        hasPhysics = hasSteering = false;
        referencePoint.setZero();
    }

    public enum JumpDirection {
        DEPART,
        ARRIVE;
    }
}
