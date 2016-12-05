package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 10.10.2016.
 */
public class Body implements Component, Pool.Poolable {
    public final Vector2 dimension = new Vector2();
    public String graphics;
    public String explosionEffect;
    public float clipY = -1;

    @Override
    public void reset() {
        dimension.setZero();
        clipY = -1;
        explosionEffect = null;
        graphics = null;
    }
}
