package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.SolarSystem;

/**
 * Created by dante on 20.10.2016.
 */
public class JumpingOut implements Component, Pool.Poolable {
    public static final float JUMP_OUT_DURATION = 3;
    public float timeRemaining = JUMP_OUT_DURATION;
    public SolarSystem to;

    @Override
    public void reset() {
        timeRemaining = JUMP_OUT_DURATION;
        to = null;
    }
}
