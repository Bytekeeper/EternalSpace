package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 10.11.2016.
 */
public class ManualControl implements Component, Pool.Poolable {
    public float thrust;
    public float turn;

    @Override
    public void reset() {
        thrust = 0;
        turn = 0;
    }
}
