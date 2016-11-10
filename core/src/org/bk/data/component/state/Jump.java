package org.bk.data.component.state;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import org.bk.data.SolarSystem;

/**
 * Created by dante on 10.11.2016.
 */
public class Jump implements Component, Pool.Poolable {
    public SolarSystem target;

    @Override
    public void reset() {
        target = null;
    }
}
