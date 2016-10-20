package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 19.10.2016.
 */
public class Character implements Component, Pool.Poolable {
    public Entity faction;

    @Override
    public void reset() {
        faction = null;
    }
}
