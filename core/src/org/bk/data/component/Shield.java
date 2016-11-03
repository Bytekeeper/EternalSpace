package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 03.11.2016.
 */
public class Shield implements Component, Pool.Poolable {
    public float shields;
    public float maxShields;

    @Override
    public void reset() {
        shields = maxShields = 0;
    }
}
