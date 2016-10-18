package org.bk.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by dante on 10.10.2016.
 */
public class Transform implements Component {
    public final Vector2 location = new Vector2();
    public float orientRad;
}
