package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 08.11.2016.
 */
public class Damage implements Component, Pool.Poolable {
    public float overall;
    public float byPlayer;

    @Override
    public void reset() {

    }
}
