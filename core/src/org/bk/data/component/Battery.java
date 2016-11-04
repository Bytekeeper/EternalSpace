package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 04.11.2016.
 */
public class Battery implements Component, Pool.Poolable {
    public float capacity;
    public float maxCapacity;

    @Override
    public void reset() {
        capacity = maxCapacity = 0;
    }
}
