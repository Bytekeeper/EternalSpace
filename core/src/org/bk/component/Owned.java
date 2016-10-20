package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 20.10.2016.
 */
public class Owned implements Component, Pool.Poolable {
    public Entity owner;

    @Override
    public void reset() {
        owner = null;
    }
}
