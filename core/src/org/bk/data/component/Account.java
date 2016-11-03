package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 03.11.2016.
 */
public class Account implements Component, Pool.Poolable {
    public long credits;

    @Override
    public void reset() {
        credits = 0;
    }
}
