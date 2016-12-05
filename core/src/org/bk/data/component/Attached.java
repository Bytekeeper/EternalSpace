package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 04.12.2016.
 */
public class Attached implements Component, Pool.Poolable {
    public final Vector2 offset = new Vector2();
    public float orientRad;
    public Entity to;
    public boolean cascadeRemoval;

    @Override
    public void reset() {
        offset.setZero();
        orientRad = 0;
        to = null;
        cascadeRemoval = false;
    }
}
