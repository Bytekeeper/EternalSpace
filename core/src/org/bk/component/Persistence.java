package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 19.10.2016.
 */
public class Persistence implements Component, Pool.Poolable {
    public boolean temporary;
    public String system;

    @Override
    public void reset() {
        temporary = false;
        system = null;
    }
}
