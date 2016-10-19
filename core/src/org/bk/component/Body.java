package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by dante on 10.10.2016.
 */
public class Body implements Component {
    public final Vector2 dimension = new Vector2();

    public Body reset() {
        dimension.setZero();
        return this;
    }
}
