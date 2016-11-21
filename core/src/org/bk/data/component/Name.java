package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Created by dante on 21.11.2016.
 */
public class Name implements Component, Pool.Poolable {
    public String name;

    @Override
    public void reset() {
        name = null;
    }
}
