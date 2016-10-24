package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by dante on 17.10.2016.
 */
public class Celestial implements Component, Pool.Poolable {
    public Color radarColor;

    @Override
    public void reset() {
        radarColor = null;
    }
}
