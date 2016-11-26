package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 16.10.2016.
 */
public class Effect implements Component, Pool.Poolable {
    public String effect;

    @Override
    public void reset() {
        effect = null;
    }
}
