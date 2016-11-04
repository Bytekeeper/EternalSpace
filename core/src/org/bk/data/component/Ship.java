package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 15.10.2016.
 */
public class Ship implements Component, Pool.Poolable {
    public float heat;

    @Override
    public void reset() {
        heat = 0;
    }
}
