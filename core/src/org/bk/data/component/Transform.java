package org.bk.data.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 10.10.2016.
 */
public class Transform implements Component {
    public final Vector2 location = new Vector2();
    public transient Location<Vector2> steerableLocation;
    public float orientRad;

    public Transform reset() {
        location.setZero();
        orientRad = 0;
        steerableLocation = null;
        return this;
    }
}
