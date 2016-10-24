package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.SolarSystem;

/**
 * Created by dante on 19.10.2016.
 */
public class Persistence implements Component, Pool.Poolable {
    public boolean temporary;
    public SolarSystem system;

    @Override
    public void reset() {
        temporary = false;
        system = null;
    }
}
