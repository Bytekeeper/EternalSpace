package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 16.10.2016.
 */
public class Health implements Component, Pool.Poolable {
    public float hull;
    public float maxHull;

    @Override
    public void reset() {
        hull = maxHull = 0;
    }
}
