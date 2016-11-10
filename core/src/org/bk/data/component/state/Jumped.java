package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.SolarSystem;

/**
 * Created by dante on 10.11.2016.
 */
public class Jumped implements Component, Pool.Poolable {
    public SolarSystem to;

    @Override
    public void reset() {
        to = null;
    }
}
