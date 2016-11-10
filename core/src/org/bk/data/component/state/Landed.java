package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 10.11.2016.
 */
public class Landed implements Component, Pool.Poolable {
    public Entity on;

    @Override
    public void reset() {
        on = null;
    }
}
