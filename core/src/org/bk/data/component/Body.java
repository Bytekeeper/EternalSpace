package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 10.10.2016.
 */
public class Body implements Component {
    public final Vector2 dimension = new Vector2();
    public String graphics;

    public Body reset() {
        dimension.setZero();
        return this;
    }
}
