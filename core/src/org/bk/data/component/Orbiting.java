package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 23.10.2016.
 */
public class Orbiting implements Component, Pool.Poolable {
    public float distance;
    public float speed;
    public Entity entity;
    public boolean tidalLock;
    public float tidalOrientation;

    @Override
    public void reset() {
        distance = 0;
        speed = 0;
        entity = null;
    }
}
